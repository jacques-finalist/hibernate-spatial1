/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *
 * Copyright © 2007 Geovise BVBA
 * Copyright © 2007 K.U. Leuven LRD, Spatial Applications Division, Belgium
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
package org.hibernatespatial.oracle;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;
import org.hibernate.HibernateException;
import org.hibernatespatial.AbstractDBGeometryType;
import org.hibernatespatial.Circle;
import org.hibernatespatial.HibernateSpatialException;
import org.hibernatespatial.helper.FinderException;
import org.hibernatespatial.mgeom.MCoordinate;
import org.hibernatespatial.mgeom.MLineString;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements Oracle 9i/10g/11g SDOGeometry type.
 *
 * @author Karel Maesen
 */
public class SDOGeometryType extends AbstractDBGeometryType {

    private static final int[] geometryTypes = new int[]{Types.STRUCT};

    static String SQL_TYPE_NAME = "MDSYS.SDO_GEOMETRY";


    static void setTypeName(String typeName) {
        SQL_TYPE_NAME = typeName;
    }

    static String getTypeName() {
        return SQL_TYPE_NAME;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlTypes()[0], SQL_TYPE_NAME);
        } else {
            Geometry jtsGeom = (Geometry) value;
            Object dbGeom = conv2DBGeometry(jtsGeom, st.getConnection());
            st.setObject(index, dbGeom);
        }
    }

    @Override
    public Object conv2DBGeometry(Geometry jtsGeom, Connection connection) {
        SDOGeometry geom = convertJTSGeometry(jtsGeom);
        if (geom != null)
            try {
                return SDOGeometry.store(geom, connection);
            } catch (SQLException e) {
                throw new HibernateSpatialException(
                        "Problem during conversion from JTS to SDOGeometry", e);
            } catch (FinderException e) {
                throw new HibernateSpatialException(
                        "OracleConnection could not be retrieved for creating SDOGeometry STRUCT", e);
            }
        else {
            throw new UnsupportedOperationException("Conversion of "
                    + jtsGeom.getClass().getSimpleName()
                    + " to Oracle STRUCT not supported");
        }
    }

    private SDOGeometry convertJTSGeometry(Geometry jtsGeom) {
        SDOGeometry geom = null;
        if (jtsGeom instanceof Point) {
            geom = convertJTSPoint((Point) jtsGeom);
        } else if (jtsGeom instanceof LineString) {
            geom = convertJTSLineString((LineString) jtsGeom);
        } else if (jtsGeom instanceof Polygon) {
            geom = convertJTSPolygon((Polygon) jtsGeom);
        } else if (jtsGeom instanceof MultiPoint) {
            geom = convertJTSMultiPoint((MultiPoint) jtsGeom);
        } else if (jtsGeom instanceof MultiLineString) {
            geom = convertJTSMultiLineString((MultiLineString) jtsGeom);
        } else if (jtsGeom instanceof MultiPolygon) {
            geom = convertJTSMultiPolygon((MultiPolygon) jtsGeom);
        } else if (jtsGeom instanceof GeometryCollection) {
            geom = convertJTSGeometryCollection((GeometryCollection) jtsGeom);
        }
        return geom;
    }

    private SDOGeometry convertJTSGeometryCollection(
            GeometryCollection collection) {
        SDOGeometry[] SDOElements = new SDOGeometry[collection
                .getNumGeometries()];
        for (int i = 0; i < collection.getNumGeometries(); i++) {
            Geometry geom = collection.getGeometryN(i);
            SDOElements[i] = convertJTSGeometry(geom);
        }
        SDOGeometry ccollect = SDOGeometry.join(SDOElements);
        ccollect.setSRID(collection.getSRID());
        return ccollect;
    }

    private SDOGeometry convertJTSMultiPolygon(MultiPolygon multiPolygon) {
        int dim = getCoordDimension(multiPolygon);
        int lrsPos = getCoordinateLrsPosition(multiPolygon);
        SDOGeometry geom = new SDOGeometry();
        geom.setGType(new SDOGType(dim, lrsPos, TypeGeometry.MULTIPOLYGON));
        geom.setSRID(multiPolygon.getSRID());
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            try {
                Polygon pg = (Polygon) multiPolygon.getGeometryN(i);
                addPolygon(geom, pg);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Found geometry that was not a geometry in MultiPolygon");
            }
        }
        return geom;
    }

    private SDOGeometry convertJTSLineString(LineString lineString) {
        int dim = getCoordDimension(lineString);
        int lrsPos = getCoordinateLrsPosition(lineString);
        boolean isLrs = lrsPos > 0;
        Double[] ordinates = convertCoordinates(lineString.getCoordinates(),
                dim, isLrs);
        SDOGeometry geom = new SDOGeometry();
        geom.setGType(new SDOGType(dim, lrsPos, TypeGeometry.LINE));
        geom.setSRID(lineString.getSRID());
        ElemInfo info = new ElemInfo(1);
        info.setElement(0, 1, ElementType.LINE_STRAITH_SEGMENTS, 0);
        geom.setInfo(info);
        geom.setOrdinates(new Ordinates(ordinates));
        return geom;

    }

    private SDOGeometry convertJTSMultiPoint(MultiPoint multiPoint) {
        int dim = getCoordDimension(multiPoint);
        int lrsDim = getCoordinateLrsPosition(multiPoint);
        boolean isLrs = (lrsDim != 0);
        SDOGeometry geom = new SDOGeometry();
        geom.setGType(new SDOGType(dim, lrsDim, TypeGeometry.MULTIPOINT));
        geom.setSRID(multiPoint.getSRID());
        ElemInfo info = new ElemInfo(multiPoint.getNumPoints());
        int oordinatesOffset = 1;
        Double[] ordinates = new Double[]{};
        for (int i = 0; i < multiPoint.getNumPoints(); i++) {
            info.setElement(i, oordinatesOffset, ElementType.POINT, 0);
            ordinates = convertAddCoordinates(ordinates, multiPoint
                    .getGeometryN(i).getCoordinates(), dim, isLrs);
            oordinatesOffset = ordinates.length + 1;
        }
        geom.setInfo(info);
        geom.setOrdinates(new Ordinates(ordinates));
        return geom;
    }

    private SDOGeometry convertJTSPoint(Point jtsGeom) {
        int dim = getCoordDimension(jtsGeom);

        int lrsDim = getCoordinateLrsPosition(jtsGeom);
        boolean isLrs = (lrsDim != 0);

        Double[] coord = convertCoordinates(jtsGeom.getCoordinates(), dim,
                isLrs);
        SDOGeometry geom = new SDOGeometry();
        geom.setGType(new SDOGType(dim, lrsDim, TypeGeometry.POINT));
        geom.setSRID(jtsGeom.getSRID());
        ElemInfo info = new ElemInfo(1);
        info.setElement(0, 1, ElementType.POINT, 1);
        geom.setInfo(info);
        geom.setOrdinates(new Ordinates(coord));
        return geom;
    }

    private SDOGeometry convertJTSPolygon(Polygon polygon) {
        int dim = getCoordDimension(polygon);
        int lrsPos = getCoordinateLrsPosition(polygon);
        SDOGeometry geom = new SDOGeometry();
        geom.setGType(new SDOGType(dim, lrsPos, TypeGeometry.POLYGON));
        geom.setSRID(polygon.getSRID());
        addPolygon(geom, polygon);
        return geom;
    }

    private void addPolygon(SDOGeometry geom, Polygon polygon) {
        int numInteriorRings = polygon.getNumInteriorRing();
        ElemInfo info = new ElemInfo(numInteriorRings + 1);
        int ordinatesPreviousOffset = 0;
        if (geom.getOrdinates() != null) {
            ordinatesPreviousOffset = geom.getOrdinates().getOrdinateArray().length;
        }
        int ordinatesOffset = ordinatesPreviousOffset + 1;
        Double[] ordinates = new Double[]{};
        for (int i = 0; i < info.getSize(); i++) {
            ElementType et;
            Coordinate[] coords;
            if (i == 0) {
                et = ElementType.EXTERIOR_RING_STRAIGHT_SEGMENTS;
                coords = polygon.getExteriorRing().getCoordinates();
                if (!CGAlgorithms.isCCW(coords)) {
                    coords = reverseRing(coords);
                }
            } else {
                et = ElementType.INTERIOR_RING_STRAIGHT_SEGMENTS;
                coords = polygon.getInteriorRingN(i - 1).getCoordinates();
                if (CGAlgorithms.isCCW(coords)) {
                    coords = reverseRing(coords);
                }
            }
            info.setElement(i, ordinatesOffset, et, 0);
            ordinates = convertAddCoordinates(ordinates, coords, geom
                    .getDimension(), geom.isLRSGeometry());
            ordinatesOffset = ordinatesPreviousOffset + ordinates.length + 1;
        }
        geom.addElement(info);
        geom.addOrdinates(ordinates);
    }

    private SDOGeometry convertJTSMultiLineString(
            MultiLineString multiLineString) {
        int dim = getCoordDimension(multiLineString);
        int lrsDim = getCoordinateLrsPosition(multiLineString);
        boolean isLrs = (lrsDim != 0);
        SDOGeometry geom = new SDOGeometry();
        geom.setGType(new SDOGType(dim, lrsDim, TypeGeometry.MULTILINE));
        geom.setSRID(multiLineString.getSRID());
        ElemInfo info = new ElemInfo(multiLineString.getNumGeometries());
        int oordinatesOffset = 1;
        Double[] ordinates = new Double[]{};
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            info.setElement(i, oordinatesOffset,
                    ElementType.LINE_STRAITH_SEGMENTS, 0);
            ordinates = convertAddCoordinates(ordinates, multiLineString
                    .getGeometryN(i).getCoordinates(), dim, isLrs);
            oordinatesOffset = ordinates.length + 1;
        }
        geom.setInfo(info);
        geom.setOrdinates(new Ordinates(ordinates));
        return geom;
    }

    private Double[] convertAddCoordinates(Double[] ordinates,
                                           Coordinate[] coordinates, int dim, boolean isLrs) {
        Double[] no = convertCoordinates(coordinates, dim, isLrs);
        Double[] newordinates = new Double[ordinates.length + no.length];
        System.arraycopy(ordinates, 0, newordinates, 0, ordinates.length);
        System.arraycopy(no, 0, newordinates, ordinates.length, no.length);
        return newordinates;
    }

    /**
     * Convert the coordinates to a double array for purposes of persisting them
     * to the database. Note that Double.NaN values are to be converted to null
     * values in the array.
     *
     * @param coordinates Coordinates to be converted to the array
     * @param dim         Coordinate dimension
     * @param isLrs       true if the coordinates contain measures
     * @return
     */
    private Double[] convertCoordinates(Coordinate[] coordinates, int dim,
                                        boolean isLrs) {
        if (dim > 4)
            throw new IllegalArgumentException(
                    "Dim parameter value cannot be greater than 4");
        Double[] converted = new Double[coordinates.length * dim];
        for (int i = 0; i < coordinates.length; i++) {
            MCoordinate c = MCoordinate.convertCoordinate(coordinates[i]);

            // set the X and Y values
            converted[i * dim] = toDouble(c.x);
            converted[i * dim + 1] = toDouble(c.y);
            if (dim == 3)
                converted[i * dim + 2] = isLrs ? toDouble(c.m) : toDouble(c.z);
            else if (dim == 4) {
                converted[i * dim + 2] = toDouble(c.z);
                converted[i * dim + 3] = toDouble(c.m);
            }
        }
        return converted;
    }

    /**
     * This method converts a double primitive to a Double wrapper instance, but
     * treats a Double.NaN value as null.
     *
     * @param d the value to be converted
     * @return A Double instance of d, Null if the parameter is Double.NaN
     */
    private Double toDouble(double d) {
        return Double.isNaN(d) ? null : d;
    }

    /**
     * Return the dimension required for building the gType in the SDOGeometry
     * object. Has support for LRS type geometries.
     *
     * @param geom and instance of the Geometry class from which the dimension is
     *             being extracted.
     * @return number of dimensions for purposes of creating the
     *         SDOGeometry.SDOGType
     */
    private int getCoordDimension(Geometry geom) {
        // This is awkward, I have to create an MCoordinate to discover what the
        // dimension is.
        // This shall be cleaner if MCoordinate.getOrdinate(int ordinateIndex)
        // is moved to the
        // Coordinate class
        MCoordinate c = MCoordinate.convertCoordinate(geom.getCoordinate());
        int d = 0;
        if (c != null) {
            if (!Double.isNaN(c.x))
                d++;
            if (!Double.isNaN(c.y))
                d++;
            if (!Double.isNaN(c.z))
                d++;
            if (!Double.isNaN(c.m))
                d++;
        }
        return d;
    }

    /**
     * Returns the lrs measure position for purposes of building the gType for
     * an oracle geometry. At this point and time, I'll have to assume that the
     * measure is always put at the end of the ordinate tuple, even though it
     * technically wouldn't have to. This method bases its decision on whether
     * the first coordinate has a measure value, as measure are required for the
     * very first and last measure in a CoordinateSequence. If there is no
     * measure value, 0 is returned.
     *
     * @param geom and instance of the Geometry class from which the lrs position
     *             is being extracted.
     * @return the lrs position for the SDOGeometry.SDOGType
     */
    private int getCoordinateLrsPosition(Geometry geom) {
        MCoordinate c = MCoordinate.convertCoordinate(geom.getCoordinate());
        int measurePos = 0;
        if (c != null && !Double.isNaN(c.m)) {
            measurePos = (Double.isNaN(c.z)) ? 3 : 4;
        }
        return measurePos;
    }

    @Override
    public Geometry convert2JTS(Object struct) {
        if (struct == null) {
            return null;
        }

        SDOGeometry SDOGeom = SDOGeometry.load((Struct) struct);
        return convert2JTS(SDOGeom);
    }

    public Geometry convert2JTS(SDOGeometry SDOGeom) {
        int dim = SDOGeom.getGType().getDimension();
        int lrsDim = SDOGeom.getGType().getLRSDimension();
        Geometry result = null;
        switch (SDOGeom.getGType().getTypeGeometry()) {
            case POINT:
                result = convertSDOPoint(SDOGeom);
                break;
            case LINE:
                result = convertSDOLine(dim, lrsDim, SDOGeom);
                break;
            case POLYGON:
                result = convertSDOPolygon(dim, lrsDim, SDOGeom);
                break;
            case MULTIPOINT:
                result = convertSDOMultiPoint(dim, lrsDim, SDOGeom);
                break;
            case MULTILINE:
                result = convertSDOMultiLine(dim, lrsDim, SDOGeom);
                break;
            case MULTIPOLYGON:
                result = convertSDOMultiPolygon(dim, lrsDim, SDOGeom);
                break;
            case COLLECTION:
                result = convertSDOCollection(dim, lrsDim, SDOGeom);
                break;
            default:
                throw new IllegalArgumentException("Type not supported: "
                        + SDOGeom.getGType().getTypeGeometry());
        }
        result.setSRID(SDOGeom.getSRID());
        return result;
    }

    private Geometry convertSDOCollection(int dim, int lrsDim,
                                          SDOGeometry SDOGeom) {
        List<Geometry> geometries = new ArrayList<Geometry>();
        for (SDOGeometry elemGeom : SDOGeom.getElementGeometries()) {
            geometries.add(convert2JTS(elemGeom));
        }
        Geometry[] geomArray = new Geometry[geometries.size()];
        return getGeometryFactory().createGeometryCollection(
                geometries.toArray(geomArray));
    }

    private Point convertSDOPoint(SDOGeometry SDOGeom) {
        Double[] ordinates = SDOGeom.getOrdinates().getOrdinateArray();
        if (ordinates.length == 0) {
            if (SDOGeom.getDimension() == 2) {
                ordinates = new Double[]{SDOGeom.getPoint().x,
                        SDOGeom.getPoint().y};
            } else {
                ordinates = new Double[]{SDOGeom.getPoint().x,
                        SDOGeom.getPoint().y, SDOGeom.getPoint().z};
            }
        }
        CoordinateSequence cs = convertOrdinateArray(ordinates, SDOGeom);
        Point point = getGeometryFactory().createPoint(cs);
        return point;
    }

    private MultiPoint convertSDOMultiPoint(int dim, int lrsDim,
                                            SDOGeometry SDOGeom) {
        Double[] ordinates = SDOGeom.getOrdinates().getOrdinateArray();
        CoordinateSequence cs = convertOrdinateArray(ordinates, SDOGeom);
        MultiPoint multipoint = getGeometryFactory().createMultiPoint(cs);
        return multipoint;
    }

    private LineString convertSDOLine(int dim, int lrsDim, SDOGeometry SDOGeom) {
        boolean lrs = SDOGeom.isLRSGeometry();
        ElemInfo info = SDOGeom.getInfo();
        CoordinateSequence cs = null;

        int i = 0;
        while (i < info.getSize()) {
            if (info.getElementType(i).isCompound()) {
                int numCompounds = info.getNumCompounds(i);
                cs = add(cs, getCompoundCSeq(i + 1, i + numCompounds, SDOGeom));
                i += 1 + numCompounds;
            } else {
                cs = add(cs, getElementCSeq(i, SDOGeom, false));
                i++;
            }
        }

        LineString ls = lrs ? getGeometryFactory().createMLineString(cs)
                : getGeometryFactory().createLineString(cs);
        return ls;
    }

    private MultiLineString convertSDOMultiLine(int dim, int lrsDim,
                                                SDOGeometry SDOGeom) {
        boolean lrs = SDOGeom.isLRSGeometry();
        ElemInfo info = SDOGeom.getInfo();
        LineString[] lines = lrs ? new MLineString[SDOGeom.getInfo().getSize()]
                : new LineString[SDOGeom.getInfo().getSize()];
        int i = 0;
        while (i < info.getSize()) {
            CoordinateSequence cs = null;
            if (info.getElementType(i).isCompound()) {
                int numCompounds = info.getNumCompounds(i);
                cs = add(cs, getCompoundCSeq(i + 1, i + numCompounds, SDOGeom));
                LineString line = lrs ? getGeometryFactory().createMLineString(
                        cs) : getGeometryFactory().createLineString(cs);
                lines[i] = line;
                i += 1 + numCompounds;
            } else {
                cs = add(cs, getElementCSeq(i, SDOGeom, false));
                LineString line = lrs ? getGeometryFactory().createMLineString(
                        cs) : getGeometryFactory().createLineString(cs);
                lines[i] = line;
                i++;
            }
        }

        MultiLineString mls = lrs ? getGeometryFactory()
                .createMultiMLineString((MLineString[]) lines)
                : getGeometryFactory().createMultiLineString(lines);
        return mls;

    }

    private Geometry convertSDOPolygon(int dim, int lrsDim, SDOGeometry SDOGeom) {
        LinearRing shell = null;
        LinearRing[] holes = new LinearRing[SDOGeom.getNumElements() - 1];
        ElemInfo info = SDOGeom.getInfo();
        int i = 0;
        int idxInteriorRings = 0;
        while (i < info.getSize()) {
            CoordinateSequence cs = null;
            int numCompounds = 0;
            if (info.getElementType(i).isCompound()) {
                numCompounds = info.getNumCompounds(i);
                cs = add(cs, getCompoundCSeq(i + 1, i + numCompounds, SDOGeom));
            } else {
                cs = add(cs, getElementCSeq(i, SDOGeom, false));
            }
            if (info.getElementType(i).isInteriorRing()) {
                holes[idxInteriorRings] = getGeometryFactory()
                        .createLinearRing(cs);
                idxInteriorRings++;
            } else {
                shell = getGeometryFactory().createLinearRing(cs);
            }
            i += 1 + numCompounds;
        }
        Polygon polygon = getGeometryFactory().createPolygon(shell, holes);
        return polygon;
    }

    private MultiPolygon convertSDOMultiPolygon(int dim, int lrsDim,
                                                SDOGeometry SDOGeom) {
        List<LinearRing> holes = new ArrayList<LinearRing>();
        List<Polygon> polygons = new ArrayList<Polygon>();
        ElemInfo info = SDOGeom.getInfo();
        LinearRing shell = null;
        int i = 0;
        while (i < info.getSize()) {
            CoordinateSequence cs = null;
            int numCompounds = 0;
            if (info.getElementType(i).isCompound()) {
                numCompounds = info.getNumCompounds(i);
                cs = add(cs, getCompoundCSeq(i + 1, i + numCompounds, SDOGeom));
            } else {
                cs = add(cs, getElementCSeq(i, SDOGeom, false));
            }
            if (info.getElementType(i).isInteriorRing()) {
                LinearRing lr = getGeometryFactory().createLinearRing(cs);
                holes.add(lr);
            } else {
                if (shell != null) {
                    Polygon polygon = getGeometryFactory().createPolygon(shell,
                            holes.toArray(new LinearRing[holes.size()]));
                    polygons.add(polygon);
                    shell = null;
                }
                shell = getGeometryFactory().createLinearRing(cs);
                holes = new ArrayList<LinearRing>();
            }
            i += 1 + numCompounds;
        }
        if (shell != null) {
            Polygon polygon = getGeometryFactory().createPolygon(shell,
                    holes.toArray(new LinearRing[holes.size()]));
            polygons.add(polygon);
        }
        MultiPolygon multiPolygon = getGeometryFactory().createMultiPolygon(
                polygons.toArray(new Polygon[polygons.size()]));
        return multiPolygon;
    }

    /**
     * Gets the CoordinateSequence corresponding to a compound element.
     *
     * @param idxFirst the first sub-element of the compound element
     * @param idxLast  the last sub-element of the compound element
     * @param SDOGeom  the SDOGeometry that holds the compound element.
     * @return
     */
    private CoordinateSequence getCompoundCSeq(int idxFirst, int idxLast,
                                               SDOGeometry SDOGeom) {
        CoordinateSequence cs = null;
        for (int i = idxFirst; i <= idxLast; i++) {
            // pop off the last element as it is added with the next
            // coordinate sequence
            if (cs != null && cs.size() > 0) {
                Coordinate[] coordinates = cs.toCoordinateArray();
                Coordinate[] newCoordinates = new Coordinate[coordinates.length - 1];
                System.arraycopy(coordinates, 0, newCoordinates, 0,
                        coordinates.length - 1);
                cs = getGeometryFactory().getCoordinateSequenceFactory()
                        .create(newCoordinates);
            }
            cs = add(cs, getElementCSeq(i, SDOGeom, (i < idxLast)));
        }
        return cs;
    }

    /**
     * Gets the CoordinateSequence corresponding to an element.
     *
     * @param i
     * @param SDOGeom
     * @return
     */
    private CoordinateSequence getElementCSeq(int i, SDOGeometry SDOGeom,
                                              boolean hasNextSE) {
        ElementType type = SDOGeom.getInfo().getElementType(i);
        Double[] elemOrdinates = extractOrdinatesOfElement(i, SDOGeom,
                hasNextSE);
        CoordinateSequence cs = null;
        if (type.isStraightSegment()) {
            cs = convertOrdinateArray(elemOrdinates, SDOGeom);
        } else if (type.isArcSegment() || type.isCircle()) {
            Coordinate[] linearized = linearize(elemOrdinates, SDOGeom
                    .getDimension(), SDOGeom.isLRSGeometry(), type.isCircle());
            cs = getGeometryFactory().getCoordinateSequenceFactory().create(
                    linearized);
        } else if (type.isRect()) {
            cs = convertOrdinateArray(elemOrdinates, SDOGeom);
            Coordinate ll = cs.getCoordinate(0);
            Coordinate ur = cs.getCoordinate(1);
            Coordinate lr = new Coordinate(ur.x, ll.y);
            Coordinate ul = new Coordinate(ll.x, ur.y);
            if (type.isExteriorRing()) {
                cs = getGeometryFactory().getCoordinateSequenceFactory()
                        .create(new Coordinate[]{ll, lr, ur, ul, ll});
            } else {
                cs = getGeometryFactory().getCoordinateSequenceFactory()
                        .create(new Coordinate[]{ll, ul, ur, lr, ll});
            }
        } else {
            throw new RuntimeException("Unexpected Element type in compound: "
                    + type);
        }
        return cs;
    }

    private CoordinateSequence add(CoordinateSequence seq1,
                                   CoordinateSequence seq2) {
        if (seq1 == null) {
            return seq2;
        }
        if (seq2 == null) {
            return seq1;
        }
        Coordinate[] c1 = seq1.toCoordinateArray();
        Coordinate[] c2 = seq2.toCoordinateArray();
        Coordinate[] c3 = new Coordinate[c1.length + c2.length];
        System.arraycopy(c1, 0, c3, 0, c1.length);
        System.arraycopy(c2, 0, c3, c1.length, c2.length);
        return getGeometryFactory().getCoordinateSequenceFactory().create(c3);
    }

    private Double[] extractOrdinatesOfElement(int element,
                                               SDOGeometry SDOGeom, boolean hasNextSE) {
        int start = SDOGeom.getInfo().getOrdinatesOffset(element);
        if (element < SDOGeom.getInfo().getSize() - 1) {
            int end = SDOGeom.getInfo().getOrdinatesOffset(element + 1);
            // if this is a subelement of a compound geometry,
            // the last point is the first point of
            // the next subelement.
            if (hasNextSE) {
                end += SDOGeom.getDimension();
            }
            return SDOGeom.getOrdinates().getOrdinatesArray(start, end);
        } else {
            return SDOGeom.getOrdinates().getOrdinatesArray(start);
        }
    }

    private CoordinateSequence convertOrdinateArray(Double[] oordinates,
                                                    SDOGeometry SDOGeom) {
        int dim = SDOGeom.getDimension();
        Coordinate[] coordinates = new Coordinate[oordinates.length / dim];
        int zDim = SDOGeom.getZDimension() - 1;
        int lrsDim = SDOGeom.getLRSDimension() - 1;
        for (int i = 0; i < coordinates.length; i++) {
            if (dim == 2)
                coordinates[i] = new Coordinate(oordinates[i * dim],
                        oordinates[i * dim + 1]);
            else if (dim == 3) {
                if (SDOGeom.isLRSGeometry()) {
                    coordinates[i] = MCoordinate.create2dWithMeasure(
                            oordinates[i * dim], // X
                            oordinates[i * dim + 1], // Y
                            oordinates[i * dim + lrsDim]); // M
                } else {
                    coordinates[i] = new Coordinate(oordinates[i * dim], // X
                            oordinates[i * dim + 1], // Y
                            oordinates[i * dim + zDim]); // Z
                }
            } else if (dim == 4) {
                // This must be an LRS Geometry
                if (!SDOGeom.isLRSGeometry())
                    throw new HibernateSpatialException(
                            "4 dimensional Geometries must be LRS geometry");
                coordinates[i] = MCoordinate.create3dWithMeasure(oordinates[i
                        * dim], // X
                        oordinates[i * dim + 1], // Y
                        oordinates[i * dim + zDim], // Z
                        oordinates[i * dim + lrsDim]); // M
            }
        }
        return getGeometryFactory().getCoordinateSequenceFactory().create(
                coordinates);
    }

    // reverses ordinates in a coordinate array in-place

    private Coordinate[] reverseRing(Coordinate[] ar) {
        for (int i = 0; i < ar.length / 2; i++) {
            Coordinate cs = ar[i];
            ar[i] = ar[ar.length - 1 - i];
            ar[ar.length - 1 - i] = cs;
        }
        return ar;
    }

    /**
     * Linearizes arcs and circles.
     *
     * @param arcOrdinates arc or circle coordinates
     * @param dim          coordinate dimension
     * @param lrs          whether this is an lrs geometry
     * @param entireCirlce whether the whole arc should be linearized
     * @return linearized interpolation of arcs or circle
     */
    private Coordinate[] linearize(Double[] arcOrdinates, int dim, boolean lrs,
                                   boolean entireCirlce) {
        Coordinate[] linearizedCoords = new Coordinate[0];
        // CoordDim is the dimension that includes only non-measure (X,Y,Z)
        // ordinates in its value
        int coordDim = lrs ? dim - 1 : dim;
        // this only works with 2-Dimensional geometries, since we use
        // JGeometry linearization;
        if (coordDim != 2)
            throw new IllegalArgumentException(
                    "Can only linearize 2D arc segments, but geometry is "
                            + dim + "D.");
        int numOrd = dim;
        while (numOrd < arcOrdinates.length) {
            numOrd = numOrd - dim;
            double x1 = arcOrdinates[numOrd++];
            double y1 = arcOrdinates[numOrd++];
            double m1 = lrs ? arcOrdinates[numOrd++] : Double.NaN;
            double x2 = arcOrdinates[numOrd++];
            double y2 = arcOrdinates[numOrd++];
            double m2 = lrs ? arcOrdinates[numOrd++] : Double.NaN;
            double x3 = arcOrdinates[numOrd++];
            double y3 = arcOrdinates[numOrd++];
            double m3 = lrs ? arcOrdinates[numOrd++] : Double.NaN;

            Coordinate[] coords;
            if (entireCirlce) {
                coords = Circle.linearizeCircle(x1, y1, x2, y2, x3, y3);
            } else {
                coords = Circle.linearizeArc(x1, y1, x2, y2, x3, y3);
            }

            // if this is an LRS geometry, fill the measure values into
            // the linearized array
            if (lrs) {
                MCoordinate[] mcoord = new MCoordinate[coords.length];
                int lastIndex = coords.length - 1;
                mcoord[0] = MCoordinate.create2dWithMeasure(x1, y1, m1);
                mcoord[lastIndex] = MCoordinate.create2dWithMeasure(x3, y3, m3);
                // convert the middle coordinates to MCoordinate
                for (int i = 1; i < lastIndex; i++) {
                    mcoord[i] = MCoordinate.convertCoordinate(coords[i]);
                    // if we happen to split on the middle measure, then
                    // assign it
                    if (Double.compare(mcoord[i].x, x2) == 0
                            && Double.compare(mcoord[i].y, y2) == 0) {
                        mcoord[i].m = m2;
                    }
                }
                coords = mcoord;
            }

            // if this is not the first arcsegment, the first linearized
            // point is already in linearizedArc, so disregard this.
            int resultBegin = 1;
            if (linearizedCoords.length == 0)
                resultBegin = 0;

            int destPos = linearizedCoords.length;
            Coordinate[] tmpCoords = new Coordinate[linearizedCoords.length
                    + coords.length - resultBegin];
            System.arraycopy(linearizedCoords, 0, tmpCoords, 0,
                    linearizedCoords.length);
            System.arraycopy(coords, resultBegin, tmpCoords, destPos,
                    coords.length - resultBegin);

            linearizedCoords = tmpCoords;
        }
        return linearizedCoords;
    }

    @Override
    public int[] sqlTypes() {
        return geometryTypes;
    }

}
