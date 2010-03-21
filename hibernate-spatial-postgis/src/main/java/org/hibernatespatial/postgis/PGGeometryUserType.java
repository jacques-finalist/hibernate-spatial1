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
package org.hibernatespatial.postgis;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.hibernatespatial.AbstractDBGeometryType;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.mgeom.MCoordinate;
import org.hibernatespatial.mgeom.MGeometry;
import org.hibernatespatial.mgeom.MLineString;
import org.postgis.*;

import java.sql.Connection;
import java.sql.Types;

/**
 * Specific <code>GeometryType</code> for Postgis geometry type
 *
 * @author Karel Maesen
 */
public class PGGeometryUserType extends AbstractDBGeometryType {

    private static final int[] geometryTypes = new int[]{Types.STRUCT};

    public int[] sqlTypes() {
        return geometryTypes;
    }

    /**
     * Converts the native geometry object to a JTS <code>Geometry</code>.
     *
     * @param object native database geometry object (depends on the JDBC spatial
     *               extension of the database)
     * @return JTS geometry corresponding to geomObj.
     */
    public Geometry convert2JTS(Object object) {
        if (object == null)
            return null;

        // in some cases, Postgis returns not PGgeometry objects
        // but org.postgis.Geometry instances.
        // This has been observed when retrieving GeometryCollections
        // as the result of an SQL-operation such as Union.
        if (object instanceof org.postgis.Geometry) {
            object = new PGgeometry((org.postgis.Geometry) object);
        }

        if (object instanceof PGgeometry) {
            PGgeometry geom = (PGgeometry) object;
            com.vividsolutions.jts.geom.Geometry out = null;
            switch (geom.getGeoType()) {
                case org.postgis.Geometry.POINT:
                    out = convertPoint((org.postgis.Point) geom.getGeometry());
                    break;
                case org.postgis.Geometry.LINESTRING:
                    out = convertLineString((org.postgis.LineString) geom
                            .getGeometry());
                    break;
                case org.postgis.Geometry.POLYGON:
                    out = convertPolygon((org.postgis.Polygon) geom.getGeometry());
                    break;
                case org.postgis.Geometry.MULTILINESTRING:
                    out = convertMultiLineString((org.postgis.MultiLineString) geom
                            .getGeometry());
                    break;
                case org.postgis.Geometry.MULTIPOINT:
                    out = convertMultiPoint((org.postgis.MultiPoint) geom
                            .getGeometry());
                    break;
                case org.postgis.Geometry.MULTIPOLYGON:
                    out = convertMultiPolygon((org.postgis.MultiPolygon) geom
                            .getGeometry());
                    break;
                case org.postgis.Geometry.GEOMETRYCOLLECTION:
                    out = convertGeometryCollection((org.postgis.GeometryCollection) geom
                            .getGeometry());
                    break;
                default:
                    throw new RuntimeException("Unknown type of PGgeometry");
            }

            return out;
        } else if (object instanceof org.postgis.PGboxbase) {
            return convertBox((org.postgis.PGboxbase) object);
        } else {
            throw new IllegalArgumentException("Can't convert object of type "
                    + object.getClass().getCanonicalName());

        }

    }

    private Geometry convertBox(PGboxbase box) {
        Point ll = box.getLLB();
        Point ur = box.getURT();
        Coordinate[] ringCoords = new Coordinate[5];
        if (box instanceof org.postgis.PGbox2d) {
            ringCoords[0] = new Coordinate(ll.x, ll.y);
            ringCoords[1] = new Coordinate(ur.x, ll.y);
            ringCoords[2] = new Coordinate(ur.x, ur.y);
            ringCoords[3] = new Coordinate(ll.x, ur.y);
            ringCoords[4] = new Coordinate(ll.x, ll.y);
        } else {
            ringCoords[0] = new Coordinate(ll.x, ll.y, ll.z);
            ringCoords[1] = new Coordinate(ur.x, ll.y, ll.z);
            ringCoords[2] = new Coordinate(ur.x, ur.y, ur.z);
            ringCoords[3] = new Coordinate(ll.x, ur.y, ur.z);
            ringCoords[4] = new Coordinate(ll.x, ll.y, ll.z);
        }
        com.vividsolutions.jts.geom.LinearRing shell = getGeometryFactory()
                .createLinearRing(ringCoords);
        return getGeometryFactory().createPolygon(shell, null);
    }

