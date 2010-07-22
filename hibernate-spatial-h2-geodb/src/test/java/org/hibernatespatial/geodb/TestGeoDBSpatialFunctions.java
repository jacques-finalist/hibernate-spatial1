/*
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright 2010 Geodan IT b.v.
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

package org.hibernatespatial.geodb;

import java.io.File;
import java.sql.SQLException;

import org.hibernatespatial.test.TestData;
import org.hibernatespatial.test.TestSpatialFunctions;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @Author Jan Boonen, Geodan IT b.v.
 */
public class TestGeoDBSpatialFunctions {

	private static GeoDBDataSourceUtils dataSourceUtils;

	private GeoDBExpectationsFactory expected;

	private TestSpatialFunctions delegate;

	@BeforeClass
	public static void beforeClass() throws Exception {
		File schemaFile = new File(TestGeoDBSpatialFunctions.class.getClass()
				.getResource("/create-table-geomtest.sql").toURI());
		dataSourceUtils = new GeoDBDataSourceUtils(
				"hibernate-spatial-geodb-test.properties",
				new GeoDBExpressionTemplate(), schemaFile);
		TestData testData = TestData.fromFile("test-geodb-data-set.xml");
		dataSourceUtils.insertTestData(testData);
		TestSpatialFunctions.setUpBeforeClass();
	}

	public TestGeoDBSpatialFunctions() {
		expected = new GeoDBExpectationsFactory();
		delegate = new TestSpatialFunctions(expected);
	}

	// NOT TESTED BECAUSE CURRENTLY NOT IMPLEMENT IN GEODB

	public void test_dimension() throws SQLException {
		delegate.test_dimension();
	}

	@Test
	public void test_astext() throws SQLException {
		delegate.test_astext();
	}

	@Test
	public void test_asbinary() throws SQLException {
		delegate.test_asbinary();
	}

	// NOT TESTED BECAUSE CURRENTLY NOT IMPLEMENT IN GEODB

	public void test_geometrytype() throws SQLException {
		delegate.test_geometrytype();
	}

	@Test
	public void test_srid() throws SQLException {
		delegate.test_srid();
	}

	@Test
	public void test_issimple() throws SQLException {
		delegate.test_issimple();
	}

	@Test
	public void test_isempty() throws SQLException {
		delegate.test_isempty();
	}

	// NOT TESTED BECAUSE CURRENTLY NOT IMPLEMENT IN GEODB

	public void test_boundary() throws SQLException {
		delegate.test_boundary();
	}

	@Test
	public void test_envelope() throws SQLException {
		delegate.test_envelope();
	}

	@Test
	public void test_within() throws SQLException {
		delegate.test_within();
	}

	@Test
	public void test_equals() throws SQLException {
		delegate.test_equals();
	}

	@Test
	public void test_crosses() throws SQLException {
		delegate.test_crosses();
	}

	@Test
	public void test_contains() throws SQLException {
		delegate.test_contains();
	}

	@Test
	public void test_disjoint() throws SQLException {
		delegate.test_disjoint();
	}

	@Test
	public void test_intersects() throws SQLException {
		delegate.test_intersects();
	}

	@Test
	public void test_overlaps() throws SQLException {
		delegate.test_overlaps();
	}

	@Test
	public void test_touches() throws SQLException {
		delegate.test_touches();
	}

	// NOT TESTED BECAUSE CURRENTLY NOT IMPLEMENT IN GEODB

	public void test_relate() throws SQLException {
		delegate.test_relate();
	}

	// NOT TESTED BECAUSE CURRENTLY NOT IMPLEMENT IN GEODB

	public void test_distance() throws SQLException {
		delegate.test_distance();
	}

	@Test
	public void test_buffer() throws SQLException {
		delegate.test_buffer();
	}

	// NOT TESTED BECAUSE CURRENTLY NOT IMPLEMENT IN GEODB

	public void test_convexhull() throws SQLException {
		delegate.test_convexhull();
	}

	// NOT TESTED BECAUSE CURRENTLY NOT IMPLEMENT IN GEODB

	public void test_intersection() throws SQLException {
		delegate.test_intersection();
	}

	// NOT TESTED BECAUSE CURRENTLY NOT IMPLEMENT IN GEODB

	public void test_difference() throws SQLException {
		delegate.test_difference();
	}

	// NOT TESTED BECAUSE CURRENTLY NOT IMPLEMENT IN GEODB

	public void test_symdifference() throws SQLException {
		delegate.test_symdifference();
	}

	// NOT TESTED BECAUSE CURRENTLY NOT IMPLEMENT IN GEODB

	public void test_geomunion() throws SQLException {
		delegate.test_geomunion();
	}

}
