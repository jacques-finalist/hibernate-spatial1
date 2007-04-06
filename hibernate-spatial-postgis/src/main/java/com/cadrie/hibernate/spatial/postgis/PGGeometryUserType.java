/**
 * $Id: PGGeometryUserType.java 138 2007-03-13 18:13:06Z maesenka $
 *
 * This file is part of MAJAS (Mapping with Asynchronous JavaScript and ASVG). a
 * framework for Rich Internet GIS Applications.
 *
 * Copyright  @ 2007 DFC Software Engineering, Belgium
 * and K.U. Leuven LRD, Spatial Applications Division, Belgium
 *
 * MAJAS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MAJAS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with gGIS; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.cadrie.hibernate.spatial.postgis;

import java.sql.Connection;
import java.sql.Types;

import org.postgis.GeometryCollection;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

import com.cadrie.hibernate.spatial.AbstractDBGeometryType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class PGGeometryUserType extends AbstractDBGeometryType {

    private static final int[] geometryTypes = new int[] { Types.STRUCT };

    private static final GeometryFactory geomFactory = new GeometryFactory();

 

    public int[] sqlTypes() {
	return geometryTypes;// PostgisDialect.getGeometrySQLType()
    }

    /**
         * Converts the native geometry object to a JTS <code>Geometry</code>.
         * 
         * @param geomObj
         *                native database geometry object (depends on the JDBC
         *                spatial extension of the database)
         * @return JTS geometry corresponding to geomObj.
         */
    public Geometry convert2JTS(Object object) {
	if (object == null)
	    return null;
	PGgeometry geom = (PGgeometry) object;
	com.vividsolutions.jts.geom.Geometry out = null;
	switch (geom.getGeoType()) {
	case org.postgis.Geometry.POINT:
	    out = convertPoint((org.postgis.Point) geom.getGeometry());
	    break;
	case org.postgis.Geometry.LINESTRING:
	    out = convertLineString((org.postgis.LineString) geom.getGeometry());
	    break;
	case org.postgis.Geometry.POLYGON:
	    out = convertPolygon((org.postgis.Polygon) geom.getGeometry());
	    break;
	case org.postgis.Geometry.MULTILINESTRING:
	    out = convertMultiLineString((org.postgis.MultiLineString) geom
		    .getGeometry());
	    break;
	case org.postgis.Geometry.MULTIPOINT:
	    out = convertMultiPoint((org.postgis.MultiPoint) geom.getGeometry());
	    break;
	case org.postgis.Geometry.MULTIPOLYGON:
	    out = convertMultiPolygon((org.postgis.MultiPolygon) geom
		    .getGeometry());
	    break;
	case org.postgis.Geometry.GEOMETRYCOLLECTION:
	    out = convertGeometryCollection((org.postgis.GeometryCollection) geom.getGeometry());
	}

	return out;
    }
    
    

    private Geometry convertGeometryCollection(GeometryCollection collection) {
	org.postgis.Geometry[] geometries = collection.getGeometries();
	com.vividsolutions.jts.geom.Geometry[] jtsGeometries = new com.vividsolutions.jts.geom.GeometryCollection[geometries.length];
	for (int i = 0; i < geometries.length; i++){
	    jtsGeometries[i] = convert2JTS(geometries[i]);
	}
	com.vividsolutions.jts.geom.GeometryCollection jtsGCollection = geomFactory.createGeometryCollection(jtsGeometries); 
	return jtsGCollection;
    }

    private Geometry convertMultiPolygon(MultiPolygon pgMultiPolygon) {
	com.vividsolutions.jts.geom.Polygon[] polygons = new com.vividsolutions.jts.geom.Polygon[pgMultiPolygon
		.numPolygons()];

	for (int i = 0; i < polygons.length; i++) {
	    Polygon pgPolygon = pgMultiPolygon.getPolygon(i);
	    polygons[i] = (com.vividsolutions.jts.geom.Polygon) convertPolygon(pgPolygon);
	}

	com.vividsolutions.jts.geom.MultiPolygon out = geomFactory
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
	com.vividsolutions.jts.geom.MultiPoint out = geomFactory
		.createMultiPoint(points);
	out.setSRID(pgMultiPoint.srid);
	return out;
    }

    private com.vividsolutions.jts.geom.Geometry convertMultiLineString(
	    MultiLineString mlstr) {

	com.vividsolutions.jts.geom.LineString[] lstrs = new com.vividsolutions.jts.geom.LineString[mlstr
		.numLines()];

	for (int i = 0; i < mlstr.numLines(); i++) {
	    lstrs[i] = geomFactory.createLineString(toJTSCoordinates(mlstr
		    .getLine(i).getPoints()));
	}
	com.vividsolutions.jts.geom.MultiLineString out = geomFactory
		.createMultiLineString(lstrs);
	out.setSRID(mlstr.srid);
	return out;
    }

    protected com.vividsolutions.jts.geom.Geometry convertPolygon(
	    Polygon polygon) {
	com.vividsolutions.jts.geom.LinearRing shell = geomFactory
		.createLinearRing(toJTSCoordinates(polygon.getRing(0)
			.getPoints()));
	com.vividsolutions.jts.geom.Polygon out = null;
	if (polygon.numRings() > 1) {
	    com.vividsolutions.jts.geom.LinearRing[] rings = new com.vividsolutions.jts.geom.LinearRing[polygon
		    .numRings() - 1];
	    for (int r = 1; r < polygon.numRings(); r++) {
		rings[r-1] = geomFactory
			.createLinearRing(toJTSCoordinates(polygon.getRing(r)
				.getPoints()));
	    }
	    out = geomFactory.createPolygon(shell, rings);
	} else {
	    out = geomFactory.createPolygon(shell, null);
	}
	out.setSRID(polygon.srid);
	return out;
    }

    protected com.vividsolutions.jts.geom.Point convertPoint(Point pnt) {
	com.vividsolutions.jts.geom.Point g = geomFactory
		.createPoint(new Coordinate(pnt.x, pnt.y));
	g.setSRID(pnt.getSrid());
	return g;
    }

    protected com.vividsolutions.jts.geom.LineString convertLineString(
	    org.postgis.LineString lstr) {
	com.vividsolutions.jts.geom.LineString out = geomFactory
		.createLineString(toJTSCoordinates(lstr.getPoints()));
	out.setSRID(lstr.getSrid());
	return out;
    }

    private com.vividsolutions.jts.geom.Coordinate[] toJTSCoordinates(
	    Point[] points) {
	Coordinate[] coordinates = new Coordinate[points.length];
	for (int i = 0; i < points.length; i++) {
	    coordinates[i] = new Coordinate(points[i].x, points[i].y);
	}
	return coordinates;
    }

    private Point[] toPoints(Coordinate[] coordinates) {
	Point[] points = new Point[coordinates.length];
	for (int i = 0; i < coordinates.length; i++) {
	    points[i] = new Point(coordinates[i].x, coordinates[i].y);
	}
	return points;
    }

    /**
         * Converts a JTS <code>Geometry</code> to a native geometry object.
         * 
         * @param jtsGeom
         *                JTS Geometry to convert
         * @param connection
         *                the current database connection
         * @return native database geometry object corresponding to jtsGeom.
         */
    public Object conv2DBGeometry(Geometry jtsGeom, Connection connection) {
	org.postgis.Geometry geom = null;
	if (jtsGeom.getClass() == com.vividsolutions.jts.geom.Point.class) {
	    geom = convertJTSPoint((com.vividsolutions.jts.geom.Point) jtsGeom);
	} else if (jtsGeom.getClass() == com.vividsolutions.jts.geom.LineString.class) {
	    geom = convertJTSLineString((com.vividsolutions.jts.geom.LineString) jtsGeom);
	} else if (jtsGeom.getClass() == com.vividsolutions.jts.geom.MultiLineString.class) {
	    geom = convertJTSMultiLineSTring((com.vividsolutions.jts.geom.MultiLineString) jtsGeom);
	} else if (jtsGeom.getClass() == com.vividsolutions.jts.geom.Polygon.class) {
	    geom = convertJTSPolygon((com.vividsolutions.jts.geom.Polygon) jtsGeom);
	} else if (jtsGeom.getClass() == com.vividsolutions.jts.geom.MultiPoint.class) {
	    geom = convertJTSMultiPoint((com.vividsolutions.jts.geom.MultiPoint) jtsGeom);
	} else if (jtsGeom.getClass() == com.vividsolutions.jts.geom.MultiPolygon.class) {
	    geom = convertJTSMultiPolygon((com.vividsolutions.jts.geom.MultiPolygon) jtsGeom);
	}

	if (geom != null)
	    return new PGgeometry(geom);
	else
	    throw new UnsupportedOperationException("Conversion of "
		    + jtsGeom.getClass().getSimpleName()
		    + " to PGgeometry not supported");
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
	ls.setSrid(string.getSRID());
	return ls;
    }

    private MultiLineString convertJTSMultiLineSTring(
	    com.vividsolutions.jts.geom.MultiLineString string) {
	org.postgis.LineString[] lines = new org.postgis.LineString[string
		.getNumGeometries()];
	for (int i = 0; i < string.getNumGeometries(); i++) {
	    lines[i] = new org.postgis.LineString(toPoints(string.getGeometryN(
		    i).getCoordinates()));
	}
	MultiLineString mls = new MultiLineString(lines);
	mls.setSrid(string.getSRID());
	return mls;
    }

    private Point convertJTSPoint(com.vividsolutions.jts.geom.Point point) {
	org.postgis.Point pgPoint = new org.postgis.Point();
	pgPoint.srid = point.getSRID();
	pgPoint.x = point.getX();
	pgPoint.y = point.getY();
	pgPoint.haveMeasure = false;
	pgPoint.dimension = 2;
	return pgPoint;
    }

}