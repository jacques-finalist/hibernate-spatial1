/**
 * $Id$
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
package com.cadrie.hibernate.spatial.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.hibernate.HibernateException;

import com.cadrie.hibernate.spatial.AbstractDBGeometryType;
import com.cadrie.hibernate.spatial.HibernateSpatialException;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Karel Maesen
 * 
 * Implements Oracle 9i/10g specific geometrytype.
 * 
 */
public class SDOGeometryType extends AbstractDBGeometryType {

    private static final int[] geometryTypes = new int[] { Types.STRUCT };

    private static final GeometryFactory geomFactory = new GeometryFactory();

    private static String SQL_TYPE_NAME = "SDO_GEOMETRY";
    
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index)
    throws HibernateException, SQLException {
	if (value == null) {
	    st.setNull(index, sqlTypes()[0],SQL_TYPE_NAME);
	} else {
	    Geometry jtsGeom = (Geometry) value;
	    Object dbGeom = conv2DBGeometry(jtsGeom, st.getConnection());
	    st.setObject(index, dbGeom);
	}
    }
    
    @Override
    public Object conv2DBGeometry(Geometry jtsGeom, Connection connection) {
	// note: we assume that there is no LRS used in jtsGeom.
	JGeometry geom = null;
	if (jtsGeom.getClass() == Point.class) {
	    geom = convertJTSPoint((Point) jtsGeom);
	} else if (jtsGeom.getClass() == MultiPoint.class) {
	    geom = convertJTSMultiPoint((MultiPoint) jtsGeom);
	} else if (jtsGeom.getClass() == LineString.class) {
	    geom = convertJTSLineString((LineString) jtsGeom);
	} else if (jtsGeom.getClass() == LinearRing.class) {
	    geom = convertJTSLinearRing((LinearRing) jtsGeom);
	} else if (jtsGeom.getClass() == MultiLineString.class) {
	    geom = convertJTSMultiLineString((MultiLineString) jtsGeom);
	} else if (jtsGeom.getClass() == Polygon.class) {
	    geom = convertJTSPolygon((Polygon) jtsGeom);
	} else if (jtsGeom.getClass() == MultiPolygon.class) {
	    geom = convertJTSMultiPolygon((MultiPolygon) jtsGeom);
	}

	if (geom != null)
	    try {
		return JGeometry.store(geom, connection);
	    } catch (SQLException e) {
		throw new HibernateSpatialException(
			"Problem during conversion from JTS to JGeometry", e);
	    }
	else
	    throw new UnsupportedOperationException("Conversion of "
		    + jtsGeom.getClass().getSimpleName()
		    + " to PGgeometry not supported");
    }

    private JGeometry convertJTSMultiPolygon(MultiPolygon polygon) {
	// TODO implement this
	throw new UnsupportedOperationException("Not yet implemented");
    }

    private JGeometry convertJTSPolygon(Polygon polygon) {
	int dim = getCoordDimension(polygon);
	Object[] coords = convertCoordinateArrays(
		convertGeometryToCoordArrays(polygon), dim);
	return JGeometry.createLinearPolygon(coords, dim, polygon.getSRID());
    }

    private JGeometry convertJTSMultiLineString(MultiLineString multiLineString) {
	int dim = getCoordDimension(multiLineString);
	Object[] coords = convertCoordinateArrays(
		convertGeometryToCoordArrays(multiLineString), dim);
	return JGeometry.createLinearMultiLineString(coords, dim,
		multiLineString.getSRID());
    }

    private JGeometry convertJTSLinearRing(LinearRing ring) {
	return convertJTSLineString(ring);
    }

    private JGeometry convertJTSLineString(LineString lineString) {
	int dim = getCoordDimension(lineString);
	double[] coords = convertCoordinates(lineString.getCoordinates(), dim);
	return JGeometry.createLinearLineString(coords, dim, lineString
		.getSRID());
    }

    private JGeometry convertJTSMultiPoint(MultiPoint multiPoint) {
	int dim = getCoordDimension(multiPoint);
	Object[] coords = convertCoordinateArrays(
		convertGeometryToCoordArrays(multiPoint), dim);
	return JGeometry.createMultiPoint(coords, dim, multiPoint.getSRID());
    }

    private JGeometry convertJTSPoint(Point jtsGeom) {
	int dim = getCoordDimension(jtsGeom);
	double[] coord = convertCoordinates(jtsGeom.getCoordinates(), dim);
	return JGeometry.createPoint(coord, dim, jtsGeom.getSRID());
    }

    private double[] convertCoordinates(Coordinate[] coordinates, int dim) {
	if (dim < 2 || dim > 3)
	    throw new IllegalArgumentException(
		    "Dim parameter value must be between 2 and 3");
	double[] converted = new double[coordinates.length * dim];
	for (int i = 0; i < coordinates.length; i++) {
	    converted[i * dim] = coordinates[i].x;
	    converted[i * dim + 1] = coordinates[i].y;
	    if (dim == 3)
		converted[i * dim + 2] = coordinates[i].z;
	}
	return converted;
    }

    /**
         * @param coordinateArrays
         *                array of Coordinate arrays
         * @param dim
         *                -dimension of the coordinates
         * @return
         */
    private Object[] convertCoordinateArrays(Object[] coordinateArrays, int dim) {
	Object[] converted = new Object[coordinateArrays.length];
	for (int i = 0; i < coordinateArrays.length; i++) {
	    Coordinate[] coordinates = (Coordinate[]) (coordinateArrays[i]);
	    converted[i] = convertCoordinates(coordinates, dim);
	}
	return converted;
    }

    private Object[] convertGeometryToCoordArrays(Geometry geom) {

	if (geom.getClass() == MultiPoint.class
		|| geom.getClass() == MultiLineString.class) {
	    Object[] arr = new Object[geom.getNumGeometries()];
	    for (int i = 0; i < arr.length; i++) {
		arr[i] = geom.getGeometryN(i).getCoordinates();
	    }
	    return arr;
	} else if (geom.getClass() == Polygon.class) {
	    Polygon poly = (Polygon) geom;
	    Object[] arr = new Object[poly.getNumInteriorRing() + 1];
	    Coordinate[] outer = poly.getExteriorRing().getCoordinates();
	    // For Oracle Spatial, outer ring must be counter clockwise
	    if (!CGAlgorithms.isCCW(outer))
	    	arr[0] = reverseRing(outer);
	    else
		arr[0] = outer;
	    
	    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
		Coordinate[] inner =  poly.getInteriorRingN(i).getCoordinates();
		if (CGAlgorithms.isCCW(inner))
		    arr[i + 1] = reverseRing(inner);
		else
		    arr[i + 1] = inner;
		
	    }
	    return arr;
	} else
	    throw new IllegalArgumentException(
		    "geom must of of type MultiPoint, MultiLineString or Polygon");
    }

    @Override
    public Geometry convert2JTS(Object geomObj) {
	if (geomObj == null)
	    return null;
	
	JGeometry jgeom;
	try {
	    jgeom = JGeometry.load((STRUCT) geomObj);
	} catch (SQLException e) {
	    throw new HibernateSpatialException(
		    "Error loading Oracle Spatial Geometry Object", e);
	}

	if (jgeom.getDimensions() > 3 || jgeom.isLRSGeometry())
	    throw new UnsupportedOperationException(
		    "Cannot handle LRS Geometries, or geometries with more than 3 dimensions");

	Geometry jtsGeom = null;
	if (jgeom.getType() == JGeometry.GTYPE_POINT) {
	    double[] ordinates = jgeom.getOrdinatesArray();
	    if (ordinates == null)
		ordinates = jgeom.getPoint();
	    CoordinateSequence cs = convertOrdinateArray(jgeom
		    .getOrdinatesArray(), jgeom.getDimensions());
	    jtsGeom = geomFactory.createPoint(cs);
	    jtsGeom.setSRID(jgeom.getSRID());
	} else if (jgeom.getType() == JGeometry.GTYPE_MULTIPOINT) {
	    CoordinateSequence cs = convertOrdinateArray(jgeom
		    .getOrdinatesArray(), jgeom.getDimensions());
	    jtsGeom = geomFactory.createMultiPoint(cs);
	    jtsGeom.setSRID(jgeom.getSRID());
	} else if (jgeom.getType() == JGeometry.GTYPE_CURVE) {
	    ElemInfo elem = new ElemInfo();
	    elem.parse(jgeom.getOrdinatesArray(),jgeom.getElemInfo(), jgeom.getDimensions());
    	    jtsGeom = createLineString(elem, jgeom.getDimensions(),jgeom.getSRID());
	} else if (jgeom.getType() == JGeometry.GTYPE_MULTICURVE) {
	    ElemInfo elem = new ElemInfo();
	    elem.parse(jgeom.getOrdinatesArray(), jgeom.getElemInfo(), jgeom.getDimensions());
	    LineString[] linestrings = new LineString[elem.getNumElems()];
	    for (int i = 0; elem != null; elem = elem.getNext(), i++){
		linestrings[i] = createLineString(elem, jgeom.getDimensions(), jgeom.getSRID());
	    }
	    jtsGeom = geomFactory.createMultiLineString(linestrings);
	    jtsGeom.setSRID(jgeom.getSRID());
	} else if (jgeom.getType() == JGeometry.GTYPE_POLYGON) {
	    ElemInfo elem = new ElemInfo();
	    elem.parse(jgeom.getOrdinatesArray(), jgeom.getElemInfo(), jgeom.getDimensions());
	    jtsGeom = createPolygon(elem, jgeom.getDimensions(), jgeom.getSRID());
	} else if (jgeom.getType() == JGeometry.GTYPE_MULTIPOLYGON) {
	    ElemInfo elem = new ElemInfo();
	    elem.parse(jgeom.getOrdinatesArray(), jgeom.getElemInfo(), jgeom.getDimensions());
	    ElemInfo[] polyElems = elem.toPolyElems();	   
	    Polygon[] polygons = new Polygon[polyElems.length]; 
	    for (int i = 0; i < polyElems.length; i++){
		polygons[i] = createPolygon(polyElems[i], jgeom.getDimensions(), jgeom.getSRID()); 
	    }
	    jtsGeom =geomFactory.createMultiPolygon(polygons);
	    jtsGeom.setSRID(jgeom.getSRID());		
	} else {
	    throw new IllegalArgumentException("Unsupported JGeometry type");
	}

	return jtsGeom;
    }
    
    
    private Polygon createPolygon(ElemInfo elem, int dimensions, int srid) {
	LinearRing shell = null; 
	LinearRing[] holes = null;
	int idxInteriorRings = 0;
	LinearRing[] rings = new LinearRing[elem.getNumElems()];
	for (int i = 0; i < rings.length; i++){
	    if (elem.getElementType() == ElementTypes.INTERIOR_RING_ARC_SEGMENTS ||
		    elem.getElementType() == ElementTypes.INTERIOR_RING_CIRCLE ||
		    elem.getElementType() == ElementTypes.INTERIOR_RING_RECT ||
		    elem.getElementType() == ElementTypes.INTERIOR_RING_STRAIGHT_SEGMENTS) { 
		rings[idxInteriorRings] = createRing(elem,dimensions);
		rings[idxInteriorRings].setSRID(srid);
		idxInteriorRings++;
	    } else {
		if (shell != null)
		    break;
		shell = createRing(elem, dimensions);
		shell.setSRID(srid);
	    }
	    elem = elem.getNext();
	}
	holes = new LinearRing[idxInteriorRings];
	System.arraycopy(rings,0, holes, 0, idxInteriorRings);
	
	Polygon polygon = geomFactory.createPolygon(shell, holes);
	polygon.setSRID(srid);
	return polygon;
    }

    private LinearRing createRing(ElemInfo elem, int dim){
	CoordinateSequence cs = convertOrdinateArray(elem.getOrdinates(), dim);
	//if the element is a rectangle, than complete the ring
	if (elem.getElementType() == ElementTypes.EXTERIOR_RING_RECT ||
		elem.getElementType() == ElementTypes.INTERIOR_RING_RECT){
	    Coordinate ll = cs.getCoordinate(0);
	    Coordinate ur = cs.getCoordinate(1);
	    Coordinate lr = new Coordinate(ur.x, ll.y);
	    Coordinate ul = new Coordinate(ll.x, ur.y);
	    cs = geomFactory.getCoordinateSequenceFactory().create(new Coordinate[]{ll, lr, ur, ul, ll});	    
	} 
	
	LinearRing ring = geomFactory.createLinearRing(cs);
	return ring;
    }
    
    private LineString createLineString(ElemInfo elem, int dim, int SRID){
	    CoordinateSequence cs = convertOrdinateArray(elem.getOrdinates(),dim);
	    LineString ls =  geomFactory.createLineString(cs);
	    ls.setSRID(SRID);
	    return ls;
    }

    private CoordinateSequence convertOrdinateArray(double[] oordinates, int dim) {
	Coordinate[] coordinates = new Coordinate[oordinates.length / dim];
	for (int i = 0; i < coordinates.length; i++) {
	    if (dim == 2)
		coordinates[i] = new Coordinate(oordinates[i * dim],
			oordinates[i * dim + 1]);
	    else if (dim == 3)
		coordinates[i] = new Coordinate(oordinates[i * dim],
			oordinates[i * dim + 1], oordinates[i * dim + 2]);
	}
	return geomFactory.getCoordinateSequenceFactory().create(coordinates);
    }

    private int getCoordDimension(Geometry jtsGeom) {
	Coordinate cd = jtsGeom.getCoordinate();
	if (cd != null && !Double.isNaN(cd.z))
	    return 3;
	else
	    return 2;
    }
    
    //reverses ordinates in a coordinate array in-place
    private Coordinate[] reverseRing(Coordinate[] ar){
	for (int i = 0; i < ar.length / 2; i++){
	    Coordinate cs = ar[i];
	    ar[i] = ar[ar.length -1 -i];
	    ar[ar.length - 1 - i] = cs;
	}
	return ar;
    }

    @Override
    public int[] sqlTypes() {
	return geometryTypes;
    }

    private static class ElementTypes {
	public static int UNSUPPORTED = 0;

	public static int POINT = 1;

	public static int ORIENTATION = 2;

	public static int POINT_CLUSTER = 3;

	public static int LINE_STRAITH_SEGMENTS = 4;

	public static int LINE_ARC_SEGMENTS = 5;

	public static int INTERIOR_RING_STRAIGHT_SEGMENTS = 6;

	public static int EXTERIOR_RING_STRAIGHT_SEGMENTS = 7;

	public static int INTERIOR_RING_ARC_SEGMENTS = 8;

	public static int EXTERIOR_RING_ARC_SEGMENTS = 9;

	public static int INTERIOR_RING_RECT = 10;

	public static int EXTERIOR_RING_RECT = 11;

	public static int INTERIOR_RING_CIRCLE = 12;

	public static int EXTERIOR_RING_CIRCLE = 13;

	public static int COMPOUND_LINE = 14;

	public static int COMPOUND_EXTERIOR_RING = 15;

	public static int COMPOUND_INTERIOR_RING = 16;
    }

    /**
         * @author Karel Maesen
         * 
         * 
         * represents a simplified view on the Elements in an SDO_Geometry.
         * Circular segments are all linearized, and compound elements combined
         * into a single element with all segments linearized.
         * 
         */
    private class ElemInfo {

	private double[] linearOrdinates = null;

	private int elementType = -1;

	// used for compound elements;
	private int numCompounds = 0;

	// used for point clusters
	private int numPoints = 0;

	private ElemInfo next = null;
	
	protected int dimension = 2;
	
	protected void setDimension(int dim){
	    this.dimension = dim;
	}
	
	
	private boolean isExteriorRing(){
	    if (getElementType() == ElementTypes.EXTERIOR_RING_ARC_SEGMENTS ||
			getElementType() == ElementTypes.EXTERIOR_RING_CIRCLE ||
			getElementType() == ElementTypes.EXTERIOR_RING_RECT ||
			getElementType() == ElementTypes.EXTERIOR_RING_STRAIGHT_SEGMENTS ){
		return true;
	    } else
		return false;
	}
	
	private int getNumExteriorRings(){
	    ElemInfo elem = this;
	    int cnt = 0;
	    while (elem != null){
		if (elem.isExteriorRing())
		    cnt++;
		elem = elem.getNext();
	    }
	    return cnt;
	}
	/**
	 * This splits the ElemInfo into an array of ElemInfo's, each pertaining to exactly one polygon
	 * @return
	 */
	public ElemInfo[] toPolyElems() {
	    if (getNumExteriorRings() == 0){
		throw new IllegalStateException("toPolyElems() should only be invoked on Polygon ElemInfo's");
	    } else if (getNumExteriorRings() == 1){
		return new ElemInfo[]{this};
	    } else {
		//For multipolygons, the 
		//
		ElemInfo elem = this;
		ElemInfo[] polyElems = new ElemInfo[getNumExteriorRings()];
		int pIdx = 0;
		while (elem != null){
		    if (elem.isExteriorRing()){
			polyElems[pIdx++] = elem;
		    }
		    elem = elem.getNext();
		}
		//strip beyond next Exterior Ring
		for (int i = 0; i < polyElems.length;i++){
		    ElemInfo pe = polyElems[i];
		    while (pe.getNext() != null){
			if (pe.getNext().isExteriorRing()){
			    pe.setNextElemInfo(null);
			    break;
			}			    
			pe  = pe.getNext();
		    }
		}
		return polyElems;
	    }
	}

	protected void setLinearOrdinates(double[] ordinates){
	    this.linearOrdinates = ordinates;
	}
	
	protected void setElementType(int type){
	    this.elementType = type;
	}
	
	protected void setNumCompounds(int num){
	    this.numCompounds = num;
	}
	
	protected void setNumPoints(int num){
	    this.numPoints = num;
	}
	
	protected void setNextElemInfo(ElemInfo elemInfo){
	    this.next = elemInfo;
	}

	public void parse(double[] ordinates, int[] elemInfo, int dim) {
	    this.dimension = dim;
	    if (ordinates.length == 0 || elemInfo.length == 0)
		return;
	    int numOrdinates = 0;
	    if (elemInfo[1] == 0)
		elementType = ElementTypes.UNSUPPORTED;
	    else if (elemInfo[1] == 1 && elemInfo[2] == 1) {
		elementType = ElementTypes.POINT;
		linearOrdinates = getElemOrdinates(ordinates, elemInfo, 0, dim);
		numOrdinates = linearOrdinates.length;
	    } else if (elemInfo[1] == 1 && elemInfo[2] == 0) {
		elementType = ElementTypes.ORIENTATION;
		linearOrdinates = getElemOrdinates(ordinates, elemInfo, 0, dim);
		numOrdinates = linearOrdinates.length;
	    } else if (elemInfo[1] == 1 && elemInfo[2] > 1) {
		elementType = ElementTypes.POINT_CLUSTER;
		linearOrdinates = getElemOrdinates(ordinates, elemInfo, 0, dim);
		numPoints = elemInfo[2];
		numOrdinates = linearOrdinates.length;
	    } else if (elemInfo[1] == 2 && elemInfo[2] == 1) {
		elementType = ElementTypes.LINE_STRAITH_SEGMENTS;
		linearOrdinates = getElemOrdinates(ordinates, elemInfo, 0, dim);
		numOrdinates = linearOrdinates.length;
	    } else if (elemInfo[1] == 2 && elemInfo[2] == 2) {
		elementType = ElementTypes.LINE_ARC_SEGMENTS;
		double[] arcOrdinates = getElemOrdinates(ordinates, elemInfo,
			0, dim);
		numOrdinates = arcOrdinates.length;
		linearOrdinates = linearize(arcOrdinates, dim);
	    } else if (elemInfo[1] == 1003 && elemInfo[2] == 1) {
		elementType = ElementTypes.EXTERIOR_RING_STRAIGHT_SEGMENTS;
		linearOrdinates = getElemOrdinates(ordinates, elemInfo, 0, dim);
		numOrdinates = linearOrdinates.length;
	    } else if (elemInfo[1] == 2003 && elemInfo[2] == 1) {
		elementType = ElementTypes.INTERIOR_RING_STRAIGHT_SEGMENTS;
		linearOrdinates = getElemOrdinates(ordinates, elemInfo, 0, dim);
		numOrdinates = linearOrdinates.length;
	    } else if (elemInfo[1] == 1003 && elemInfo[2] == 2) {
		elementType = ElementTypes.EXTERIOR_RING_ARC_SEGMENTS;
		double[] arcOrdinates = getElemOrdinates(ordinates, elemInfo,
			0, dim);
		numOrdinates = arcOrdinates.length;
		linearOrdinates = linearize(arcOrdinates, dim);
	    } else if (elemInfo[1] == 2003 && elemInfo[2] == 2) {
		elementType = ElementTypes.INTERIOR_RING_ARC_SEGMENTS;
		double[] arcOrdinates = getElemOrdinates(ordinates, elemInfo,
			0, dim);
		numOrdinates = arcOrdinates.length;
		linearOrdinates = linearize(arcOrdinates, dim);
	    } else if (elemInfo[1] == 1003 && elemInfo[2] == 3) {
		elementType = ElementTypes.EXTERIOR_RING_RECT;
		linearOrdinates = getElemOrdinates(ordinates, elemInfo, 0, dim);
		numOrdinates = linearOrdinates.length;
	    } else if (elemInfo[1] == 2003 && elemInfo[2] == 3) {
		elementType = ElementTypes.INTERIOR_RING_RECT;
		linearOrdinates = getElemOrdinates(ordinates, elemInfo, 0, dim);
		numOrdinates = linearOrdinates.length;
	    } else if (elemInfo[1] == 1003 && elemInfo[2] == 4) {
		elementType = ElementTypes.EXTERIOR_RING_CIRCLE;
		double[] arcOrdinates = getElemOrdinates(ordinates, elemInfo,
			0, dim);
		numOrdinates = arcOrdinates.length;
		linearOrdinates = linearize(arcOrdinates, dim);
	    } else if (elemInfo[1] == 2003 && elemInfo[2] == 4) {
		elementType = ElementTypes.INTERIOR_RING_CIRCLE;
		double[] arcOrdinates = getElemOrdinates(ordinates, elemInfo,
			0, dim);
		numOrdinates = arcOrdinates.length;
		linearOrdinates = linearize(arcOrdinates, dim);
	    } else if (elemInfo[1] == 4) {
		elementType = ElementTypes.COMPOUND_LINE;
		numCompounds = elemInfo[2];
		double[] cmpOrdinates = getElemOrdinates(ordinates, elemInfo,
			numCompounds, dim);
		numOrdinates = cmpOrdinates.length;
		int[] cmpElemInfo = getCompoundElemInfo(elemInfo, numCompounds);
		CElemInfo celemInfo = new CElemInfo();
		celemInfo.parse(cmpOrdinates, cmpElemInfo,
			dim);
		linearOrdinates = celemInfo.flattenOrdinates();

	    } else if (elemInfo[1] == 1005) {
		elementType = ElementTypes.COMPOUND_EXTERIOR_RING;
		numCompounds = elemInfo[2];
		double[] cmpOrdinates = getElemOrdinates(ordinates, elemInfo,
			numCompounds, dim);
		numOrdinates = cmpOrdinates.length;
		int[] cmpElemInfo = getCompoundElemInfo(elemInfo, numCompounds);
		CElemInfo celemInfo = new CElemInfo();
		celemInfo.parse(cmpOrdinates, cmpElemInfo,
			dim);
		linearOrdinates = celemInfo.flattenOrdinates();
	    } else if (elemInfo[1] == 1005) {
		elementType = ElementTypes.COMPOUND_INTERIOR_RING;
		numCompounds = elemInfo[2];
		double[] cmpOrdinates = getElemOrdinates(ordinates, elemInfo,
			numCompounds, dim);
		numOrdinates = cmpOrdinates.length;
		int[] cmpElemInfo = getCompoundElemInfo(elemInfo, numCompounds);
		CElemInfo celemInfo = new CElemInfo();
		celemInfo.parse(cmpOrdinates, cmpElemInfo,
			dim);
		linearOrdinates = celemInfo.flattenOrdinates();
	    } else
		throw new IllegalArgumentException(
			"Can't convert ELEM_INFO to type");

	    if (!(ordinates.length == numOrdinates)){
		int[] tailElemInfo = new int[elemInfo.length
		    - (3 * (numCompounds + 1))];
	    	System.arraycopy(elemInfo, 3 * (numCompounds + 1), tailElemInfo, 0,
		    tailElemInfo.length);
	    	ordinates = getTailOrdinates(ordinates, numOrdinates);
	    	elemInfo = tailElemInfo;
	    	next = createNextElemInfo();
	    	next.parse(ordinates, elemInfo, dim);
	    	}
	}
	
	protected double [] getTailOrdinates(double[] ordinates, int numOrdinates){
	    double[] tailOrdinates = new double[ordinates.length - numOrdinates];
	    System.arraycopy(ordinates, numOrdinates, tailOrdinates, 0,
		    tailOrdinates.length);	    
	    return tailOrdinates;
	}
	
	protected int[] getTailElemInfo(int[] elemInfo, int compounds){
	    int[] tailElemInfo = new int[elemInfo.length
	             		    - (3 * (numCompounds + 1))];
	    System.arraycopy(elemInfo, 3 * (numCompounds + 1), tailElemInfo, 0,
	             		    tailElemInfo.length);
	    return tailElemInfo;
	}
	
	protected ElemInfo createNextElemInfo(){
	    return new ElemInfo();
	}

	protected double[] getElemOrdinates(double[] ordinates, int[] elemInfo,
		int numCompounds, int dim) {
	    int numOrdinates = 0;
	    if (elemInfo.length == (3 * (numCompounds + 1))) {
		numOrdinates = ordinates.length;
		return ordinates;
	    } else {
		numOrdinates = elemInfo[3 * (numCompounds + 1)] - elemInfo[0];
		double[] elemOrdinates = new double[numOrdinates];
		System.arraycopy(ordinates, 0, elemOrdinates, 0,
			elemOrdinates.length);
		return elemOrdinates;
	    }
	}

	protected int[] getCompoundElemInfo(int[] elemInfo, int numCompounds) {
	    int infoArraySize = 3;
	    int startOffset = 0;
	    if (numCompounds > 0) {
		infoArraySize = 3 * numCompounds;
		startOffset = 3;
	    }
	    int[] celemInfo = new int[infoArraySize];
	    System.arraycopy(elemInfo, startOffset, celemInfo, 0,
		    celemInfo.length);
	    return celemInfo;
	}

	private double[] linearize(double[] arcOrdinates, int dim) {
	    int numOrd = 2;
	    double[] linearizedCoords = new double[0];
	    // this only works with 2-Dimensional geometries, since we use
	    // JGeometry linearization;
	    if (dim != 2)
		throw new IllegalArgumentException(
			"Can only linearize 2D arc segments, but geometry is "
				+ dim + "D.");
	    while (numOrd < arcOrdinates.length) {
		numOrd -= 2; // start point of next arc segment is end point
		// of previous segment, and is not repeated.
		double x1 = arcOrdinates[numOrd++];
		double y1 = arcOrdinates[numOrd++];
		double x2 = arcOrdinates[numOrd++];
		double y2 = arcOrdinates[numOrd++];
		double x3 = arcOrdinates[numOrd++];
		double y3 = arcOrdinates[numOrd++];
		double[] result = JGeometry
			.linearizeArc(x1, y1, x2, y2, x3, y3);
		//if this is not the first arcsegment, the first linearized point is already in linearizedArc, so disregard this.
		int resultBegin = 2;
		if (linearizedCoords.length == 0)
		    resultBegin = 0;
		
		int destPos = linearizedCoords.length;
		double[] tmpCoords = new double[linearizedCoords.length
			+ result.length - resultBegin];
		System.arraycopy(linearizedCoords, 0, tmpCoords, 0, linearizedCoords.length);
		System.arraycopy(result, resultBegin, tmpCoords, destPos,
			result.length - resultBegin);
		linearizedCoords = tmpCoords;
	    }
	    
	    //garuantee that first and last oordinate pairs of the
	    //linearizedOrdinates are exactly the same as in input ArcOrdinates
	    //This is neede because JGeometry linearization "bruises" coordinates
	    //which causes problems in case of linear rings.
	    linearizedCoords[0] = arcOrdinates[0];
	    linearizedCoords[1] = arcOrdinates[1];
	    linearizedCoords[linearizedCoords.length - 1] = arcOrdinates[arcOrdinates.length - 1];
	    linearizedCoords[linearizedCoords.length - 2] = arcOrdinates[arcOrdinates.length - 2];
    
	    return linearizedCoords;
	}


	public double[] getOrdinates() {
	    return linearOrdinates;
	}

	public int getElementType() {
	    return elementType;
	}

	public int getNumCompounts() {
	    return numCompounds;
	}

	public int getNumPoints() {
	    return numPoints;
	}

	public ElemInfo getNext() {
	    return next;
	}
	
	public int getNumElems(){
	    int cnt = 0;
	    for (ElemInfo elem = this; elem != null; elem = elem.getNext(), cnt++);
	    return cnt;
	}

    }

    private class CElemInfo extends ElemInfo {


	public CElemInfo() {
	    super();
	}
	
	protected ElemInfo createNextElemInfo(){
	    return new CElemInfo();
	}


	//subelements of a compound element are contiguous. Thefore
	//we include the first coordinate of the next subelement.
	protected double[] getElemOrdinates(double[] ordinates, int[] elemInfo,
		int numCompounds, int dim) {
	    if (elemInfo.length == (3 * (numCompounds + 1))) {
		return ordinates;
	    } else {
		//numOrdinates includes first ordinate of next subelement, if there is one
		int numOrdinates = elemInfo[3 * (numCompounds + 1)] - elemInfo[0] + dim; 
		double[] elemOrdinates = new double[numOrdinates];
		System.arraycopy(ordinates, 0, elemOrdinates, 0,
			elemOrdinates.length);
		return elemOrdinates;
	    }
	}

	protected double [] getTailOrdinates(double[] ordinates, int numOrdinates){
	    double[] tailOrdinates = new double[ordinates.length - numOrdinates + this.dimension];
	    if (numOrdinates < ordinates.length)
		System.arraycopy(ordinates, numOrdinates - this.dimension, tailOrdinates, 0,
		    tailOrdinates.length);
		else
		    System.arraycopy(ordinates, numOrdinates, tailOrdinates, 0,
			    tailOrdinates.length);
	    return tailOrdinates;
	}
	protected double[] flattenOrdinates() {
	    ElemInfo elem = this;
	    double[] ordinates = new double[elem.getOrdinates().length];
	    System.arraycopy(elem.getOrdinates(),0, ordinates,0, this.getOrdinates().length);
	    int nextStartPos = ordinates.length;
	    while (elem.getNext() != null){
		elem = elem.getNext();
		int nextOrdinateArraySize = elem.getOrdinates().length - dimension;
		double[] newArray = new double[ordinates.length + nextOrdinateArraySize];
		System.arraycopy(ordinates, 0, newArray,0, ordinates.length);
		System.arraycopy(elem.getOrdinates(),dimension, newArray, nextStartPos, nextOrdinateArraySize);
		ordinates = newArray;
		nextStartPos = ordinates.length;
	    }
	    return ordinates;
	}

    }

}