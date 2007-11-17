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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import oracle.jdbc.driver.OracleConnection;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.Datum;
import oracle.sql.NUMBER;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

import org.hibernate.HibernateException;
import org.hibernatespatial.AbstractDBGeometryType;
import org.hibernatespatial.HibernateSpatialException;
import org.hibernatespatial.Circle;
import org.hibernatespatial.mgeom.MCoordinate;
import org.hibernatespatial.mgeom.MGeometryFactory;
import org.hibernatespatial.mgeom.MLineString;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Implements Oracle 9i/10g specific geometrytype.
 * 
 * @author Karel Maesen
 */
public class SDOGeometryType extends AbstractDBGeometryType {

	private static final int[] geometryTypes = new int[] { Types.STRUCT };

	// TODO -- allow user to set a specific PrecisionModel
	private static final MGeometryFactory geomFactory = new MGeometryFactory();

	private static String SQL_TYPE_NAME = "SDO_GEOMETRY";

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, sqlTypes()[0], SQL_TYPE_NAME);
		} else {
			Geometry jtsGeom = (Geometry) value;
			Object dbGeom = conv2DBGeometry(jtsGeom, findOracleConnection(st
					.getConnection()));
			st.setObject(index, dbGeom);
		}
	}

	@Override
	public Object conv2DBGeometry(Geometry jtsGeom, Connection connection) {
		// note: we assume that there is no LRS used in jtsGeom.
		SDO_GEOMETRY geom = null;
		if (jtsGeom.getClass() == Point.class) {
			geom = convertJTSPoint((Point) jtsGeom);
		} else if (jtsGeom.getClass() == LineString.class
				|| jtsGeom.getClass() == MLineString.class) {
			geom = convertJTSLineString((LineString) jtsGeom);
		} else if (jtsGeom.getClass() == Polygon.class) {
			geom = convertJTSPolygon((Polygon) jtsGeom);
		} else if (jtsGeom.getClass() == MultiPoint.class) {
			geom = convertJTSMultiPoint((MultiPoint) jtsGeom);
		} else if (jtsGeom.getClass() == MultiLineString.class) {
			geom = convertJTSMultiLineString((MultiLineString) jtsGeom);
		} else if (jtsGeom.getClass() == MultiPolygon.class) {
			geom = convertJTSMultiPolygon((MultiPolygon) jtsGeom);
		}

		if (geom != null)
			try {
				return SDO_GEOMETRY.store(geom, (OracleConnection) connection);
			} catch (SQLException e) {
				throw new HibernateSpatialException(
						"Problem during conversion from JTS to JGeometry", e);
			}
		else {
			throw new UnsupportedOperationException("Conversion of "
					+ jtsGeom.getClass().getSimpleName()
					+ " to Oracle STRUCT not supported");
		}
	}

	/**
	 * 
	 * This method is necessary because in some environments, the
	 * OracleConnection is wrapped into some other Connection object (e.g. in
	 * the JBoss Application Server).
	 * 
	 * Thanks to Martin Steinweder for bringing this to my attention.
	 * 
	 * TODO : make this configurable, in the sense that a
	 * OracleConnectionResolver object could be passed that determines the
	 * OracleConnection based on the environment.
	 * 
	 * @param con
	 * @return
	 */
	private OracleConnection findOracleConnection(Connection con) {
		if (con == null) {
			return null;
		}
		if (con instanceof OracleConnection) {
			return (OracleConnection) con;
		}
		// try to find the Oracleconnection recursively

		for (Method method : con.getClass().getMethods()) {
			if (method.getReturnType().isAssignableFrom(
					java.sql.Connection.class)
					&& method.getParameterTypes().length == 0) {
				try {
					return findOracleConnection((java.sql.Connection) (method
							.invoke(con, new Object[] {})));
				} catch (Exception e) {
					// Shouldm't ever happen.
				}
			}
		}
		throw new HibernateSpatialException(
				"Couldn't get at the OracleSpatial Connection object from the PreparedStatement.");
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
		Double[] ordinates = new Double[] {};
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
		int ordinatesOffset = 1;
		if (geom.getOrdinates() != null) {
			ordinatesOffset = geom.getOrdinates().getOrdinateArray().length + 1;
		}
		Double[] ordinates = new Double[] {};
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
			ordinatesOffset = ordinates.length + 1;
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
		Double[] ordinates = new Double[] {};
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
	 * @param coordinates
	 *            Coordinates to be converted to the array
	 * @param dim
	 *            Coordinate dimension
	 * @param isLrs
	 *            true if the coordinates contain measures
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
	 * @param d
	 *            the value to be converted
	 * @return A Double instance of d, Null if the parameter is Double.NaN
	 */
	private Double toDouble(double d) {
		return Double.isNaN(d) ? null : d;
	}


	/**
	 * Return the dimension required for building the gType in the SDO_GEOMETRY
	 * object. Has support for LRS type geometries.
	 * 
	 * @param geom
	 *            and instance of the Geometry class from which the dimension is
	 *            being extracted.
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
	 * @param geom
	 *            and instance of the Geometry class from which the lrs position
	 *            is being extracted.
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

	// private Object[] convertGeometryToCoordArrays(Geometry geom) {
	//
	// if (geom.getClass() == MultiPoint.class
	// || geom.getClass() == MultiLineString.class) {
	// Object[] arr = new Object[geom.getNumGeometries()];
	// for (int i = 0; i < arr.length; i++) {
	// arr[i] = geom.getGeometryN(i).getCoordinates();
	// }
	// return arr;
	// } else if (geom.getClass() == Polygon.class) {
	// Polygon poly = (Polygon) geom;
	// Object[] arr = new Object[poly.getNumInteriorRing() + 1];
	// Coordinate[] outer = poly.getExteriorRing().getCoordinates();
	// // For Oracle Spatial, outer ring must be counter clockwise
	// if (!CGAlgorithms.isCCW(outer))
	// arr[0] = reverseRing(outer);
	// else
	// arr[0] = outer;
	//
	// for (int i = 0; i < poly.getNumInteriorRing(); i++) {
	// Coordinate[] inner = poly.getInteriorRingN(i).getCoordinates();
	// if (CGAlgorithms.isCCW(inner))
	// arr[i + 1] = reverseRing(inner);
	// else
	// arr[i + 1] = inner;
	//
	// }
	// return arr;
	// } else
	// throw new IllegalArgumentException(
	// "geom must of of type MultiPoint, MultiLineString or Polygon");
	// }

	@Override
	public Geometry convert2JTS(Object struct) {
		if (struct == null) {
			return null;
		}

		SDO_GEOMETRY sdoGeom = SDO_GEOMETRY.load((STRUCT) struct);
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
		default:
			throw new IllegalArgumentException("Type not supported: "
					+ sdoGeom.getGType().getTypeGeometry());
		}

	}

	private Point convertSDOPoint(SDO_GEOMETRY sdoGeom) {
		Double[] ordinates = sdoGeom.getOrdinates().getOrdinateArray();
		if (ordinates.length == 0) {
			if (sdoGeom.getDimension() == 2) {
				ordinates = new Double[] { sdoGeom.getPoint().x,
						sdoGeom.getPoint().y };
			} else {
				ordinates = new Double[] { sdoGeom.getPoint().y,
						sdoGeom.getPoint().y, sdoGeom.getPoint().z };
			}
		}
		CoordinateSequence cs = convertOrdinateArray(ordinates, sdoGeom);
		Point point = geomFactory.createPoint(cs);

		point.setSRID(sdoGeom.getSRID());
		return point;
	}

	private MultiPoint convertSDOMultiPoint(int dim, int lrsDim,
			SDO_GEOMETRY sdoGeom) {
		Double[] ordinates = sdoGeom.getOrdinates().getOrdinateArray();
		CoordinateSequence cs = convertOrdinateArray(ordinates, sdoGeom);
		MultiPoint multipoint = geomFactory.createMultiPoint(cs);
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

		LineString ls = lrs ? geomFactory.createMLineString(cs) : geomFactory
				.createLineString(cs);
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
				LineString line = lrs ? geomFactory.createMLineString(cs)
						: geomFactory.createLineString(cs);
				lines[i] = line;
				i += 1 + numCompounds;
			} else {
				cs = add(cs, getElementCSeq(i, sdoGeom, false));
				LineString line = lrs ? geomFactory.createMLineString(cs)
						: geomFactory.createLineString(cs);
				lines[i] = line;
				i++;
			}
		}

		MultiLineString mls = lrs ? geomFactory
				.createMultiMLineString((MLineString[]) lines) : geomFactory
				.createMultiLineString(lines);
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
				holes[idxInteriorRings] = geomFactory.createLinearRing(cs);
				holes[idxInteriorRings].setSRID(sdoGeom.getSRID());
				idxInteriorRings++;
			} else {
				shell = geomFactory.createLinearRing(cs);
				shell.setSRID(sdoGeom.getSRID());
			}
			i += 1 + numCompounds;
		}
		Polygon polygon = geomFactory.createPolygon(shell, holes);
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
				LinearRing lr = geomFactory.createLinearRing(cs);
				lr.setSRID(sdoGeom.getSRID());
				holes.add(lr);
			} else {
				if (shell != null) {
					Polygon polygon = geomFactory.createPolygon(shell, holes
							.toArray(new LinearRing[holes.size()]));
					polygon.setSRID(sdoGeom.getSRID());
					polygons.add(polygon);
					shell = null;
				}
				shell = geomFactory.createLinearRing(cs);
				shell.setSRID(sdoGeom.getSRID());
				holes = new ArrayList<LinearRing>();
			}
			i += 1 + numCompounds;
		}
		if (shell != null) {
			Polygon polygon = geomFactory.createPolygon(shell, holes
					.toArray(new LinearRing[holes.size()]));
			polygon.setSRID(sdoGeom.getSRID());
			polygons.add(polygon);
		}
		MultiPolygon multiPolygon = geomFactory.createMultiPolygon(polygons
				.toArray(new Polygon[polygons.size()]));
		multiPolygon.setSRID(sdoGeom.getSRID());
		return multiPolygon;
	}

	/**
	 * Gets the CoordinateSequence corresponding to a compound element.
	 * 
	 * @param idxFirst
	 *            the first sub-element of the compound element
	 * @param idxLast
	 *            the last sub-element of the compound element
	 * @param sdoGeom
	 *            the SDO_GEOMETRY that holds the compound element.
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
				cs = geomFactory.getCoordinateSequenceFactory().create(
						newCoordinates);
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
	private CoordinateSequence getElementCSeq(int i, SDO_GEOMETRY sdoGeom, boolean hasNextSE) {
		ElementType type = sdoGeom.getInfo().getElementType(i);
		Double[] elemOrdinates = extractOrdinatesOfElement(i, sdoGeom, hasNextSE);
		CoordinateSequence cs = null;
		if (type.isStraightSegment()) {
			cs = convertOrdinateArray(elemOrdinates, sdoGeom);
		} else if (type.isArcSegment() || type.isCircle()) {
			// remember that the last point of a subelement is the first point
			// of the next subelement.
			// throw new UnsupportedOperationException("Serialization of arc
			// segments not yet implemented");
			Coordinate[] linearized = linearize(elemOrdinates, sdoGeom
					.getDimension(), sdoGeom.isLRSGeometry(), type.isCircle());
			cs = geomFactory.getCoordinateSequenceFactory().create(linearized);
		} else if (type.isRect()) {
			cs = convertOrdinateArray(elemOrdinates, sdoGeom);
			Coordinate ll = cs.getCoordinate(0);
			Coordinate ur = cs.getCoordinate(1);
			Coordinate lr = new Coordinate(ur.x, ll.y);
			Coordinate ul = new Coordinate(ll.x, ur.y);
			if (type.isExteriorRing()) {
				cs = geomFactory.getCoordinateSequenceFactory().create(
						new Coordinate[] { ll, lr, ur, ul, ll });
			} else {
				cs = geomFactory.getCoordinateSequenceFactory().create(
						new Coordinate[] { ll, ul, ur, lr, ll });
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
		return geomFactory.getCoordinateSequenceFactory().create(c3);
	}

	private Double[] extractOrdinatesOfElement(int element, SDO_GEOMETRY sdoGeom, boolean hasNextSE) {
		int start = sdoGeom.getInfo().getOordinatesOffset(element);
		if (element < sdoGeom.getInfo().getSize() - 1) {
			int end = sdoGeom.getInfo().getOordinatesOffset(element + 1);
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

	// public Geometry convert2JTS(Object geomObj) {
	// if (geomObj == null)
	// return null;
	//
	// JGeometry jgeom;
	// try {
	// jgeom = JGeometry.load((STRUCT) geomObj);
	// } catch (SQLException e) {
	// throw new HibernateSpatialException(
	// "Error loading Oracle Spatial Geometry Object", e);
	// }
	//
	// if (jgeom.getDimensions() > 4)
	// throw new UnsupportedOperationException(
	// "Cannot handle LRS Geometries, or geometries with more than 4
	// dimensions");
	//
	// Geometry jtsGeom = null;
	// if (jgeom.getType() == JGeometry.GTYPE_POINT) {
	// double[] ordinates = jgeom.getOrdinatesArray();
	// if (ordinates == null)
	// ordinates = jgeom.getPoint();
	// CoordinateSequence cs = convertOrdinateArray(jgeom
	// .getOrdinatesArray(), jgeom.getDimensions(), jgeom.isLRSGeometry());
	// jtsGeom = geomFactory.createPoint(cs);
	// jtsGeom.setSRID(jgeom.getSRID());
	// } else if (jgeom.getType() == JGeometry.GTYPE_MULTIPOINT) {
	// CoordinateSequence cs = convertOrdinateArray(jgeom
	// .getOrdinatesArray(), jgeom.getDimensions(), jgeom.isLRSGeometry());
	// jtsGeom = geomFactory.createMultiPoint(cs);
	// jtsGeom.setSRID(jgeom.getSRID());
	// } else if (jgeom.getType() == JGeometry.GTYPE_CURVE) {
	// Element elem = new Element(jgeom.isLRSGeometry());
	// elem.parse(jgeom.getOrdinatesArray(), jgeom.getElemInfo(),
	// jgeom.getDimensions());
	// jtsGeom = createLineString(elem, jgeom.getDimensions(), jgeom.getSRID(),
	// jgeom.isLRSGeometry());
	//
	// } else if (jgeom.getType() == JGeometry.GTYPE_MULTICURVE) {
	// Element elem = new Element();
	// elem.parse(jgeom.getOrdinatesArray(), jgeom.getElemInfo(), jgeom
	// .getDimensions());
	// LineString[] linestrings = new LineString[elem.getNumElems()];
	// for (int i = 0; elem != null; elem = elem.getNext(), i++) {
	// linestrings[i] = createLineString(elem, jgeom.getDimensions(),
	// jgeom.getSRID(), jgeom.isLRSGeometry());
	// }
	// jtsGeom = geomFactory.createMultiLineString(linestrings);
	// jtsGeom.setSRID(jgeom.getSRID());
	// } else if (jgeom.getType() == JGeometry.GTYPE_POLYGON) {
	// Element elem = new Element();
	// elem.parse(jgeom.getOrdinatesArray(), jgeom.getElemInfo(), jgeom
	// .getDimensions());
	// jtsGeom = createPolygon(elem, jgeom.getDimensions(), jgeom
	// .getSRID());
	// } else if (jgeom.getType() == JGeometry.GTYPE_MULTIPOLYGON) {
	// Element elem = new Element();
	// elem.parse(jgeom.getOrdinatesArray(), jgeom.getElemInfo(), jgeom
	// .getDimensions());
	// Element[] polyElems = elem.toPolyElems();
	// Polygon[] polygons = new Polygon[polyElems.length];
	// for (int i = 0; i < polyElems.length; i++) {
	// polygons[i] = createPolygon(polyElems[i], jgeom
	// .getDimensions(), jgeom.getSRID());
	// }
	// jtsGeom = geomFactory.createMultiPolygon(polygons);
	// jtsGeom.setSRID(jgeom.getSRID());
	// } else {
	// throw new IllegalArgumentException("Unsupported JGeometry type");
	// }
	//
	// return jtsGeom;
	// }

	// private Polygon createPolygon(Element elem, int dimensions, int srid) {
	// LinearRing shell = null;
	// LinearRing[] holes = null;
	// int idxInteriorRings = 0;
	// LinearRing[] rings = new LinearRing[elem.getNumElems()];
	// for (int i = 0; i < rings.length; i++) {
	// if (elem.getElementType() == ElementType.INTERIOR_RING_ARC_SEGMENTS
	// || elem.getElementType() == ElementType.INTERIOR_RING_CIRCLE
	// || elem.getElementType() == ElementType.INTERIOR_RING_RECT
	// || elem.getElementType() == ElementType.INTERIOR_RING_STRAIGHT_SEGMENTS)
	// {
	// rings[idxInteriorRings] = createRing(elem, dimensions);
	// rings[idxInteriorRings].setSRID(srid);
	// idxInteriorRings++;
	// } else {
	// if (shell != null)
	// break;
	// shell = createRing(elem, dimensions);
	// shell.setSRID(srid);
	// }
	// elem = elem.getNext();
	// }
	// holes = new LinearRing[idxInteriorRings];
	// System.arraycopy(rings, 0, holes, 0, idxInteriorRings);
	//
	// Polygon polygon = geomFactory.createPolygon(shell, holes);
	// polygon.setSRID(srid);
	// return polygon;
	// }

	// private LinearRing createRing(int i, SDO_GEOMETRY sdoGeom) {
	// CoordinateSequence cs = null ;
	// cs = getElementCSeq(i,null,sdoGeom);
	// if (sdoGeom.getInfo().getElementType(i).isRect()) {
	// Coordinate ll = cs.getCoordinate(0);
	// Coordinate ur = cs.getCoordinate(1);
	// Coordinate lr = new Coordinate(ur.x, ll.y);
	// Coordinate ul = new Coordinate(ll.x, ur.y);
	// cs = geomFactory.getCoordinateSequenceFactory().create(
	// new Coordinate[]{ll, lr, ur, ul, ll});
	// }
	// LinearRing ring = geomFactory.createLinearRing(cs);
	// return ring;
	// }

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
		return geomFactory.getCoordinateSequenceFactory().create(coordinates);
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
	 * @param arcOrdinates
	 *            arc or circle coordinates
	 * @param dim
	 *            coordinate dimension
	 * @param lrs
	 *            whether this is an lrs geometry
	 * @param entireCirlce
	 *            whether the whole arc should be linearized
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
		if (array == null || Array.getLength(array) == 0) {
			return "()";
		}
		int length = Array.getLength(array);
		StringBuilder stb = new StringBuilder();
		stb.append("(").append(Array.get(array, 0));
		for (int i = 1; i < length; i++) {
			stb.append(",").append(Array.get(array, i));
		}
		stb.append(")");
		return stb.toString();
	}

	// public static String arrayToString(int[] ds){
	// if (ds != null || ds.length == 0 ){
	// return "";
	// }
	// StringBuilder stb = new StringBuilder();
	// stb.append(ds[0]);
	// for ( int i = 1; i < ds.length; i++){
	// stb.append(",").append(ds[i]);
	// }
	// stb.append(")");
	// return stb.toString();
	// }

	public enum TypeGeometry {

		UNKNOWN_GEOMETRY(0), POINT(1), LINE(2), POLYGON(3), COLLECTION(4), MULTIPOINT(
				5), MULTILINE(6), MULTIPOLYGON(7);

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

		public static SDO_GTYPE parse(Datum datum) {

			try {
				int v = ((NUMBER) datum).intValue();
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

		public SDO_POINT(STRUCT struct) {
			try {
				Datum[] data = struct.getOracleAttributes();
				this.x = ((NUMBER) data[0]).doubleValue();
				this.y = ((NUMBER) data[1]).doubleValue();
				if (data[2] != null) {
					this.z = ((NUMBER) data[1]).doubleValue();
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

	public static class ELEM_INFO {

		private static final String TYPE_NAME = "MDSYS.SDO_ELEM_INFO_ARRAY";

		private static ArrayDescriptor arrayDescriptor = null;

		private int[] triplets;

		public ELEM_INFO(int size) {
			this.triplets = new int[3 * size];
		}

		public ELEM_INFO(int[] elem_info) {
			this.triplets = elem_info;
		}

		public ELEM_INFO(ARRAY array) {
			if (array == null) {
				this.triplets = new int[] {};
				return;
			}
			try {
				triplets = array.getIntArray();
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

		public int getOordinatesOffset(int i) {
			return this.triplets[i * 3];
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

		public ARRAY toOracleArray(OracleConnection conn) throws SQLException {

			if (arrayDescriptor == null) {
				arrayDescriptor = ArrayDescriptor.createDescriptor(TYPE_NAME,
						conn);
			} else {
				arrayDescriptor.setConnection(conn);
			}

			return new ARRAY(arrayDescriptor, conn, this.triplets);
		}
	}

	public static class ORDINATES {

		private static final String TYPE_NAME = "MDSYS.SDO_ORDINATE_ARRAY";

		private static ArrayDescriptor arrayDescriptor = null;

		private Double[] ordinates;

		public ORDINATES(Double[] ordinates) {
			this.ordinates = ordinates;
		}

		public ORDINATES(ARRAY array) {
			if (array == null) {
				this.ordinates = new Double[] {};
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

		public ARRAY toOracleArray(OracleConnection conn) throws SQLException {
			if (arrayDescriptor == null) {
				arrayDescriptor = ArrayDescriptor.createDescriptor(TYPE_NAME,
						conn);
			} else {
				arrayDescriptor.setConnection(conn);
			}
			return new ARRAY(arrayDescriptor, conn, this.ordinates);
		}

	}

	public static class SDO_GEOMETRY {

		private static final String TYPE_NAME = "MDSYS.SDO_GEOMETRY";

		private static StructDescriptor structDescriptor = null;

		private SDO_GTYPE gtype;

		private Integer srid;

		private SDO_POINT point;

		private ELEM_INFO info;

		private ORDINATES ordinates;

		public SDO_GEOMETRY() {

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

		public static SDO_GEOMETRY load(STRUCT struct) {

			Datum[] data;
			try {
				data = struct.getOracleAttributes();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}

			SDO_GEOMETRY geom = new SDO_GEOMETRY();
			geom.setGType(SDO_GTYPE.parse(data[0]));
			geom.setSRID(data[1]);
			if (data[2] != null) {
				geom.setPoint(new SDO_POINT((STRUCT) data[2]));
			}
			geom.setInfo(new ELEM_INFO((ARRAY) data[3]));
			geom.setOrdinates(new ORDINATES((ARRAY) data[4]));

			return geom;
		}

		public static STRUCT store(SDO_GEOMETRY geom, OracleConnection conn)
				throws SQLException {

			if (structDescriptor == null) {
				structDescriptor = StructDescriptor.createDescriptor(TYPE_NAME,
						conn);
			} else {
				structDescriptor.setConnection(conn);
			}
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

		private void setSRID(Datum datum) {
			if (datum == null) {
				this.srid = 0;
				return;
			}
			try {
				this.srid = new Integer(((NUMBER) datum).intValue());
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
		 * 
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

	}

}
