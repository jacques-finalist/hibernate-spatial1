/*
 * $Id:$
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

package org.hibernatespatial.postgis;

import com.vividsolutions.jts.io.ParseException;
import org.hibernatespatial.test.DataSourceUtils;
import org.hibernatespatial.test.TestData;
import org.hibernatespatial.test.TestStoreRetrieve;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

/**
 * This test class verifies whether the <code>Geometry</code>s retrieved are equal to the <code>Geometry</code>s stored.
 *
 * @Author Karel Maesen, Geovise BVBA
 */
public class TestPostgisStoreRetrieve {

    private final TestStoreRetrieve delegate;

    public TestPostgisStoreRetrieve() {
        DataSourceUtils dataSourceUtils = new DataSourceUtils("hibernate-spatial-postgis-test.properties", new PostgisExpressionTemplate());
        TestData testData = dataSourceUtils.getTestData();
        delegate = new TestStoreRetrieve(dataSourceUtils, testData);
    }

    @Before
    public void setUp() throws SQLException {
        delegate.setUp();
    }

    @Test
    public void test_store_retrieve() throws ParseException {
        delegate.test_store_retrieve();
    }

    @Test
    public void test_store_retrieve_null() {
        delegate.test_store_retrieve_null_geometry();
    }
}
