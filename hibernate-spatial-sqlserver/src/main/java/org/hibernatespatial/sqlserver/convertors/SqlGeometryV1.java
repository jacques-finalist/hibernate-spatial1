/*
 * $Id:$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2009 Geovise BVBA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, visit: http://www.hibernatespatial.org/
 */

package org.hibernatespatial.sqlserver.convertors;

import com.vividsolutions.jts.geom.Coordinate;
import org.hibernatespatial.mgeom.MCoordinate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
class SqlGeometryV1 {

    public static final byte SUPPORTED_VERSION = 1;

    private static final byte hasZValuesMask = 1;
    private static final byte hasMValuesMask = 2;
    private static final byte isValidMask = 4;
    private static final byte isSinglePointMask = 8;
    private static final byte isSingleLineSegment = 16;

    private ByteBuffer buffer;
    private Integer srid;
    private byte version;
    private byte serializationPropertiesByte;
    private int numberOfPoints;
    private Point[] points;
    private double[] mValues;
    private double[] zValues;
    private int numberOfFigures;
    private Figure[] figures = null;
    private int numberOfShapes;
    private Shape[] shapes = null;


    private SqlGeometryV1(byte[] bytes) {
        buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public SqlGeometryV1() {
    }

    //TODO -- refactor: all iterations in separate methods.
    public static byte[] store(SqlGeometryV1 sqlNative) {
        int capacity = sqlNative.calculateCapacity();
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(sqlNative.srid);
        buffer.put(SUPPORTED_VERSION);
        buffer.put(sqlNative.serializationPropertiesByte);
        if (!sqlNative.isSinglePoint() && !sqlNative.isSingleLineSegment()) {
            buffer.putInt(sqlNative.numberOfPoints);
        }
        for (int i = 0; i < sqlNative.points.length; i++) {
            buffer.putDouble(sqlNative.points[i].x);
            buffer.putDouble(sqlNative.points[i].y);
        }
        if (sqlNative.hasZValues()) {
            for (int i = 0; i < sqlNative.zValues.length; i++) {
                buffer.putDouble(sqlNative.zValues[i]);
            }
        }
        if (sqlNative.hasMValues()) {
            for (int i = 0; i < sqlNative.mValues.length; i++) {
                buffer.putDouble(sqlNative.mValues[i]);
            }
        }
        if (sqlNative.isSingleLineSegment() || sqlNative.isSinglePoint())
            return buffer.array();

        //in all other cases, we continue to store shapes and figures
        buffer.putInt(sqlNative.getNumFigures());
        for (int i = 0; i < sqlNative.getNumFigures(); i++) {
            sqlNative.getFigure(i).store(buffer);
        }

        buffer.putInt(sqlNative.getNumShapes());
        for (int i = 0; i < sqlNative.getNumShapes(); i++) {
            sqlNative.getShape(i).store(buffer);
        }

        return buffer.array();
    }

    public static SqlGeometryV1 load(byte[] bytes) {
        SqlGeometryV1 result = new SqlGeometryV1(bytes);
        result.parse();
        return result;
    }

    public MCoordinate getCoordinate(int index) {
        MCoordinate coordinate = new MCoordinate();
        coordinate.x = points[index].x;
        coordinate.y = points[index].y;
        if (hasZValues()) coordinate.z = zValues[index];
        if (hasMValues()) coordinate.m = mValues[index];
        return coordinate;
    }

    public Figure getFigure(int index) {
        return figures[index];
    }

    public Shape getShape(int index) {
        return shapes[index];
    }

    public void setCoordinate(int index, Coordinate coordinate) {
        Point pnt = new Point(coordinate.x, coordinate.y);
        points[index] = pnt;
        if (hasZValues()) {
            zValues[index] = coordinate.z;
        }
        if (hasMValues()) {
            mValues[index] = ((MCoordinate) coordinate).m;
        }
    }

    public boolean isEmpty() {
        return this.numberOfPoints == 0;
    }

    public OpenGisType openGisType() {
        if (isValid() && isSinglePoint())
            return OpenGisType.POINT;
        if (isValid() && isSingleLineSegment())
            return OpenGisType.LINESTRING;
        return firstShapeOpenGisType();
    }

    public void setHasZValues() {
        this.zValues = new double[this.numberOfPoints];
        serializationPropertiesByte |= hasZValuesMask;

    }


    public void setHasMValues() {
        this.mValues = new double[this.numberOfPoints];
        serializationPropertiesByte |= hasMValuesMask;
    }

    public void setIsValid() {
        serializationPropertiesByte |= isValidMask;
    }

    public void setIsSinglePoint() {
        setNumberOfPoints(1);
        serializationPropertiesByte |= isSinglePointMask;
    }

    public void setIsSingleLineSegment() {
        setNumberOfPoints(2);
        serializationPropertiesByte |= isSingleLineSegment;
    }

    public int getNumPoints() {
        return this.numberOfPoints;
    }

    public void setNumberOfPoints(int num) {
        this.numberOfPoints = num;
        this.points = new Point[this.numberOfPoints];
        if (num == 1) {
            this.setIsSinglePoint();
        }
    }

    private void parse() {
        srid = buffer.getInt();
        version = buffer.get();
        //TODO -- create a specific parse exception for this.
        if (!isCompatible())
            throw new IllegalStateException("Version mismatch. Expected version " + SUPPORTED_VERSION + ", but received version " + version);
        serializationPropertiesByte = buffer.get();
        determineNumberOfPoints();
        readPoints();
        if (hasZValues())
            readZValues();
        if (hasMValues())
            readMValues();

        if (isSingleLineSegment() ||
                isSinglePoint()) {
            return; //parsing can stop here
        }
        readFigures();
        readShapes();
    }

    private void readShapes() {
        setNumberOfShapes(buffer.getInt());
        for (int sIdx = 0; sIdx < numberOfShapes; sIdx++) {
            int parentOffset = buffer.getInt();
            int figureOffset = buffer.getInt();
            byte ogtByte = buffer.get();
            OpenGisType type = OpenGisType.valueOf(ogtByte);
            Shape shape = new Shape(parentOffset, figureOffset, type);
            setShape(sIdx, shape);
        }
    }

    private void readFigures() {
        setNumberOfFigures(buffer.getInt());
        for (int fIdx = 0; fIdx < numberOfFigures; fIdx++) {
            byte faByte = buffer.get();
            int pointOffset = buffer.getInt();
            FigureAttribute fa = FigureAttribute.valueOf(faByte);
            Figure figure = new Figure(fa, pointOffset);
            setFigure(fIdx, figure);
        }
    }

    private OpenGisType firstShapeOpenGisType() {
        if (shapes == null || shapes.length == 0)
            return OpenGisType.INVALID_TYPE;
        return shapes[0].openGisType;
    }

    private int calculateCapacity() {
        int numPoints = getNumPoints();
        int prefixSize = 6;

        if (isSinglePoint() ||
                isSingleLineSegment()) {
            int capacity = prefixSize + 16 * numPoints;
            if (hasZValues())
                capacity += 8 * numPoints;
            if (hasMValues())
                capacity += 8 * numPoints;
            return capacity;
        }

        int pointSize = getPointByteSize();
        int size = prefixSize + 3 * 4; // prefix + 3 ints for points, shapes and figures
        size += getNumPoints() * pointSize;
        size += getNumFigures() * Figure.getByteSize();
        size += getNumShapes() * Shape.getByteSize();
        return size;
    }

    private int getNumShapes() {
        return this.numberOfShapes;
    }

    private int getPointByteSize() {
        int size = 16; //for X/Y values
        if (hasMValues()) size += 8;
        if (hasZValues()) size += 8;
        return size;

    }

    private void readPoints() {
        points = new Point[numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++) {
            double x = buffer.getDouble();
            double y = buffer.getDouble();
            points[i] = new Point(x, y);
        }
    }

    private void readZValues() {
        zValues = new double[numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++) {
            zValues[i] = buffer.getDouble();
        }
    }


    private void readMValues() {
        mValues = new double[numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++) {
            mValues[i] = buffer.getDouble();
        }
    }

    private void determineNumberOfPoints() {
        if (isSinglePoint()) {
            numberOfPoints = 1;
            return;
        }
        if (isSingleLineSegment()) {
            numberOfPoints = 2;
            return;
        }
        numberOfPoints = buffer.getInt();
    }

    boolean isCompatible() {
        return version == SUPPORTED_VERSION;
    }

    void setSrid(Integer srid) {
        this.srid = (srid == null) ? -1 : srid;
    }

    Integer getSrid() {
        return srid != -1 ? srid : null;
    }

    boolean hasZValues() {
        return (serializationPropertiesByte & hasZValuesMask) != 0;
    }

    boolean hasMValues() {
        return (serializationPropertiesByte & hasMValuesMask) != 0;
    }

    boolean isValid() {
        return (serializationPropertiesByte & isValidMask) != 0;
    }

    boolean isSinglePoint() {
        return (serializationPropertiesByte & isSinglePointMask) != 0;
    }

    boolean isSingleLineSegment() {
        return (serializationPropertiesByte & isSingleLineSegment) != 0;
    }

    void setNumberOfFigures(int num) {
        numberOfFigures = num;
        figures = new Figure[numberOfFigures];
    }

    void setFigure(int i, Figure figure) {
        figures[i] = figure;
    }

    void setNumberOfShapes(int num) {
        numberOfShapes = num;
        shapes = new Shape[numberOfShapes];
    }

    void setShape(int i, Shape shape) {
        shapes[i] = shape;
    }

    int getNumFigures() {
        return this.numberOfFigures;
    }

    private static class Point {
        final double x;
        final double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

    }
}