    private Geometry convertGeometryCollection(GeometryCollection collection) {
        org.postgis.Geometry[] geometries = collection.getGeometries();
        com.vividsolutions.jts.geom.Geometry[] jtsGeometries = new com.vividsolutions.jts.geom.Geometry[geometries.length];
        for (int i = 0; i < geometries.length; i++) {
            jtsGeometries[i] = convert2JTS(geometries[i]);
        }
        com.vividsolutions.jts.geom.GeometryCollection jtsGCollection = getGeometryFactory()
                .createGeometryCollection(jtsGeometries);
        return jtsGCollection;
    }

    private Geometry convertMultiPolygon(MultiPolygon pgMultiPolygon) {
        com.vividsolutions.jts.geom.Polygon[] polygons = new com.vividsolutions.jts.geom.Polygon[pgMultiPolygon
                .numPolygons()];

        for (int i = 0; i < polygons.length; i++) {
            Polygon pgPolygon = pgMultiPolygon.getPolygon(i);
            polygons[i] = (com.vividsolutions.jts.geom.Polygon) convertPolygon(pgPolygon);
        }

        com.vividsolutions.jts.geom.MultiPolygon out = getGeometryFactory()
                .createMultiPolygon(polygons);
        out.setSRID(pgMultiPolygon.srid);
        return out;
    }

    private Geometry convertMultiPoint(MultiPoint pgMultiPoint) {
        com.vividsolutions.jts.geom.Point[] points = new com.vividsolutions.jts.geom.Point[pgMultiPoint
                .numPoints()];

        for (int i = 0; i < points.length; i++) {
            points[i] = convertPoint(pgMultiPoint.getPoint(i));
        }
        com.vividsolutions.jts.geom.MultiPoint out = getGeometryFactory()
                .createMultiPoint(points);
        out.setSRID(pgMultiPoint.srid);
        return out;
    }

    private com.vividsolutions.jts.geom.Geometry convertMultiLineString(
            MultiLineString mlstr) {
        com.vividsolutions.jts.geom.MultiLineString out;
        if (mlstr.haveMeasure) {
            MLineString[] lstrs = new MLineString[mlstr.numLines()];
            for (int i = 0; i < mlstr.numLines(); i++) {
                MCoordinate[] coordinates = toJTSCoordinates(mlstr.getLine(i)
                        .getPoints());
                lstrs[i] = getGeometryFactory().createMLineString(coordinates);
            }
            out = getGeometryFactory().createMultiMLineString(lstrs);
        } else {
            com.vividsolutions.jts.geom.LineString[] lstrs = new com.vividsolutions.jts.geom.LineString[mlstr
                    .numLines()];
            for (int i = 0; i < mlstr.numLines(); i++) {
                lstrs[i] = getGeometryFactory().createLineString(
                        toJTSCoordinates(mlstr.getLine(i).getPoints()));
            }
            out = getGeometryFactory().createMultiLineString(lstrs);
        }
        out.setSRID(mlstr.srid);
        return out;
    }

