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
package org.hibernatespatial.postgis.test;

import java.sql.Connection;
import java.sql.DriverManager;

import junit.framework.JUnit4TestAdapter;

import org.hibernatespatial.test.TestSpatialQueries;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPostgisSpatialQueries {

	private final static String DBNAME = "test";

	private static Connection conn;

	private static TestSpatialQueries delegate;

	static {
		String url = "jdbc:postgresql://localhost:5432/" + DBNAME;
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(url, "postgres", "");
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
	public void testHQLAsBinary() throws Exception {
		delegate.testHQLAsBinary();
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
		String sqlString = "select count(*) from $table$ where intersects(geomfromtext(?, 31370), geom)";
		delegate.testHQLIntersectsLineString(sqlString);
	}

	@Test
	public void testHQLIsEmpty() throws Exception {
		String sql = "select count(*) from $table$ where geom is not null and isempty(geom)";
		delegate.testHQLIsEmpty(sql);
	}

	@Test
	public void testHQLIsSimple() throws Exception {
		String sql = "select  count(*) from $table$ where geom is not null and issimple(geom)";
		delegate.testHQLIsSimple(sql);
	}

	@Test
	public void testHQLOverlaps() throws Exception {
		String sql = "select count(*) from $table$ where overlaps(geomfromtext(?, 31370), geom) and geom is not null";
		delegate.testHQLOverlaps(sql);
	}

	@Test
	public void testHQLSRID() throws Exception {
		delegate.testHQLSRID();
	}
	
	@Test
	public void testExtent() throws Exception {
		String sql = "select area(extent(geom)) from $table$";
		delegate.testExtent(sql);
	}
	
	@Test
	public void testHQLExtent() throws Exception {
		String sql = "select AREA(extent(geom)) from $table$";
		delegate.testHQLExtent(sql);
	}

	@Test
	public void testFiltering() throws Exception {
		String sql = "select count(*) from $table$ where geom && geomFromText(?,31370)";
		delegate.testFiltering(sql);
	}

	@Test
	public void testContains() throws Exception {
		String sql = "select count(*) from $table$ where geom && geomFromText(?,31370) and contains(geom, geomFromText(?, 31370))";
		delegate.testContains(sql);
	}

	@Test
	public void testCrosses() throws Exception {
		String sql = "select count(*) from $table$ where geom && geomFromText(?,31370) and crosses(geom, geomFromText(?, 31370))";
		delegate.testCrosses(sql);
	}

	@Test
	public void testDisjoint() throws Exception {
		String sql = "select count(*) from $table$ where disjoint(geom, geomFromText(?, 31370))";
		delegate.testDisjoint(sql);
	}

	@Test
	public void testHQLDisjoint() throws Exception {
		String sql = "select count(*) from $table$ where disjoint(geomfromtext(?, 31370), geom) and geom is not null";
		delegate.testHQLDisjoint(sql);
	}

	@Test
	public void testEquals() throws Exception {
		String sql = "select count(*) from $table$ where geom && geomFromText(?,31370) and equals(geom, geomFromText(?, 31370))";
		delegate.testEquals(sql);
	}

	@Test
	public void testIntersects() throws Exception {
		String sql = "select count(*) from $table$ where geom && geomFromText(?,31370) and intersects(geom, geomFromText(?, 31370))";
		delegate.testIntersects(sql);
	}

	@Test
	public void testOverlaps() throws Exception {
		String sql = "select count(*) from $table$ where geom && geomFromText(?,31370) and overlaps(geom, geomFromText(?, 31370))";
		delegate.testOverlaps(sql);
	}

	@Test
	public void testTouches() throws Exception {
		String sql = "select count(*) from $table$ where geom && geomFromText(?,31370) and touches(geom, geomFromText(?, 31370))";
		delegate.testTouches(sql);
	}

	@Test
	public void testWithin() throws Exception {
		String sql = "select count(*) from $table$ where geom && geomFromText(?,31370) and within(geom, geomFromText(?, 31370))";
		delegate.testWithin(sql);
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestPostgisSpatialQueries.class);
	}

	@Test
	public void testHQLBoundary() throws Exception {
		delegate.testHQLBoundary();
	}

	@Test
	public void testHQLRelateLineString() throws Exception {
		delegate.testHQLRelateLineString();
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
	public void testHQLIntersection() throws Exception {
		delegate.testHQLIntersection();
	}

	@Test
	public void testHQLDifference() throws Exception {
		delegate.testHQLDifference();
	}

	@Test
	public void testHQLSymDifference() throws Exception {
		delegate.testHQLSymDifference();
	}

	@Test
	public void testHQLUnion() throws Exception {
		delegate.testHQLUnion();
	}

}
