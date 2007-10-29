/**
 * $Id$
 *
 * This file is part of Spatial Hibernate, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
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
 * For more information, visit: http://www.cadrie.com/
 */

package org.hibernatespatial.oracle.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import junit.framework.JUnit4TestAdapter;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.mgeom.MCoordinate;
import org.hibernatespatial.mgeom.MCoordinateSequence;
import org.hibernatespatial.mgeom.MGeometry;
import org.hibernatespatial.mgeom.MGeometryFactory;
import org.hibernatespatial.oracle.test.model.TestGeom;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * Test cases for the Oracle SDO_GEOMETRY to JTS Geometry conversion routines
 * 
 * @author Karel Maesen <p/> The test geometries are those from the Oracle
 *         Spatial User's Guide and Reference, section 2.5, Example 2-13
 */
public class TestGeomConversion {

	private static SessionFactory sf = null;

	private static final Map<Long, Geometry> expected = new HashMap<Long, Geometry>();

	private static final MGeometryFactory geomFactory = new MGeometryFactory(
			new PrecisionModel());

	static {
		Geometry geom = null;
		Coordinate[] coordinates;

		// case 1: Point
		geom = geomFactory.createPoint(new Coordinate(10, 5));
		expected.put(1L, geom);

		// case 2: LineString
		coordinates = new Coordinate[2];
		coordinates[0] = new Coordinate(10., 10.);
		coordinates[1] = new Coordinate(20., 10.);
		geom = geomFactory.createLineString(coordinates);
		expected.put(2L, geom);

		// case 3: arcsegment
		// TO DO - can't check argsegments
		coordinates = new Coordinate[3];
		coordinates[0] = new Coordinate(10., 15.);
		coordinates[1] = new Coordinate(15., 20.);
		coordinates[2] = new Coordinate(20., 15.);
		geom = geomFactory.createMultiPoint(coordinates);
		expected.put(3L, geom);

		// case 4: linestring
		coordinates = new Coordinate[4];
		coordinates[0] = new Coordinate(10., 25.);
		coordinates[1] = new Coordinate(20., 30.);
		coordinates[2] = new Coordinate(25., 25.);
		coordinates[3] = new Coordinate(30., 30.);
		geom = geomFactory.createLineString(coordinates);
		expected.put(4L, geom);

		// case 5: arcstring
		coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(10., 35.);
		coordinates[1] = new Coordinate(15., 40.);
		coordinates[2] = new Coordinate(20., 35.);
		coordinates[3] = new Coordinate(25., 30.);
		coordinates[4] = new Coordinate(30., 35.);
		geom = geomFactory.createMultiPoint(coordinates);
		expected.put(5L, geom);

		// case 6: Compound line string
		coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(10., 45.);
		coordinates[1] = new Coordinate(20., 45.);
		coordinates[2] = new Coordinate(23., 48.);
		coordinates[3] = new Coordinate(20., 51.);
		coordinates[4] = new Coordinate(10., 51.);
		geom = geomFactory.createMultiPoint(coordinates);
		expected.put(6L, geom);

		// case 7: Closed line string
		coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(10., 55.);
		coordinates[1] = new Coordinate(15., 55.);
		coordinates[2] = new Coordinate(20., 60.);
		coordinates[3] = new Coordinate(10., 60.);
		coordinates[4] = new Coordinate(10., 55.);
		geom = geomFactory.createLineString(coordinates);
		expected.put(7L, geom);

		// case 8 - 10 not implemented

		// case 11: Polygon
		coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(10., 105.);
		coordinates[1] = new Coordinate(15., 105.);
		coordinates[2] = new Coordinate(20., 110.);
		coordinates[3] = new Coordinate(10., 110.);
		coordinates[4] = new Coordinate(10., 105.);
		geom = geomFactory.createPolygon(geomFactory
				.createLinearRing(coordinates), null);
		expected.put(11L, geom);

		// case 12: Arc polygon
		coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(15., 115.);
		coordinates[1] = new Coordinate(20., 118.);
		coordinates[2] = new Coordinate(15., 120.);
		coordinates[3] = new Coordinate(10., 118.);
		coordinates[4] = new Coordinate(15., 115.);
		geom = geomFactory.createMultiPoint(coordinates);
		expected.put(12L, geom);

		// case 13: compount polygon
		// 10,128, 10,125, 20,125, 20,128, 15,130, 10,128))
		coordinates = new Coordinate[6];
		coordinates[0] = new Coordinate(10., 128.);
		coordinates[1] = new Coordinate(10., 125.);
		coordinates[2] = new Coordinate(20., 125.);
		coordinates[3] = new Coordinate(20., 128.);
		coordinates[4] = new Coordinate(15., 130.);
		coordinates[5] = new Coordinate(10., 128.);
		geom = geomFactory.createMultiPoint(coordinates);
		expected.put(13L, geom);

		// case 14: Rectangle
		// 10,135, 20,140
		coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(10., 135.);
		coordinates[1] = new Coordinate(20., 135.);
		coordinates[2] = new Coordinate(20., 140.);
		coordinates[3] = new Coordinate(10., 140.);
		coordinates[4] = new Coordinate(10., 135.);
		geom = geomFactory.createPolygon(geomFactory
				.createLinearRing(coordinates), null);
		expected.put(14L, geom);

		// case 15 - not implemented

		// Case 16: point cluster
		// 50,5, 55,7, 60,5
		coordinates = new Coordinate[3];
		coordinates[0] = new Coordinate(50., 5.);
		coordinates[1] = new Coordinate(55., 7.);
		coordinates[2] = new Coordinate(60., 5.);
		geom = geomFactory.createMultiPoint(coordinates);
		expected.put(16L, geom);

		// Case 17: multi point
		// 65,5, 70,7, 75,5)
		coordinates = new Coordinate[3];
		coordinates[0] = new Coordinate(65., 5.);
		coordinates[1] = new Coordinate(70., 7.);
		coordinates[2] = new Coordinate(75., 5.);
		geom = geomFactory.createMultiPoint(coordinates);
		expected.put(17L, geom);

		// Case 18: multi line
		// 50,15, 55,15, 60,15, 65,15
		LineString[] lineStrings = new LineString[2];
		coordinates = new Coordinate[2];
		coordinates[0] = new Coordinate(50., 15.);
		coordinates[1] = new Coordinate(55., 15.);
		lineStrings[0] = geomFactory.createLineString(coordinates);
		coordinates = new Coordinate[2];
		coordinates[0] = new Coordinate(60., 15.);
		coordinates[1] = new Coordinate(65., 15.);
		lineStrings[1] = geomFactory.createLineString(coordinates);
		geom = geomFactory.createMultiLineString(lineStrings);
		expected.put(18L, geom);

		// Cases 19 - 22 not implemented

		// Case 23: disjoint multipolygon (exterior_ring, exterior_rect)
		// 50,105, 55,105, 60,110, 50,110, 50,105, 62,108, 65,112
		LinearRing[] rings = new LinearRing[2];
		Polygon[] polygons = new Polygon[2];
		coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(50., 105.);
		coordinates[1] = new Coordinate(55., 105.);
		coordinates[2] = new Coordinate(60., 110.);
		coordinates[3] = new Coordinate(50., 110);
		coordinates[4] = new Coordinate(50., 105);
		rings[0] = geomFactory.createLinearRing(coordinates);
		coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(62., 108.);
		coordinates[1] = new Coordinate(65., 108.);
		coordinates[2] = new Coordinate(65., 112.);
		coordinates[3] = new Coordinate(62., 112);
		coordinates[4] = new Coordinate(62., 108.);
		rings[1] = geomFactory.createLinearRing(coordinates);
		polygons[0] = geomFactory.createPolygon(rings[0], null);
		polygons[1] = geomFactory.createPolygon(rings[1], null);
		geom = geomFactory.createMultiPolygon(polygons);
		expected.put(23L, geom);

		// Case 27 - polygon with void:
		// 'Polygon with void',
		// sdo_geometry (2003, null, null, sdo_elem_info_array (1,1003,3,
		// 5,2003,3),
		// sdo_ordinate_array (50,135, 60,140, 51,136, 59,139))
		coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(50., 135.);
		coordinates[1] = new Coordinate(60., 135.);
		coordinates[2] = new Coordinate(60., 140.);
		coordinates[3] = new Coordinate(50., 140);
		coordinates[4] = new Coordinate(50., 135);
		LinearRing shell = geomFactory.createLinearRing(coordinates);
		coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(51., 136.);
		coordinates[1] = new Coordinate(51., 139);
		coordinates[2] = new Coordinate(59., 139.);
		coordinates[3] = new Coordinate(59., 136.);
		coordinates[4] = new Coordinate(51., 136);
		LinearRing hole = geomFactory.createLinearRing(coordinates);
		geom = geomFactory.createPolygon(shell, new LinearRing[] { hole });
		expected.put(27L, geom);

		// case 33: MLineString
		MCoordinate[] mCoordinates = new MCoordinate[4];
		mCoordinates[0] = MCoordinate.create2dWithMeasure(10., 25., 1);
		mCoordinates[1] = MCoordinate.create2dWithMeasure(20., 30., 2);
		mCoordinates[2] = MCoordinate.create2dWithMeasure(25., 25., 3);
		mCoordinates[3] = MCoordinate.create2dWithMeasure(30., 30., 4);
		geom = geomFactory.createMLineString(mCoordinates);
		expected.put(33L, geom);

		// case 34: LRS arcsegment
		mCoordinates = new MCoordinate[3];
		mCoordinates[0] = MCoordinate.create2dWithMeasure(10., 15., 0);
		mCoordinates[1] = MCoordinate.create2dWithMeasure(15., 20., 3);
		mCoordinates[2] = MCoordinate.create2dWithMeasure(20., 15., 5);
		geom = geomFactory.createMultiPoint(mCoordinates);
		expected.put(34L, geom);

		// case 35: LRS Point
		geom = geomFactory.createPoint(MCoordinate
				.create2dWithMeasure(10, 5, 0));
		expected.put(35L, geom);

		// case 36: Simple point
		geom = geom = geomFactory.createPoint(new Coordinate(12, 14));
		expected.put(36L, geom);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Configuration config = new Configuration();
		config.configure();
		config.addClass(TestGeom.class);
		try {
			sf = config.buildSessionFactory();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testPoint() {
		System.out.println("Case 1: Point");
		testCaseById(1L, false);
	}

	@Test
	public void testLineSegment() {
		System.out.println("Case 2: LineSegment");
		testCaseById(2L, false);
	}

	@Test
	public void testArcSegment() {
		System.out.println("Case 3: ArcSegment");
		testCaseById(3L, true);
	}

	@Test
	public void testLineString() {
		System.out.println("Case 4: linestring");
		testCaseById(4L, false);
	}

	@Test
	public void testArcString() {
		System.out.println("Case 5: arcstring");
		testCaseById(5L, true);
	}

	@Test
	public void testCompoundLineString() {
		System.out.println("Case 6: compound line string");
		Geometry geom = testCaseById(6L, true);
		assertTrue(geom.getGeometryType().equalsIgnoreCase("linestring"));
	}

	@Test
	public void testCLosedLineString() {
		System.out.println("Case 7: closed linestring");
		Geometry geom = testCaseById(7L, false);
		assertTrue(geom.getGeometryType().equalsIgnoreCase("linestring"));
		assertTrue(((LineString) geom).isClosed());
	}

	@Test
	public void testPolygon() {
		System.out.println("Case 11: Polygon");
		Geometry geom = testCaseById(11L, false);
		assertTrue(geom.getGeometryType().equalsIgnoreCase("polygon"));
		geom.normalize();
		assertTrue(((Polygon) geom).isValid());
	}

	@Test
	public void testArcPolygon() {
		System.out.println("Case 12: Arc Polygon");
		Geometry geom = testCaseById(12L, true);
		assertTrue(geom.getGeometryType().equalsIgnoreCase("polygon"));
		assertTrue(((Polygon) geom).isValid());
	}

	@Test
	public void testCompoundPolygon() {
		System.out.println("Case 13: Compound Polygon");
		Geometry geom = testCaseById(13L, true);
		assertTrue(geom.getGeometryType().equalsIgnoreCase("polygon"));
		assertTrue(((Polygon) geom).isValid());
	}

	@Test
	public void testRectPolygon() {
		System.out.println("Case 14: Rectangle");
		Geometry geom = testCaseById(14L, false);
		assertTrue(geom.getGeometryType().equalsIgnoreCase("polygon"));
		assertTrue(((Polygon) geom).isValid());
	}

	@Test
	public void testPointCluster() {
		System.out.println("Case 16: PointCluster");
		Geometry geom = testCaseById(16L, false);
		assertTrue(geom.getGeometryType().equalsIgnoreCase("MultiPoint"));
	}

	@Test
	public void testMultiPoint() {
		System.out.println("Case 17: PointCluster");
		Geometry geom = testCaseById(17L, false);
		assertTrue(geom.getGeometryType().equalsIgnoreCase("MultiPoint"));
	}

	@Test
	public void testMultiLine() {
		System.out.println("Case 18: MultiLine");
		Geometry geom = testCaseById(18L, false);
		assertTrue(geom.getGeometryType().equalsIgnoreCase("MultiLineString"));
		assertEquals(geom.getNumGeometries(), 2);
	}

	@Test
	public void testMultiPolygon() {
		System.out.println("Case 23: MultiPolygon");
		Geometry geom = testCaseById(23L, false);
		assertTrue(geom.getGeometryType().equalsIgnoreCase("MultiPolygon"));
		assertEquals(geom.getNumGeometries(), 2);
	}

	@Test
	public void testPolygonVoid() {
		System.out.println("Case 27: Polygon with Void");
		Geometry geom = testCaseById(27L, false);
		assertTrue(geom.getGeometryType().equalsIgnoreCase("Polygon"));
		assertEquals(((Polygon) geom).getNumInteriorRing(), 1);
	}

	@Test
	public void testMLineString() {
		System.out.println("Case 33: 2D LineString with Measure");
		testLrsCaseById(33L, false);
	}

	@Test
	public void testSimplePoint() {
		System.out.println("Case 36: Point");
		testCaseById(36L, false);
	}

	@Test
	public void testMArcSegment() {
		System.out.println("Case 34: ArcSegment with Measure");
		testLrsCaseById(34L, true);
	}

	private Geometry testCaseById(long id, boolean isArc) {
		Session session = null;
		try {
			session = sf.openSession();
			TestGeom testGeom = (TestGeom) session.get(TestGeom.class, id);
			Geometry geom = testGeom.getGeom();
			// Test whether geometries as retrieved are equal to what is
			// expected
			if (!isArc) {
				assertTrue("Geometries differ for case#: " + id, expected.get(
						id).equalsExact(geom));
			} else {
				// only check if coordinates of expected are on the retrieved
				// geometry
				MultiPoint mpco = (MultiPoint) expected.get(id);
				for (int i = 0; i < mpco.getNumPoints(); i++) {
					assertTrue("Arc coordinate not on geometry for case# " + id
							+ "(point : " + mpco.getGeometryN(i) + "):", geom
							.isWithinDistance(mpco.getGeometryN(i), 0.03));
				}
			}
			// write geometry to table and read again
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				TestGeom writeTest = new TestGeom();
				writeTest.setGeom(geom);
				writeTest.setDescription("Testing jts to SDO case: " + id);
				session.save(writeTest);
				tx.commit();
				long writtenId = writeTest.getId();
				writeTest = null;
				TestGeom read2Test = (TestGeom) session.get(TestGeom.class,
						writtenId);
				Geometry read2Geom = read2Test.getGeom();
				assertTrue("Geometries differ for case#: " + id, expected.get(
						id).equalsExact(read2Geom));
				assertTrue("Geometries differ for case#: " + id, expected.get(
						id).equalsExact(read2Test.getGeom()));
				// tx = session.beginTransaction();
				// session.delete(read2Test);
				// tx.commit();
			} catch (Exception e) {
				try {
					tx.rollback();
				} catch (Exception re) {
				}
				throw new RuntimeException(e);
			}

			return geom;
		} finally {
			System.out.println("Closing session");
			if (session != null)
				session.close();
		}
	}

	private Geometry testLrsCaseById(long id, boolean isArc) {
		Geometry actualGeom = testCaseById(id, isArc);
		Geometry expectedGeom = expected.get(id);

		assertTrue(actualGeom instanceof MGeometry);
		MCoordinateSequence actualCS = new MCoordinateSequence(actualGeom
				.getCoordinates());
		MCoordinateSequence expectedCS = new MCoordinateSequence(expectedGeom
				.getCoordinates());
		if (isArc) {
			// verify the LRS begin and end measures didn't change
			int lastIndex = actualGeom.getCoordinates().length - 1;
			MCoordinate actualCBegin = (MCoordinate) actualGeom.getCoordinate();
			MCoordinate actualCEnd = (MCoordinate) actualGeom.getCoordinates()[lastIndex];

			lastIndex = expectedGeom.getCoordinates().length - 1;
			MCoordinate expectedCBegin = (MCoordinate) expectedGeom
					.getCoordinate();
			MCoordinate expectedCEnd = (MCoordinate) expectedGeom
					.getCoordinates()[lastIndex];

			assertTrue(Double.compare(actualCBegin.m, expectedCBegin.m) == 0);
			assertTrue(Double.compare(actualCEnd.m, expectedCEnd.m) == 0);
		} else {
			// compare each of the measures
			for (int i = 0; i < expectedCS.size(); i++) {
				assertEquals(expectedCS.getM(i), actualCS.getM(i));
			}
		}
		return actualGeom;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Closing factory");
		sf.close();
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestGeomConversion.class);
	}

}
