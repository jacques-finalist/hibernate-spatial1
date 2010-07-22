/*
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2007-2010 Geovise BVBA
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
import org.hibernatespatial.test.TestSpatialRestrictions;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @Author Jan Boonen, Geodan IT b.v.
 */
public class TestGeoDBSpatialRestrictions {

	private static GeoDBDataSourceUtils dataSourceUtils;

	private GeoDBNoSRIDExpectationsFactory expected;

	private TestSpatialRestrictions delegate;

	@BeforeClass
	public static void beforeClass() throws Exception {
		File schemaFile = new File(TestGeoDBSpatialFunctions.class.getClass()
				.getResource("/create-table-geomtest.sql").toURI());
		dataSourceUtils = new GeoDBDataSourceUtils(
				"hibernate-spatial-geodb-test.properties",
				new GeoDBExpressionTemplate(), schemaFile);
		TestData testData = TestData.fromFile("test-geodb-data-set.xml");
		dataSourceUtils.insertTestData(testData);

		TestSpatialRestrictions.setUpBeforeClass();
	}

	public TestGeoDBSpatialRestrictions() {
		expected = new GeoDBNoSRIDExpectationsFactory();
		delegate = new TestSpatialRestrictions(expected);
	}

	@Test
	public void test_within() throws SQLException {
		delegate.test_within();
	}

	// NOT TESTED BECAUSE CURRENTLY NOT IMPLEMENT IN GEODB
	
	public void test_filter() throws SQLException {
		delegate.test_filter();
	}

	@Test
	public void test_contains() throws SQLException {
		delegate.test_contains();
	}

	@Test
	public void test_crosses() throws SQLException {
		delegate.test_crosses();
	}

	@Test
	public void test_touches() throws SQLException {
		delegate.test_touches();
	}

	@Test
	public void test_disjoint() throws SQLException {
		delegate.test_disjoint();
	}

	@Test
	public void test_eq() throws SQLException {
		delegate.test_eq();
	}

	@Test
	public void test_intersects() throws SQLException {
		delegate.test_intersects();
	}

	@Test
	public void test_overlaps() throws SQLException {
		delegate.test_overlaps();
	}

}