    protected com.vividsolutions.jts.geom.Geometry convertPolygon(
            Polygon polygon) {
        com.vividsolutions.jts.geom.LinearRing shell = getGeometryFactory()
                .createLinearRing(
                        toJTSCoordinates(polygon.getRing(0).getPoints()));
        com.vividsolutions.jts.geom.Polygon out = null;
        if (polygon.numRings() > 1) {
            com.vividsolutions.jts.geom.LinearRing[] rings = new com.vividsolutions.jts.geom.LinearRing[polygon
                    .numRings() - 1];
            for (int r = 1; r < polygon.numRings(); r++) {
                rings[r - 1] = getGeometryFactory().createLinearRing(
                        toJTSCoordinates(polygon.getRing(r).getPoints()));
            }
            out = getGeometryFactory().createPolygon(shell, rings);
        } else {
            out = getGeometryFactory().createPolygon(shell, null);
        }
        out.setSRID(polygon.srid);
        return out;
    }

    protected com.vividsolutions.jts.geom.Point convertPoint(Point pnt) {
        com.vividsolutions.jts.geom.Point g = getGeometryFactory().createPoint(
                this.toJTSCoordinate(pnt));
        g.setSRID(pnt.getSrid());
        return g;
    }

    protected com.vividsolutions.jts.geom.LineString convertLineString(
            org.postgis.LineString lstr) {
        com.vividsolutions.jts.geom.LineString out = lstr.haveMeasure ? getGeometryFactory()
                .createMLineString(toJTSCoordinates(lstr.getPoints()))
                : getGeometryFactory().createLineString(
                toJTSCoordinates(lstr.getPoints()));
        out.setSRID(lstr.getSrid());
        return out;
    }

    private MCoordinate[] toJTSCoordinates(Point[] points) {
        MCoordinate[] coordinates = new MCoordinate[points.length];
        for (int i = 0; i < points.length; i++) {
            coordinates[i] = this.toJTSCoordinate(points[i]);
        }
        return coordinates;
    }

    private MCoordinate toJTSCoordinate(Point pt) {
        MCoordinate mc;
        if (pt.dimension == 2) {
            mc = pt.haveMeasure ? MCoordinate.create2dWithMeasure(pt.getX(), pt
                    .getY(), pt.getM()) : MCoordinate.create2d(pt.getX(), pt
                    .getY());
        } else {
            mc = pt.haveMeasure ? MCoordinate.create3dWithMeasure(pt.getX(), pt
                    .getY(), pt.getZ(), pt.getM()) : MCoordinate.create3d(pt
                    .getX(), pt.getY(), pt.getZ());
        }
        return mc;
    }

    private Point[] toPoints(Coordinate[] coordinates) {
        Point[] points = new Point[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate c = coordinates[i];
            Point pt;
            if (Double.isNaN(c.z)) {
                pt = new Point(c.x, c.y);
            } else {
                pt = new Point(c.x, c.y, c.z);
            }
            if (c instanceof MCoordinate) {
                MCoordinate mc = (MCoordinate) c;
                if (!Double.isNaN(mc.m)) {
                    pt.setM(mc.m);
                }
            }
            points[i] = pt;
        }
        return points;
    }

