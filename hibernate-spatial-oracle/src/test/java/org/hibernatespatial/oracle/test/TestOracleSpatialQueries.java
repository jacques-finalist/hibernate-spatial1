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
package org.hibernatespatial.oracle.test;

import java.sql.Connection;
import java.sql.DriverManager;

import junit.framework.JUnit4TestAdapter;

import org.hibernatespatial.test.TestSpatialQueries;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * This is the Spatial Queries unit test suite for the Oracle Spatial provider
 * 
 * This unit test suite has been tested with Oracle 10g version 10.2.0.1.0
 * 
 * Remarks: <il> <li>Unit tests for the EQUALS relation fails due to bugs in
 * the Oracle implementation</li> <li>Unit test for Boundary functions fails
 * due to bugs in Oracle Implementation. Oracle's boundary function returns null
 * values when applied to linear (1-dimension) features.</li> <li>Unit test
 * for convexHull fails. Oracle's ConvexHull returns null for some geometries.</li>
 * <li>ST_Geometry(SDO_GEOMETRY) and
 * ST_Geometry.FROM_SDO_GEOMETRY(SDO_GEOMETRY) does not return the same result!
 * (Don't ask me why, I don't understand it either.) <li>Experience shows that
 * ST_Geometry.FROM_SDO_GEOMETRY is the safer choice. </il>
 * 
 */
public class TestOracleSpatialQueries {

	private final static String DBURL = "jdbc:oracle:thin:@test.geovise.com/orcl";

	private final static String DBNAME = "hbs";

	private final static String DBPASSWD = "hbs";

	private static Connection conn;

