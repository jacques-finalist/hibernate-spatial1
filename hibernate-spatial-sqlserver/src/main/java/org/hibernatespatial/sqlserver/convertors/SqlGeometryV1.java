/*
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2009 Geovise BVBA
 *
 * This work was partially supported by the European Commission,
 * under the 6th Framework Programme, contract IST-2-004688-STP.
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

import org.hibernatespatial.mgeom.MCoordinate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.vividsolutions.jts.geom.Coordinate;

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

    public void setCoordinate(int index, Coordinate coordinate) {
        if (points == null) {
            initPointArrays();
        }
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
        if (isValid() && getNumPoints() == 1)
            return OpenGisType.POINT;
        return OpenGisType.INVALD_TYPE;
    }

    public void setHasZValues() {
        serializationPropertiesByte |= hasZValuesMask;
    }

    public void setHasMValues() {
        serializationPropertiesByte |= hasMValuesMask;
    }

    public void setIsValid() {
        serializationPropertiesByte |= isValidMask;
    }

    public void setIsSinglePoint() {
        serializationPropertiesByte |= isSinglePointMask;
    }

    public void setIsSingleLineSegment() {
        serializationPropertiesByte |= isSingleLineSegment;
    }

    public int getNumPoints() {
        return this.numberOfPoints;
    }

    public void setNumberOfPoints(int num) {
        this.numberOfPoints = num;
    }

    public void initPointArrays() {
        this.points = new Point[this.numberOfPoints];
        if (hasMValues())
            this.mValues = new double[this.numberOfPoints];
        if (hasZValues())
            this.zValues = new double[this.numberOfPoints];
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
    }

    private int calculateCapacity() {
        if (openGisType() == OpenGisType.POINT) {
            int capacity = 22;
            if (hasZValues())
                capacity += 8;
            if (hasMValues())
                capacity += 8;
            return capacity;
        }
        throw new IllegalArgumentException("Can't determine the capacity for type " + openGisType());
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

    private boolean isCompatible() {
        return version == SUPPORTED_VERSION;
    }

    public void setSrid(Integer srid) {
        this.srid = (srid == null) ? -1 : srid;
    }

    public Integer getSrid() {
        return srid != -1 ? srid : null;
    }

    private boolean hasZValues() {
        return (serializationPropertiesByte & hasZValuesMask) != 0;
    }

    private boolean hasMValues() {
        return (serializationPropertiesByte & hasMValuesMask) != 0;
    }

    private boolean isValid() {
        return (serializationPropertiesByte & isValidMask) != 0;
    }

    private boolean isSinglePoint() {
        return (serializationPropertiesByte & isSinglePointMask) != 0;
    }

    private boolean isSingleLineSegment() {
        return (serializationPropertiesByte & isSingleLineSegment) != 0;
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