    /**
     * Converts a JTS <code>Geometry</code> to a native geometry object.
     *
     * @param jtsGeom    JTS Geometry to convert
     * @param connection the current database connection
     * @return native database geometry object corresponding to jtsGeom.
     */
    public Object conv2DBGeometry(Geometry jtsGeom, Connection connection) {
        org.postgis.Geometry geom = null;
        jtsGeom = forceEmptyToGeometryCollection(jtsGeom);
        if (jtsGeom instanceof com.vividsolutions.jts.geom.Point) {
            geom = convertJTSPoint((com.vividsolutions.jts.geom.Point) jtsGeom);
        } else if (jtsGeom instanceof com.vividsolutions.jts.geom.LineString) {
            geom = convertJTSLineString((com.vividsolutions.jts.geom.LineString) jtsGeom);
        } else if (jtsGeom instanceof com.vividsolutions.jts.geom.MultiLineString) {
            geom = convertJTSMultiLineString((com.vividsolutions.jts.geom.MultiLineString) jtsGeom);
        } else if (jtsGeom instanceof com.vividsolutions.jts.geom.Polygon) {
            geom = convertJTSPolygon((com.vividsolutions.jts.geom.Polygon) jtsGeom);
        } else if (jtsGeom instanceof com.vividsolutions.jts.geom.MultiPoint) {
            geom = convertJTSMultiPoint((com.vividsolutions.jts.geom.MultiPoint) jtsGeom);
        } else if (jtsGeom instanceof com.vividsolutions.jts.geom.MultiPolygon) {
            geom = convertJTSMultiPolygon((com.vividsolutions.jts.geom.MultiPolygon) jtsGeom);
        } else if (jtsGeom instanceof com.vividsolutions.jts.geom.GeometryCollection) {
            geom = convertJTSGeometryCollection((com.vividsolutions.jts.geom.GeometryCollection) jtsGeom);
        }

        if (geom != null)
            return new PGgeometry(geom);
        else
            throw new UnsupportedOperationException("Conversion of "
                    + jtsGeom.getClass().getSimpleName()
                    + " to PGgeometry not supported");
    }

    //Postgis treats every empty geometry as an empty geometrycollection

    private Geometry forceEmptyToGeometryCollection(Geometry jtsGeom) {
        Geometry forced = jtsGeom;
        if (forced.isEmpty()) {
            GeometryFactory factory = jtsGeom.getFactory();
            if (factory == null) {
                factory = HBSpatialExtension.getDefaultGeomFactory();
            }
            forced = factory.createGeometryCollection(null);
            forced.setSRID(jtsGeom.getSRID());
        }
        return forced;
    }

    private MultiPolygon convertJTSMultiPolygon(
            com.vividsolutions.jts.geom.MultiPolygon multiPolygon) {
        Polygon[] pgPolygons = new Polygon[multiPolygon.getNumGeometries()];
        for (int i = 0; i < pgPolygons.length; i++) {
            pgPolygons[i] = convertJTSPolygon((com.vividsolutions.jts.geom.Polygon) multiPolygon
                    .getGeometryN(i));
        }
        MultiPolygon mpg = new MultiPolygon(pgPolygons);
        mpg.setSrid(multiPolygon.getSRID());
        return mpg;
    }

    private MultiPoint convertJTSMultiPoint(
            com.vividsolutions.jts.geom.MultiPoint multiPoint) {
        Point[] pgPoints = new Point[multiPoint.getNumGeometries()];
        for (int i = 0; i < pgPoints.length; i++) {
            pgPoints[i] = convertJTSPoint((com.vividsolutions.jts.geom.Point) multiPoint
                    .getGeometryN(i));
        }
        MultiPoint mp = new MultiPoint(pgPoints);
        mp.setSrid(multiPoint.getSRID());
        return mp;
    }

    private Polygon convertJTSPolygon(
            com.vividsolutions.jts.geom.Polygon jtsPolygon) {
        int numRings = jtsPolygon.getNumInteriorRing();
        org.postgis.LinearRing[] rings = new org.postgis.LinearRing[numRings + 1];
        rings[0] = convertJTSLineStringToLinearRing(jtsPolygon
                .getExteriorRing());
        for (int i = 0; i < numRings; i++) {
            rings[i + 1] = convertJTSLineStringToLinearRing(jtsPolygon
                    .getInteriorRingN(i));
        }
        Polygon polygon = new org.postgis.Polygon(rings);
        polygon.setSrid(jtsPolygon.getSRID());
        return polygon;
    }

    private LinearRing convertJTSLineStringToLinearRing(
            com.vividsolutions.jts.geom.LineString lineString) {
        LinearRing lr = new org.postgis.LinearRing(toPoints(lineString
                .getCoordinates()));
        lr.setSrid(lineString.getSRID());
        return lr;
    }

