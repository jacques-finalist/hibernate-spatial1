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
import oracle.jdbc.driver.OracleConnection;
import oracle.sql.*;
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
 * Implements Oracle 9i/10g/11g SDO_GEOMETRY type.
 *
 * @author Karel Maesen
 */
public class SDOGeometryType extends AbstractDBGeometryType {

    private static final int[] geometryTypes = new int[]{Types.STRUCT};

    private static String SQL_TYPE_NAME = "MDSYS.SDO_GEOMETRY";

    private static ConnectionFinder connectionFinder = new DefaultConnectionFinder();

    static ConnectionFinder getConnectionFinder() {
        return connectionFinder;
    }

    static void setConnectionFinder(ConnectionFinder finder) {
        connectionFinder = finder;
    }

    static void setSQLTypeName(String typeName) {
        SQL_TYPE_NAME = typeName;
    }

    static String getSQLTypeName() {
        return SQL_TYPE_NAME;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlTypes()[0], SQL_TYPE_NAME);
        } else {
            Geometry jtsGeom = (Geometry) value;
            try {
                Object dbGeom = conv2DBGeometry(jtsGeom, getConnectionFinder()
                        .find(st.getConnection()));
                st.setObject(index, dbGeom);
            } catch (FinderException e) {
                throw new HibernateException(e);
            }
        }
    }

    @Override
    public Object conv2DBGeometry(Geometry jtsGeom, Connection connection) {
        SDO_GEOMETRY geom = convertJTSGeometry(jtsGeom);
        if (geom != null)
            try {
                return SDO_GEOMETRY.store(geom, (OracleConnection) connection);
            } catch (SQLException e) {
                throw new HibernateSpatialException(
                        "Problem during conversion from JTS to SDO_GEOMETRY", e);
            }
        else {
            throw new UnsupportedOperationException("Conversion of "
                    + jtsGeom.getClass().getSimpleName()
                    + " to Oracle STRUCT not supported");
        }
    }

    private SDO_GEOMETRY convertJTSGeometry(Geometry jtsGeom) {
        SDO_GEOMETRY geom = null;
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

    private SDO_GEOMETRY convertJTSGeometryCollection(
            GeometryCollection collection) {
        SDO_GEOMETRY[] sdoElements = new SDO_GEOMETRY[collection
                .getNumGeometries()];
        for (int i = 0; i < collection.getNumGeometries(); i++) {
            Geometry geom = collection.getGeometryN(i);
            sdoElements[i] = convertJTSGeometry(geom);
        }
        SDO_GEOMETRY ccollect = SDO_GEOMETRY.join(sdoElements);
        ccollect.setSRID(collection.getSRID());
        return ccollect;
    }

    private SDO_GEOMETRY convertJTSMultiPolygon(MultiPolygon multiPolygon) {
        int dim = getCoordDimension(multiPolygon);
        int lrsPos = getCoordinateLrsPosition(multiPolygon);
        SDO_GEOMETRY geom = new SDO_GEOMETRY();
        geom.setGType(new SDO_GTYPE(dim, lrsPos, TypeGeometry.MULTIPOLYGON));
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

    private SDO_GEOMETRY convertJTSLineString(LineString lineString) {
        int dim = getCoordDimension(lineString);
        int lrsPos = getCoordinateLrsPosition(lineString);
        boolean isLrs = lrsPos > 0;
        Double[] ordinates = convertCoordinates(lineString.getCoordinates(),
                dim, isLrs);
        SDO_GEOMETRY geom = new SDO_GEOMETRY();
        geom.setGType(new SDO_GTYPE(dim, lrsPos, TypeGeometry.LINE));
        geom.setSRID(lineString.getSRID());
        ELEM_INFO info = new ELEM_INFO(1);
        info.setElement(0, 1, ElementType.LINE_STRAITH_SEGMENTS, 0);
        geom.setInfo(info);
        geom.setOrdinates(new ORDINATES(ordinates));
        return geom;

    }

    private SDO_GEOMETRY convertJTSMultiPoint(MultiPoint multiPoint) {
        int dim = getCoordDimension(multiPoint);
        int lrsDim = getCoordinateLrsPosition(multiPoint);
        boolean isLrs = (lrsDim != 0);
        SDO_GEOMETRY geom = new SDO_GEOMETRY();
        geom.setGType(new SDO_GTYPE(dim, lrsDim, TypeGeometry.MULTIPOINT));
        geom.setSRID(multiPoint.getSRID());
        ELEM_INFO info = new ELEM_INFO(multiPoint.getNumPoints());
        int oordinatesOffset = 1;
        Double[] ordinates = new Double[]{};
        for (int i = 0; i < multiPoint.getNumPoints(); i++) {
            info.setElement(i, oordinatesOffset, ElementType.POINT, 0);
            ordinates = convertAddCoordinates(ordinates, multiPoint
                    .getGeometryN(i).getCoordinates(), dim, isLrs);
            oordinatesOffset = ordinates.length + 1;
        }
        geom.setInfo(info);
        geom.setOrdinates(new ORDINATES(ordinates));
        return geom;
    }

    private SDO_GEOMETRY convertJTSPoint(Point jtsGeom) {
        int dim = getCoordDimension(jtsGeom);

        int lrsDim = getCoordinateLrsPosition(jtsGeom);
        boolean isLrs = (lrsDim != 0);

        Double[] coord = convertCoordinates(jtsGeom.getCoordinates(), dim,
                isLrs);
        SDO_GEOMETRY geom = new SDO_GEOMETRY();
        geom.setGType(new SDO_GTYPE(dim, lrsDim, TypeGeometry.POINT));
        geom.setSRID(jtsGeom.getSRID());
        ELEM_INFO info = new ELEM_INFO(1);
        info.setElement(0, 1, ElementType.POINT, 1);
        geom.setInfo(info);
        geom.setOrdinates(new ORDINATES(coord));
        return geom;
    }

    private SDO_GEOMETRY convertJTSPolygon(Polygon polygon) {
        int dim = getCoordDimension(polygon);
        int lrsPos = getCoordinateLrsPosition(polygon);
        SDO_GEOMETRY geom = new SDO_GEOMETRY();
        geom.setGType(new SDO_GTYPE(dim, lrsPos, TypeGeometry.POLYGON));
        geom.setSRID(polygon.getSRID());
        addPolygon(geom, polygon);
        return geom;
    }

    private void addPolygon(SDO_GEOMETRY geom, Polygon polygon) {
        int numInteriorRings = polygon.getNumInteriorRing();
        ELEM_INFO info = new ELEM_INFO(numInteriorRings + 1);
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

    private SDO_GEOMETRY convertJTSMultiLineString(
            MultiLineString multiLineString) {
        int dim = getCoordDimension(multiLineString);
        int lrsDim = getCoordinateLrsPosition(multiLineString);
        boolean isLrs = (lrsDim != 0);
        SDO_GEOMETRY geom = new SDO_GEOMETRY();
        geom.setGType(new SDO_GTYPE(dim, lrsDim, TypeGeometry.MULTILINE));
        geom.setSRID(multiLineString.getSRID());
        ELEM_INFO info = new ELEM_INFO(multiLineString.getNumGeometries());
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
        geom.setOrdinates(new ORDINATES(ordinates));
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
     * Return the dimension required for building the gType in the SDO_GEOMETRY
     * object. Has support for LRS type geometries.
     *
     * @param geom and instance of the Geometry class from which the dimension is
     *             being extracted.
     * @return number of dimensions for purposes of creating the
     *         SDO_GEOMETRY.SDO_GTYPE
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
     * @return the lrs position for the SDO_GEOMETRY.SDO_GTYPE
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

        SDO_GEOMETRY sdoGeom = SDO_GEOMETRY.load((Struct) struct);
        return convert2JTS(sdoGeom);
    }

    public Geometry convert2JTS(SDO_GEOMETRY sdoGeom) {
        int dim = sdoGeom.getGType().getDimension();
        int lrsDim = sdoGeom.getGType().getLRSDimension();

        switch (sdoGeom.getGType().getTypeGeometry()) {
            case POINT:
                return convertSDOPoint(sdoGeom);
            case LINE:
                return convertSDOLine(dim, lrsDim, sdoGeom);
            case POLYGON:
                return convertSDOPolygon(dim, lrsDim, sdoGeom);
            case MULTIPOINT:
                return convertSDOMultiPoint(dim, lrsDim, sdoGeom);
            case MULTILINE:
                return convertSDOMultiLine(dim, lrsDim, sdoGeom);
            case MULTIPOLYGON:
                return convertSDOMultiPolygon(dim, lrsDim, sdoGeom);
            case COLLECTION:
                return convertSDOCollection(dim, lrsDim, sdoGeom);
            default:
                throw new IllegalArgumentException("Type not supported: "
                        + sdoGeom.getGType().getTypeGeometry());
        }

    }

    private Geometry convertSDOCollection(int dim, int lrsDim,
                                          SDO_GEOMETRY sdoGeom) {
        List<Geometry> geometries = new ArrayList<Geometry>();
        for (SDO_GEOMETRY elemGeom : sdoGeom.getElementGeometries()) {
            geometries.add(convert2JTS(elemGeom));
        }
        Geometry[] geomArray = new Geometry[geometries.size()];
        return getGeometryFactory().createGeometryCollection(
                geometries.toArray(geomArray));
    }

    private Point convertSDOPoint(SDO_GEOMETRY sdoGeom) {
        Double[] ordinates = sdoGeom.getOrdinates().getOrdinateArray();
        if (ordinates.length == 0) {
            if (sdoGeom.getDimension() == 2) {
                ordinates = new Double[]{sdoGeom.getPoint().x,
                        sdoGeom.getPoint().y};
            } else {
                ordinates = new Double[]{sdoGeom.getPoint().x,
                        sdoGeom.getPoint().y, sdoGeom.getPoint().z};
            }
        }
        CoordinateSequence cs = convertOrdinateArray(ordinates, sdoGeom);
        Point point = getGeometryFactory().createPoint(cs);

        point.setSRID(sdoGeom.getSRID());
        return point;
    }

    private MultiPoint convertSDOMultiPoint(int dim, int lrsDim,
                                            SDO_GEOMETRY sdoGeom) {
        Double[] ordinates = sdoGeom.getOrdinates().getOrdinateArray();
        CoordinateSequence cs = convertOrdinateArray(ordinates, sdoGeom);
        MultiPoint multipoint = getGeometryFactory().createMultiPoint(cs);
        multipoint.setSRID(sdoGeom.getSRID());
        return multipoint;
    }

    private LineString convertSDOLine(int dim, int lrsDim, SDO_GEOMETRY sdoGeom) {
        boolean lrs = sdoGeom.isLRSGeometry();
        ELEM_INFO info = sdoGeom.getInfo();
        CoordinateSequence cs = null;

        int i = 0;
        while (i < info.getSize()) {
            if (info.getElementType(i).isCompound()) {
                int numCompounds = info.getNumCompounds(i);
                cs = add(cs, getCompoundCSeq(i + 1, i + numCompounds, sdoGeom));
                i += 1 + numCompounds;
            } else {
                cs = add(cs, getElementCSeq(i, sdoGeom, false));
                i++;
            }
        }

        LineString ls = lrs ? getGeometryFactory().createMLineString(cs)
                : getGeometryFactory().createLineString(cs);
        ls.setSRID(sdoGeom.getSRID());
        return ls;
    }

    private MultiLineString convertSDOMultiLine(int dim, int lrsDim,
                                                SDO_GEOMETRY sdoGeom) {
        boolean lrs = sdoGeom.isLRSGeometry();
        ELEM_INFO info = sdoGeom.getInfo();
        LineString[] lines = lrs ? new MLineString[sdoGeom.getInfo().getSize()]
                : new LineString[sdoGeom.getInfo().getSize()];
        int i = 0;
        while (i < info.getSize()) {
            CoordinateSequence cs = null;
            if (info.getElementType(i).isCompound()) {
                int numCompounds = info.getNumCompounds(i);
                cs = add(cs, getCompoundCSeq(i + 1, i + numCompounds, sdoGeom));
                LineString line = lrs ? getGeometryFactory().createMLineString(
                        cs) : getGeometryFactory().createLineString(cs);
                lines[i] = line;
                i += 1 + numCompounds;
            } else {
                cs = add(cs, getElementCSeq(i, sdoGeom, false));
                LineString line = lrs ? getGeometryFactory().createMLineString(
                        cs) : getGeometryFactory().createLineString(cs);
                lines[i] = line;
                i++;
            }
        }

        MultiLineString mls = lrs ? getGeometryFactory()
                .createMultiMLineString((MLineString[]) lines)
                : getGeometryFactory().createMultiLineString(lines);
        mls.setSRID(sdoGeom.getSRID());
        return mls;

    }

    private Geometry convertSDOPolygon(int dim, int lrsDim, SDO_GEOMETRY sdoGeom) {
        LinearRing shell = null;
        LinearRing[] holes = new LinearRing[sdoGeom.getNumElements() - 1];
        ELEM_INFO info = sdoGeom.getInfo();
        int i = 0;
        int idxInteriorRings = 0;
        while (i < info.getSize()) {
            CoordinateSequence cs = null;
            int numCompounds = 0;
            if (info.getElementType(i).isCompound()) {
                numCompounds = info.getNumCompounds(i);
                cs = add(cs, getCompoundCSeq(i + 1, i + numCompounds, sdoGeom));
            } else {
                cs = add(cs, getElementCSeq(i, sdoGeom, false));
            }
            if (info.getElementType(i).isInteriorRing()) {
                holes[idxInteriorRings] = getGeometryFactory()
                        .createLinearRing(cs);
                holes[idxInteriorRings].setSRID(sdoGeom.getSRID());
                idxInteriorRings++;
            } else {
                shell = getGeometryFactory().createLinearRing(cs);
                shell.setSRID(sdoGeom.getSRID());
            }
            i += 1 + numCompounds;
        }
        Polygon polygon = getGeometryFactory().createPolygon(shell, holes);
        polygon.setSRID(sdoGeom.getSRID());
        return polygon;
    }

    private MultiPolygon convertSDOMultiPolygon(int dim, int lrsDim,
                                                SDO_GEOMETRY sdoGeom) {
        List<LinearRing> holes = new ArrayList<LinearRing>();
        List<Polygon> polygons = new ArrayList<Polygon>();
        ELEM_INFO info = sdoGeom.getInfo();
        LinearRing shell = null;
        int i = 0;
        while (i < info.getSize()) {
            CoordinateSequence cs = null;
            int numCompounds = 0;
            if (info.getElementType(i).isCompound()) {
                numCompounds = info.getNumCompounds(i);
                cs = add(cs, getCompoundCSeq(i + 1, i + numCompounds, sdoGeom));
            } else {
                cs = add(cs, getElementCSeq(i, sdoGeom, false));
            }
            if (info.getElementType(i).isInteriorRing()) {
                LinearRing lr = getGeometryFactory().createLinearRing(cs);
                lr.setSRID(sdoGeom.getSRID());
                holes.add(lr);
            } else {
                if (shell != null) {
                    Polygon polygon = getGeometryFactory().createPolygon(shell,
                            holes.toArray(new LinearRing[holes.size()]));
                    polygon.setSRID(sdoGeom.getSRID());
                    polygons.add(polygon);
                    shell = null;
                }
                shell = getGeometryFactory().createLinearRing(cs);
                shell.setSRID(sdoGeom.getSRID());
                holes = new ArrayList<LinearRing>();
            }
            i += 1 + numCompounds;
        }
        if (shell != null) {
            Polygon polygon = getGeometryFactory().createPolygon(shell,
                    holes.toArray(new LinearRing[holes.size()]));
            polygon.setSRID(sdoGeom.getSRID());
            polygons.add(polygon);
        }
        MultiPolygon multiPolygon = getGeometryFactory().createMultiPolygon(
                polygons.toArray(new Polygon[polygons.size()]));
        multiPolygon.setSRID(sdoGeom.getSRID());
        return multiPolygon;
    }

    /**
     * Gets the CoordinateSequence corresponding to a compound element.
     *
     * @param idxFirst the first sub-element of the compound element
     * @param idxLast  the last sub-element of the compound element
     * @param sdoGeom  the SDO_GEOMETRY that holds the compound element.
     * @return
     */
    private CoordinateSequence getCompoundCSeq(int idxFirst, int idxLast,
                                               SDO_GEOMETRY sdoGeom) {
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
            cs = add(cs, getElementCSeq(i, sdoGeom, (i < idxLast)));
        }
        return cs;
    }

    /**
     * Gets the CoordinateSequence corresponding to an element.
     *
     * @param i
     * @param sdoGeom
     * @return
     */
    private CoordinateSequence getElementCSeq(int i, SDO_GEOMETRY sdoGeom,
                                              boolean hasNextSE) {
        ElementType type = sdoGeom.getInfo().getElementType(i);
        Double[] elemOrdinates = extractOrdinatesOfElement(i, sdoGeom,
                hasNextSE);
        CoordinateSequence cs = null;
        if (type.isStraightSegment()) {
            cs = convertOrdinateArray(elemOrdinates, sdoGeom);
        } else if (type.isArcSegment() || type.isCircle()) {
            Coordinate[] linearized = linearize(elemOrdinates, sdoGeom
                    .getDimension(), sdoGeom.isLRSGeometry(), type.isCircle());
            cs = getGeometryFactory().getCoordinateSequenceFactory().create(
                    linearized);
        } else if (type.isRect()) {
            cs = convertOrdinateArray(elemOrdinates, sdoGeom);
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
                                               SDO_GEOMETRY sdoGeom, boolean hasNextSE) {
        int start = sdoGeom.getInfo().getOrdinatesOffset(element);
        if (element < sdoGeom.getInfo().getSize() - 1) {
            int end = sdoGeom.getInfo().getOrdinatesOffset(element + 1);
            // if this is a subelement of a compound geometry,
            // the last point is the first point of
            // the next subelement.
            if (hasNextSE) {
                end += sdoGeom.getDimension();
            }
            return sdoGeom.getOrdinates().getOrdinatesArray(start, end);
        } else {
            return sdoGeom.getOrdinates().getOrdinatesArray(start);
        }
    }

    private CoordinateSequence convertOrdinateArray(Double[] oordinates,
                                                    SDO_GEOMETRY sdoGeom) {
        int dim = sdoGeom.getDimension();
        Coordinate[] coordinates = new Coordinate[oordinates.length / dim];
        int zDim = sdoGeom.getZDimension() - 1;
        int lrsDim = sdoGeom.getLRSDimension() - 1;
        for (int i = 0; i < coordinates.length; i++) {
            if (dim == 2)
                coordinates[i] = new Coordinate(oordinates[i * dim],
                        oordinates[i * dim + 1]);
            else if (dim == 3) {
                if (sdoGeom.isLRSGeometry()) {
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
                if (!sdoGeom.isLRSGeometry())
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

    public static String arrayToString(Object array) {
        if (array == null || java.lang.reflect.Array.getLength(array) == 0) {
            return "()";
        }
        int length = java.lang.reflect.Array.getLength(array);
        StringBuilder stb = new StringBuilder();
        stb.append("(").append(java.lang.reflect.Array.get(array, 0));
        for (int i = 1; i < length; i++) {
            stb.append(",").append(java.lang.reflect.Array.get(array, i));
        }
        stb.append(")");
        return stb.toString();
    }

    public enum TypeGeometry {

        UNKNOWN_GEOMETRY(0), POINT(1), LINE(2), POLYGON(3), COLLECTION(4), MULTIPOINT(
                5), MULTILINE(6), MULTIPOLYGON(7), SOLID(8), MULTISOLID(9);

        private int gtype = 0;

        TypeGeometry(int gtype) {
            this.gtype = gtype;
        }

        int intValue() {
            return this.gtype;
        }

        static TypeGeometry parse(int v) {
            for (TypeGeometry gt : values()) {
                if (gt.intValue() == v) {
                    return gt;
                }
            }
            throw new RuntimeException("Value " + v
                    + " isn't a valid TypeGeometry value");
        }

    }

    public static enum ElementType {
        UNSUPPORTED(0, true), POINT(1, 1), ORIENTATION(1, 0), POINT_CLUSTER(1,
                true), LINE_STRAITH_SEGMENTS(2, 1), LINE_ARC_SEGMENTS(2, 2), INTERIOR_RING_STRAIGHT_SEGMENTS(
                2003, 1), EXTERIOR_RING_STRAIGHT_SEGMENTS(1003, 1), INTERIOR_RING_ARC_SEGMENTS(
                2003, 2), EXTERIOR_RING_ARC_SEGMENTS(1003, 2), INTERIOR_RING_RECT(
                2003, 3), EXTERIOR_RING_RECT(1003, 3), INTERIOR_RING_CIRCLE(
                2003, 4), EXTERIOR_RING_CIRCLE(1003, 4), COMPOUND_LINE(4, true), COMPOUND_EXTERIOR_RING(
                1005, true), COMPOUND_INTERIOR_RING(2005, true);

        private int etype;

        private int interpretation = 2;

        private boolean compound = false;

        private ElementType(int etype, int interp) {
            this.etype = etype;
            this.interpretation = interp;

        }

        private ElementType(int etype, boolean compound) {
            this.etype = etype;
            this.compound = compound;
        }

        public int getEType() {
            return this.etype;
        }

        public int getInterpretation() {
            return this.interpretation;
        }

        /**
         * @return true, if the SDO_INTERPRETATION value is the number of points
         *         or compounds in the element.
         */
        public boolean isCompound() {
            return this.compound;
        }

        public boolean isLine() {
            return (etype == 2 || etype == 4);
        }

        public boolean isInteriorRing() {
            return (etype == 2003 || etype == 2005);
        }

        public boolean isExteriorRing() {
            return (etype == 1003 || etype == 1005);
        }

        public boolean isStraightSegment() {
            return (interpretation == 1);
        }

        public boolean isArcSegment() {
            return (interpretation == 2);
        }

        public boolean isCircle() {
            return (interpretation == 4);
        }

        public boolean isRect() {
            return (interpretation == 3);
        }

        public static ElementType parseType(int etype, int interpretation) {
            for (ElementType t : values()) {
                if (t.etype == etype) {
                    if (t.isCompound()
                            || t.getInterpretation() == interpretation) {
                        return t;
                    }
                }
            }
            throw new RuntimeException(
                    "Can't determine ElementType from etype:" + etype
                            + " and interp.:" + interpretation);
        }

    }

    public static class SDO_GTYPE {

        private int dimension = 2;

        private int lrsDimension = 0;

        private TypeGeometry typeGeometry = TypeGeometry.UNKNOWN_GEOMETRY;

        public SDO_GTYPE(int dimension, int lrsDimension,
                         TypeGeometry typeGeometry) {
            setDimension(dimension);
            setLrsDimension(lrsDimension);
            setTypeGeometry(typeGeometry);
        }

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            if (dimension < 2 || dimension > 4) {
                throw new IllegalArgumentException(
                        "Dimension can only be 2,3 or 4.");
            }
            this.dimension = dimension;
        }

        public TypeGeometry getTypeGeometry() {
            return typeGeometry;
        }

        public void setTypeGeometry(TypeGeometry typeGeometry) {

            this.typeGeometry = typeGeometry;
        }

        public int getLRSDimension() {
            if (this.lrsDimension > 0) {
                return this.lrsDimension;
            } else if (this.lrsDimension == 0 && this.dimension == 4) {
                return 4;
            }
            return 0;
        }

        public int getZDimension() {
            if (this.dimension > 2) {
                if (!isLRSGeometry()) {
                    return this.dimension;
                } else {
                    return (getLRSDimension() < this.dimension ? 4 : 3);
                }
            }
            return 0;
        }

        public boolean isLRSGeometry() {
            return (this.lrsDimension > 0 || (this.lrsDimension == 0 && this.dimension == 4));
        }

        public void setLrsDimension(int lrsDimension) {
            if (lrsDimension != 0 && lrsDimension > this.dimension) {
                throw new IllegalArgumentException(
                        "lrsDimension must be 0 or lower or equal to dimenstion.");
            }
            this.lrsDimension = lrsDimension;
        }

        public int intValue() {
            int v = this.dimension * 1000;
            v += lrsDimension * 100;
            v += typeGeometry.intValue();
            return v;
        }

        public static SDO_GTYPE parse(int v) {
            int dim = v / 1000;
            v -= dim * 1000;
            int lrsDim = v / 100;
            v -= lrsDim * 100;
            TypeGeometry typeGeometry = TypeGeometry.parse(v);
            return new SDO_GTYPE(dim, lrsDim, typeGeometry);
        }

        public static SDO_GTYPE parse(Object datum) {

            try {
                int v = ((Number) datum).intValue();
                return parse(v);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        public String toString() {
            return Integer.toString(this.intValue());
        }
    }

    public static class SDO_POINT {
        public double x = 0.0;

        public double y = 0.0;

        public double z = Double.NaN;

        public SDO_POINT(Struct struct) {
            try {
                Object[] data = struct.getAttributes();
                this.x = ((Number) data[0]).doubleValue();
                this.y = ((Number) data[1]).doubleValue();
                if (data[2] != null) {
                    this.z = ((Number) data[1]).doubleValue();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public String toString() {
            StringBuilder stb = new StringBuilder();
            stb.append("(").append(x).append(",").append(y).append(",").append(
                    z).append(")");
            return stb.toString();
        }

    }

    private static int[] toIntArray(Array array) throws SQLException {
        Number[] numAr = (Number[]) array.getArray();
        int[] result = new int[numAr.length];
        int i = 0;
        for (Number number : numAr) {
            result[i++] = number.intValue();
        }
        return result;
    }

    public static class ELEM_INFO {

        private static final String TYPE_NAME = "MDSYS.SDO_ELEM_INFO_ARRAY";

        private int[] triplets;

        public ELEM_INFO(int size) {
            this.triplets = new int[3 * size];
        }

        public ELEM_INFO(int[] elem_info) {
            this.triplets = elem_info;
        }

        public ELEM_INFO(Array array) {
            if (array == null) {
                this.triplets = new int[]{};
                return;
            }
            try {
                triplets = toIntArray(array);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        public int[] getElements() {
            return this.triplets;
        }

        public int getSize() {
            return this.triplets.length / 3;
        }

        public int getOrdinatesOffset(int i) {
            return this.triplets[i * 3];
        }

        public void setOrdinatesOffset(int i, int offset) {
            this.triplets[i * 3] = offset;
        }

        public ElementType getElementType(int i) {
            int etype = this.triplets[i * 3 + 1];
            int interp = this.triplets[i * 3 + 2];
            ElementType et = ElementType.parseType(etype, interp);
            return et;
        }

        public boolean isCompound(int i) {
            return getElementType(i).isCompound();
        }

        public int getNumCompounds(int i) {
            if (getElementType(i).isCompound()) {
                return this.triplets[i * 3 + 2];
            } else {
                return 1;
            }
        }

        public void setElement(int i, int ordinatesOffset, ElementType et,
                               int numCompounds) {
            if (i > getSize()) {
                throw new RuntimeException(
                        "Attempted to set more elements in ELEM_INFO Array than capacity.");
            }
            this.triplets[i * 3] = ordinatesOffset;
            this.triplets[i * 3 + 1] = et.getEType();
            this.triplets[i * 3 + 2] = et.isCompound() ? numCompounds : et
                    .getInterpretation();
        }

        public String toString() {
            return arrayToString(this.triplets);
        }

        public void addElement(int[] element) {
            int[] newTriplets = new int[this.triplets.length + element.length];
            System.arraycopy(this.triplets, 0, newTriplets, 0,
                    this.triplets.length);
            System.arraycopy(element, 0, newTriplets, this.triplets.length,
                    element.length);
            this.triplets = newTriplets;
        }

        public void addElement(ELEM_INFO element) {
            this.addElement(element.getElements());
        }

        public int[] getElement(int i) {
            int[] ea = null;
            if (this.getElementType(i).isCompound()) {
                int numCompounds = this.getNumCompounds(i);
                ea = new int[numCompounds + 1];
            } else {
                ea = new int[3];
            }
            System.arraycopy(this.triplets, 3 * i, ea, 0, ea.length);
            return ea;
        }

        public ARRAY toOracleArray(Connection conn) throws SQLException {
            ArrayDescriptor arrayDescriptor = ArrayDescriptor.createDescriptor(
                    TYPE_NAME, conn);
            return new ARRAY(arrayDescriptor, conn, this.triplets);
        }
    }

    public static class ORDINATES {

        private static final String TYPE_NAME = "MDSYS.SDO_ORDINATE_ARRAY";

        private Double[] ordinates;

        public ORDINATES(Double[] ordinates) {
            this.ordinates = ordinates;
        }

        public ORDINATES(Array array) {
            if (array == null) {
                this.ordinates = new Double[]{};
                return;
            }
            try {
                Number[] ords = (Number[]) array.getArray();
                this.ordinates = new Double[ords.length];
                for (int i = 0; i < ords.length; i++) {
                    this.ordinates[i] = ords[i] != null ? ords[i].doubleValue()
                            : Double.NaN;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Double[] getOrdinateArray() {
            return this.ordinates;
        }

        public Double[] getOrdinatesArray(int startPosition, int endPosition) {
            Double[] a = new Double[endPosition - startPosition];
            System.arraycopy(this.ordinates, startPosition - 1, a, 0, a.length);
            return a;
        }

        public Double[] getOrdinatesArray(int startPosition) {
            Double[] a = new Double[this.ordinates.length - (startPosition - 1)];
            System.arraycopy(this.ordinates, startPosition - 1, a, 0, a.length);
            return a;
        }

        public String toString() {
            return arrayToString(this.ordinates);
        }

        public void addOrdinates(Double[] ordinatesToAdd) {
            Double[] newOrdinates = new Double[this.ordinates.length
                    + ordinatesToAdd.length];
            System.arraycopy(this.ordinates, 0, newOrdinates, 0,
                    this.ordinates.length);
            System.arraycopy(ordinatesToAdd, 0, newOrdinates,
                    this.ordinates.length, ordinatesToAdd.length);
            this.ordinates = newOrdinates;
        }

        public ARRAY toOracleArray(Connection conn) throws SQLException {
            ArrayDescriptor arrayDescriptor = ArrayDescriptor.createDescriptor(
                    TYPE_NAME, conn);
            return new ARRAY(arrayDescriptor, conn, this.ordinates);
        }

    }

    public static class SDO_GEOMETRY {

        private static final String TYPE_NAME = "MDSYS.SDO_GEOMETRY";

        private SDO_GTYPE gtype;

        private int srid;

        private SDO_POINT point;

        private ELEM_INFO info;

        private ORDINATES ordinates;

        public SDO_GEOMETRY() {

        }

        /**
         * This joins an array of SDO_GEOMETRIES to a SDO_GEOMETRY of type
         * COLLECTION
         *
         * @param sdoElements
         * @return
         */
        public static SDO_GEOMETRY join(SDO_GEOMETRY[] sdoElements) {
            SDO_GEOMETRY sdoCollection = new SDO_GEOMETRY();
            if (sdoElements == null || sdoElements.length == 0) {
                sdoCollection.setGType(new SDO_GTYPE(2, 0,
                        TypeGeometry.COLLECTION));
            } else {
                SDO_GEOMETRY firstElement = sdoElements[0];
                int dim = firstElement.getGType().getDimension();
                int lrsDim = firstElement.getGType().getLRSDimension();
                sdoCollection.setGType(new SDO_GTYPE(dim, lrsDim,
                        TypeGeometry.COLLECTION));
                int ordinatesOffset = 1;
                for (int i = 0; i < sdoElements.length; i++) {
                    ELEM_INFO element = sdoElements[i].getInfo();
                    Double[] ordinates = sdoElements[i].getOrdinates()
                            .getOrdinateArray();
                    if (element != null && element.getSize() > 0) {
                        int shift = ordinatesOffset
                                - element.getOrdinatesOffset(0);
                        shiftOrdinateOffset(element, shift);
                        sdoCollection.addElement(element);
                        sdoCollection.addOrdinates(ordinates);
                        ordinatesOffset += ordinates.length;
                    }
                }
            }
            return sdoCollection;
        }

        public ELEM_INFO getInfo() {
            return info;
        }

        public void setInfo(ELEM_INFO info) {
            this.info = info;
        }

        public SDO_GTYPE getGType() {
            return gtype;
        }

        public void setGType(SDO_GTYPE gtype) {
            this.gtype = gtype;
        }

        public ORDINATES getOrdinates() {
            return ordinates;
        }

        public void setOrdinates(ORDINATES ordinates) {
            this.ordinates = ordinates;
        }

        public SDO_POINT getPoint() {
            return point;
        }

        public void setPoint(SDO_POINT point) {
            this.point = point;
        }

        public int getSRID() {
            return srid;
        }

        public void setSRID(int srid) {
            this.srid = srid;
        }

        public static SDO_GEOMETRY load(Struct struct) {

            Object[] data;
            try {
                data = struct.getAttributes();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            SDO_GEOMETRY geom = new SDO_GEOMETRY();
            geom.setGType(SDO_GTYPE.parse(data[0]));
            geom.setSRID(data[1]);
            if (data[2] != null) {
                geom.setPoint(new SDO_POINT((Struct) data[2]));
            }
            geom.setInfo(new ELEM_INFO((Array) data[3]));
            geom.setOrdinates(new ORDINATES((Array) data[4]));

            return geom;
        }

        public static STRUCT store(SDO_GEOMETRY geom, Connection conn)
                throws SQLException {
            StructDescriptor structDescriptor = StructDescriptor
                    .createDescriptor(TYPE_NAME, conn);
            Datum[] attributes = new Datum[5];
            attributes[0] = new NUMBER(geom.getGType().intValue());
            if (geom.getSRID() > 0) {
                attributes[1] = new NUMBER(geom.getSRID());
            } else {
                attributes[1] = null;
            }
            attributes[3] = geom.getInfo().toOracleArray(conn);
            attributes[4] = geom.getOrdinates().toOracleArray(conn);
            return new STRUCT(structDescriptor, conn, attributes);

        }

        private void setSRID(Object datum) {
            if (datum == null) {
                this.srid = 0;
                return;
            }
            try {
                this.srid = new Integer(((Number) datum).intValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean isLRSGeometry() {
            return gtype.isLRSGeometry();
        }

        public int getDimension() {
            return gtype.getDimension();
        }

        public int getLRSDimension() {
            return gtype.getLRSDimension();
        }

        public int getZDimension() {
            return gtype.getZDimension();
        }

        /**
         * Gets the number of elements or compound elements.
         * <p/>
         * Subelements of a compound element are not counted.
         *
         * @return the number of elements
         */
        public int getNumElements() {
            int cnt = 0;
            int i = 0;
            while (i < info.getSize()) {
                if (info.getElementType(i).isCompound()) {
                    int numCompounds = info.getNumCompounds(i);
                    i += 1 + numCompounds;
                } else {
                    i++;
                }
                cnt++;
            }
            return cnt;
        }

        public String toString() {
            StringBuilder stb = new StringBuilder();
            stb.append("(").append(gtype).append(",").append(srid).append(",")
                    .append(point).append(",").append(info).append(",").append(
                    ordinates).append(")");
            return stb.toString();
        }

        public void addOrdinates(Double[] newOrdinates) {
            if (this.ordinates == null) {
                this.ordinates = new ORDINATES(newOrdinates);
            } else {
                this.ordinates.addOrdinates(newOrdinates);
            }
        }

        public void addElement(ELEM_INFO element) {
            if (this.info == null) {
                this.info = element;
            } else {
                this.info.addElement(element);
            }
        }

        /**
         * If this SDO_GEOMETRY is a COLLECTION, this method returns an array of
         * the SDO_GEOMETRIES that make up the collection. If not a Collection,
         * an array containing this SDO_GEOMETRY is returned.
         *
         * @return collection elements as individual SDO_GEOMETRIES
         */
        public SDO_GEOMETRY[] getElementGeometries() {
            if (getGType().getTypeGeometry() == TypeGeometry.COLLECTION) {
                List<SDO_GEOMETRY> elements = new ArrayList<SDO_GEOMETRY>();
                int i = 0;
                while (i < this.getNumElements()) {
                    ElementType et = this.getInfo().getElementType(i);
                    int next = i + 1;
                    // if the element is an exterior ring, or a compound
                    // element, then this geometry spans multiple elements.
                    if (et.isExteriorRing()) { // then next element is the
                        // first non-interior ring
                        while (next < this.getNumElements()) {
                            if (!this.getInfo().getElementType(next)
                                    .isInteriorRing()) {
                                break;
                            }
                            next++;
                        }
                    } else if (et.isCompound()) {
                        next = i + this.getInfo().getNumCompounds(i) + 1;
                    }
                    SDO_GEOMETRY elemGeom = new SDO_GEOMETRY();
                    SDO_GTYPE elemGtype = deriveGTYPE(this.getInfo()
                            .getElementType(i), this);
                    elemGeom.setGType(elemGtype);
                    elemGeom.setSRID(this.getSRID());
                    ELEM_INFO elemInfo = new ELEM_INFO(this.getInfo()
                            .getElement(i));
                    shiftOrdinateOffset(elemInfo, -elemInfo
                            .getOrdinatesOffset(0) + 1);
                    elemGeom.setInfo(elemInfo);
                    int startPosition = this.getInfo().getOrdinatesOffset(i);
                    ORDINATES elemOrdinates = null;
                    if (next < this.getNumElements()) {
                        int endPosition = this.getInfo().getOrdinatesOffset(
                                next);
                        elemOrdinates = new ORDINATES(this.getOrdinates()
                                .getOrdinatesArray(startPosition, endPosition));
                    } else {
                        elemOrdinates = new ORDINATES(this.getOrdinates()
                                .getOrdinatesArray(startPosition));
                    }
                    elemGeom.setOrdinates(elemOrdinates);
                    elements.add(elemGeom);
                    i = next;
                }
                return elements.toArray(new SDO_GEOMETRY[elements.size()]);
            } else {
                return new SDO_GEOMETRY[]{this};
            }
        }

        private static void shiftOrdinateOffset(ELEM_INFO elemInfo, int offset) {
            for (int i = 0; i < elemInfo.getSize(); i++) {
                int newOffset = elemInfo.getOrdinatesOffset(i) + offset;
                elemInfo.setOrdinatesOffset(i, newOffset);
            }
        }

    }

    private static SDO_GTYPE deriveGTYPE(ElementType elementType,
                                         SDO_GEOMETRY origGeom) {
        switch (elementType) {
            case POINT:
            case ORIENTATION:
                return new SDO_GTYPE(origGeom.getDimension(), origGeom
                        .getLRSDimension(), TypeGeometry.POINT);
            case POINT_CLUSTER:
                return new SDO_GTYPE(origGeom.getDimension(), origGeom
                        .getLRSDimension(), TypeGeometry.MULTIPOINT);
            case LINE_ARC_SEGMENTS:
            case LINE_STRAITH_SEGMENTS:
            case COMPOUND_LINE:
                return new SDO_GTYPE(origGeom.getDimension(), origGeom
                        .getLRSDimension(), TypeGeometry.LINE);
            case COMPOUND_EXTERIOR_RING:
		case EXTERIOR_RING_ARC_SEGMENTS:
		case EXTERIOR_RING_CIRCLE:
		case EXTERIOR_RING_RECT:
		case EXTERIOR_RING_STRAIGHT_SEGMENTS:
			return new SDO_GTYPE(origGeom.getDimension(), origGeom
					.getLRSDimension(), TypeGeometry.POLYGON);
		}
		return null;
	}

}