	private static TestSpatialQueries delegate;

	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(DBURL, DBNAME, DBPASSWD);
			delegate = new TestSpatialQueries();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// create the delegate

	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		delegate.setUpBeforeClass(conn);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		delegate.tearDownAfterClass();
	}

	@Test
	public void testHQLGeometryType() throws Exception {
		delegate.testHQLGeometryType();
	}

	@Test
	public void testHQLAsText() throws Exception {
		delegate.testHQLAsText();
	}

	@Test
	public void testHQLDimension() throws Exception {
		delegate.testHQLDimension();
	}

	@Test
	public void testHQLEnvelope() throws Exception {
		delegate.testHQLEnvelope();
	}

	@Test
	public void testHQLIntersectsLineString() throws Exception {
		String sqlString = "select count(*) from $table$ where sdo_relate(geom,sdo_geometry( ? , 31370), 'mask=ANYINTERACT') = 'TRUE' ";
		delegate.testHQLIntersectsLineString(sqlString);
	}

	@Test
	public void testHQLIsEmpty() throws Exception {
		String sql = "select count(*) from $table$ where geom is not null and MDSYS.OGC_ISEMPTY(MDSYS.ST_GEOMETRY.FROM_SDO_GEOM(geom)) = 1";
		delegate.testHQLIsEmpty(sql);
	}

	@Test
	public void testHQLIsSimple() throws Exception {
		String sql = "select count(*) from $table$ where geom is not null and MDSYS.OGC_ISSIMPLE(MDSYS.ST_GEOMETRY.FROM_SDO_GEOM(geom)) = 1";
		delegate.testHQLIsSimple(sql);
	}

	@Test
	public void testHQLBoundary() throws Exception {
		// This unit test throws an error for LineStrings.
		// Oracle 10g returns null values for linestring boundaries.
		delegate.testHQLBoundary();
	}

	@Test
	public void testHQLOverlaps() throws Exception {
		String sql = "select count(*) from $table$ where geom is not null and mdsys.OGC_OVERLAP(mdsys.st_geometry.from_sdo_geom(sdo_geometry(?, 31370)), mdsys.st_geometry.from_sdo_geom(geom)) = 1";
		delegate.testHQLOverlaps(sql);
	}

	@Test
	public void testHQLSRID() throws Exception {
		delegate.testHQLSRID();
	}
	
	@Test
	public void testExtent() throws Exception {
		String sql = "select SDO_GEOM.SDO_AREA(SDO_AGGR_MBR(geom), .001) from $table$";
		delegate.testExtent(sql);
	}
	
	@Test
	public void testHQLExtent() throws Exception {
		String sql = "select SDO_GEOM.SDO_AREA(SDO_AGGR_MBR(geom), .001) from $table$";
		delegate.testHQLExtent(sql);
	}

	@Test
	public void testFiltering() throws Exception {
		String sql = "select count(*) from $table$ where SDO_FILTER(geom, sdo_geometry(?,31370)) = 'TRUE'";
		delegate.testFiltering(sql);
	}

	@Test
	public void testContains() throws Exception {
		String sql = "select count(*) from $table$ where SDO_CONTAINS(geom, sdo_geometry(?,31370)) = 'TRUE'";
		delegate.testContains(sql);
	}

	@Test
	public void testHQLRelateLineString() throws Exception {
		delegate.testHQLRelateLineString();
	}

	@Test
	public void testHQLDisjoint() throws Exception {
		String sql = "select count(*) from $table$ where geom is not null and mdsys.OGC_DISJOINT(mdsys.st_geometry.from_sdo_geom(sdo_geometry(?, 31370)), mdsys.st_geometry.from_sdo_geom(geom)) = 1";
		delegate.testHQLDisjoint(sql);
	}

	@Test
	public void testHQLDistance() throws Exception {
		delegate.testHQLDistance();
	}

	@Test
	public void testHQLBuffer() throws Exception {
		delegate.testHQLBuffer();
	}

	@Test
	public void testHQLConvexHull() throws Exception {
		delegate.testHQLConvexHull();
	}

	@Test
	public void testHQLDifference() throws Exception {
		delegate.testHQLDifference();
	}

	@Test
	public void testHQLIntersection() throws Exception {
		delegate.testHQLIntersection();
	}

	@Test
	public void testHQLSymDifference() throws Exception {
		delegate.testHQLSymDifference();
	}

	@Test
	public void testHQLUnion() throws Exception {
		delegate.testHQLUnion();
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestOracleSpatialQueries.class);
	}

	@Test
	public void testCrosses() throws Exception {
		String sqlTemplate = "select count(*) from $table$ where geom is not null and mdsys.OGC_CROSS(mdsys.st_geometry.from_sdo_geom(geom), mdsys.ogc_polygonfromtext(?, 31370)) = 1";
		delegate.testCrosses(sqlTemplate);
	}

	@Test
	public void testDisjoint() throws Exception {
		String sqlTemplate = "select count(*) from $table$ where geom is not null and mdsys.OGC_DISJOINT(mdsys.st_geometry.from_sdo_geom(geom), mdsys.ogc_polygonfromtext(?, 31370)) = 1";
		delegate.testDisjoint(sqlTemplate);
	}

	@Test
	public void testEquals() throws Exception {
		String sqlTemplate = "select count(*) from $table$ where geom is not null and mdsys.OGC_EQUALS(mdsys.st_geometry.from_sdo_geom(geom), mdsys.ogc_polygonfromtext(?, 31370)) = 1";
		delegate.testEquals(sqlTemplate);
	}

	@Test
	public void testHQLAsBinary() throws Exception {
		delegate.testHQLAsBinary();
	}

	@Test
	public void testIntersects() throws Exception {
		String sqlTemplate = "select count(*) from $table$ where geom is not null and mdsys.OGC_INTERSECTS(mdsys.st_geometry.from_sdo_geom(geom), mdsys.ogc_polygonfromtext(?, 31370)) = 1";
		delegate.testIntersects(sqlTemplate);
	}

	@Test
	public void testOverlaps() throws Exception {
		String sqlTemplate = "select count(*) from $table$ where geom is not null and mdsys.OGC_OVERLAP(mdsys.st_geometry.from_sdo_geom(geom), mdsys.ogc_polygonfromtext(?, 31370)) = 1";
		delegate.testOverlaps(sqlTemplate);
	}

	@Test
	public void testTouches() throws Exception {
		String sqlTemplate = "select count(*) from $table$ where geom is not null and mdsys.OGC_TOUCH(mdsys.st_geometry.from_sdo_geom(geom), mdsys.ogc_polygonfromtext(?, 31370)) = 1";
		delegate.testTouches(sqlTemplate);
	}

	@Test
	public void testWithin() throws Exception {
		String sqlTemplate = "select count(*) from $table$ where geom is not null and mdsys.OGC_WITHIN(mdsys.st_geometry.from_sdo_geom(geom), mdsys.ogc_polygonfromtext(?, 31370)) = 1";
		delegate.testWithin(sqlTemplate);
	}

}