    private LineString convertJTSLineString(
            com.vividsolutions.jts.geom.LineString string) {
        LineString ls = new org.postgis.LineString(toPoints(string
                .getCoordinates()));
        if (string instanceof MGeometry) {
            ls.haveMeasure = true;
        }
        ls.setSrid(string.getSRID());
        return ls;
    }

    private MultiLineString convertJTSMultiLineString(
            com.vividsolutions.jts.geom.MultiLineString string) {
        org.postgis.LineString[] lines = new org.postgis.LineString[string
                .getNumGeometries()];
        for (int i = 0; i < string.getNumGeometries(); i++) {
            lines[i] = new org.postgis.LineString(toPoints(string.getGeometryN(
                    i).getCoordinates()));
        }
        MultiLineString mls = new MultiLineString(lines);
        if (string instanceof MGeometry) {
            mls.haveMeasure = true;
        }
        mls.setSrid(string.getSRID());
        return mls;
    }

    private Point convertJTSPoint(com.vividsolutions.jts.geom.Point point) {
        org.postgis.Point pgPoint = new org.postgis.Point();
        pgPoint.srid = point.getSRID();
        pgPoint.x = point.getX();
        pgPoint.y = point.getY();
        if (new Double(point.getCoordinate().z).isNaN()) {
            pgPoint.dimension = 2;
        } else {
            pgPoint.z = point.getCoordinate().z;
            pgPoint.dimension = 3;
        }
        pgPoint.haveMeasure = false;
        return pgPoint;
    }

    private GeometryCollection convertJTSGeometryCollection(
            com.vividsolutions.jts.geom.GeometryCollection collection) {
        com.vividsolutions.jts.geom.Geometry currentGeom;
        org.postgis.Geometry[] pgCollections = new org.postgis.Geometry[collection
                .getNumGeometries()];
        for (int i = 0; i < pgCollections.length; i++) {
            currentGeom = collection.getGeometryN(i);
            currentGeom = forceEmptyToGeometryCollection(currentGeom);
            if (currentGeom.getClass() == com.vividsolutions.jts.geom.LineString.class) {
                pgCollections[i] = convertJTSLineString((com.vividsolutions.jts.geom.LineString) currentGeom);
            } else if (currentGeom.getClass() == com.vividsolutions.jts.geom.LinearRing.class) {
                pgCollections[i] = convertJTSLineStringToLinearRing((com.vividsolutions.jts.geom.LinearRing) currentGeom);
            } else if (currentGeom.getClass() == com.vividsolutions.jts.geom.MultiLineString.class) {
                pgCollections[i] = convertJTSMultiLineString((com.vividsolutions.jts.geom.MultiLineString) currentGeom);
            } else if (currentGeom.getClass() == com.vividsolutions.jts.geom.MultiPoint.class) {
                pgCollections[i] = convertJTSMultiPoint((com.vividsolutions.jts.geom.MultiPoint) currentGeom);
            } else if (currentGeom.getClass() == com.vividsolutions.jts.geom.MultiPolygon.class) {
                pgCollections[i] = convertJTSMultiPolygon((com.vividsolutions.jts.geom.MultiPolygon) currentGeom);
            } else if (currentGeom.getClass() == com.vividsolutions.jts.geom.Point.class) {
                pgCollections[i] = convertJTSPoint((com.vividsolutions.jts.geom.Point) currentGeom);
            } else if (currentGeom.getClass() == com.vividsolutions.jts.geom.Polygon.class) {
                pgCollections[i] = convertJTSPolygon((com.vividsolutions.jts.geom.Polygon) currentGeom);
            } else if (currentGeom.getClass() == com.vividsolutions.jts.geom.GeometryCollection.class) {
                pgCollections[i] = convertJTSGeometryCollection((com.vividsolutions.jts.geom.GeometryCollection) currentGeom);
            }
        }
        GeometryCollection gc = new GeometryCollection(pgCollections);
        gc.setSrid(collection.getSRID());
        return gc;
    }

}
