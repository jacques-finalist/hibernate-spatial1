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

package org.hibernatespatial.oracle;

import com.vividsolutions.jts.io.ParseException;
import org.hibernatespatial.test.TestData;
import org.hibernatespatial.test.TestStoreRetrieve;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: maesenka
 * Date: Mar 24, 2010
 * Time: 9:53:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestSDOGeometryStoreRetrieve {

    private final TestStoreRetrieve delegate;

    public TestSDOGeometryStoreRetrieve() {
        SDODataSourceUtils dataSourceUtils = new SDODataSourceUtils("hibernate-spatial-oracle10g-test.properties");
        TestData testData = TestData.fromFile("test-sdo-geometry-data-set.xml", new SDOTestDataReader());

        //drop index, set geometry metadata to 4D
        try {
            dataSourceUtils.setGeomMetaDataTo4D();
            dataSourceUtils.dropIndex();
        } catch (SQLException e) {
            // ignore these errors (usually it's just that there is no index)
        }

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
