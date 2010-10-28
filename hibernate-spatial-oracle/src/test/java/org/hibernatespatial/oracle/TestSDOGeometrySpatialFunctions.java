///*
// * $Id:$
// *
// * This file is part of Hibernate Spatial, an extension to the
// * hibernate ORM solution for geographic data.
// *
// * Copyright © 2007-2010 Geovise BVBA
// *
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public
// * License as published by the Free Software Foundation; either
// * version 2.1 of the License, or (at your option) any later version.
// *
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with this library; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// *
// * For more information, visit: http://www.hibernatespatial.org/
// */
//
//package org.hibernatespatial.oracle;
//
//import org.hibernatespatial.test.TestData;
//import org.hibernatespatial.test.TestSpatialFunctions;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.sql.SQLException;
//import java.util.Map;
//
///**
// * @author Karel Maesen, Geovise BVBA
// */
//public class TestSDOGeometrySpatialFunctions {
//
//    private final static SDODataSourceUtils dataSourceUtils = new SDODataSourceUtils("hibernate-spatial-oracle10g-test.properties");
//    private SDOGeometryExpectationsFactory expected;
//    private TestSpatialFunctions delegate;
//
//
//    @BeforeClass
//    public static void beforeClass() throws Exception {
//        dataSourceUtils.deleteTestData();
//        TestData testData = TestData.fromFile("test-sdo-geometry-data-set-2D.xml", new SDOTestDataReader());
//        dataSourceUtils.insertTestData(testData);
//        TestSpatialFunctions.setUpBeforeClass();
//    }
//
//    public TestSDOGeometrySpatialFunctions() {
//
//
//        //drop index, set geometry metadata to 2D
//        try {
//            dataSourceUtils.deleteTestData();
//            dataSourceUtils.dropIndex();
//        } catch (SQLException e) {
//            // ignore if this fails
//        }
//
//        try {
//            dataSourceUtils.setGeomMetaDataTo2D();
//            dataSourceUtils.createIndex();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        expected = new SDOGeometryExpectationsFactory();
//        delegate = new TestSpatialFunctions(expected);
//    }
//
//
//    @Test
//    public void test_dimension() throws SQLException {
//        delegate.test_dimension();
//    }
//
//    @Test
//    public void test_astext() throws SQLException {
//        delegate.test_astext();
//    }
//
//    @Test
//    public void test_asbinary() throws SQLException {
//        delegate.test_asbinary();
//    }
//
//    @Test
//    public void test_geometrytype() throws SQLException {
//        delegate.test_geometrytype();
//    }
//
//    @Test
//    public void test_srid() throws SQLException {
//        delegate.test_srid();
//    }
//
//    //IsSimple can't be tested in the usual way because Oracle10g return null for false,
//    // so we create a custom test case
//
//    @Test
//    public void test_issimple() throws SQLException {
//        Map<Integer, Boolean> dbexpected = expected.getIsSimple();
//        String hql = "SELECT id, issimple(geom) from GeomEntity where issimple(geom) = 1";
//        delegate.retrieveHQLResultsAndCompare(dbexpected, hql);
//    }
//
//    @Test
//    public void test_isempty() throws SQLException {
//        delegate.test_isempty();
//    }
//
//    @Test
//    public void test_boundary() throws SQLException {
//        delegate.test_boundary();
//    }
//
//    @Test
//    public void test_envelope() throws SQLException {
//        delegate.test_envelope();
//    }
//
//    @Test
//    public void test_within() throws SQLException {
//        delegate.test_within();
//    }
//
//    @Test
//    public void test_equals() throws SQLException {
//        delegate.test_equals();
//    }
//
//    @Test
//    public void test_crosses() throws SQLException {
//        delegate.test_crosses();
//    }
//
//    @Test
//    public void test_contains() throws SQLException {
//        delegate.test_contains();
//    }
//
//    @Test
//    public void test_disjoint() throws SQLException {
//        delegate.test_disjoint();
//    }
//
//    @Test
//    public void test_intersects() throws SQLException {
//        delegate.test_intersects();
//    }
//
//    @Test
//    public void test_overlaps() throws SQLException {
//        delegate.test_overlaps();
//    }
//
//    @Test
//    public void test_touches() throws SQLException {
//        delegate.test_touches();
//    }
//
//    @Test
//    public void test_relate() throws SQLException {
//        delegate.test_relate();
//    }
//
//    @Test
//    public void test_distance() throws SQLException {
//        delegate.test_distance();
//    }
//
//    @Test
//    public void test_buffer() throws SQLException {
//        delegate.test_buffer();
//    }
//
//    @Test
//    public void test_convexhull() throws SQLException {
//        delegate.test_convexhull();
//    }
//
//    @Test
//    public void test_intersection() throws SQLException {
//        delegate.test_intersection();
//    }
//
//    @Test
//    public void test_difference() throws SQLException {
//        delegate.test_difference();
//    }
//
//    @Test
//    public void test_symdifference() throws SQLException {
//        delegate.test_symdifference();
//    }
//
//    @Test
//    public void test_geomunion() throws SQLException {
//        delegate.test_geomunion();
//    }
//}
