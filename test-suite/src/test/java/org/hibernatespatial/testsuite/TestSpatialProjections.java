/*
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2007-2011 Geovise BVBA
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

package org.hibernatespatial.testsuite;

import com.vividsolutions.jts.geom.Polygon;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;


/**
 * Tests the spatial projections.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 10/18/11
 */
public class TestSpatialProjections extends SpatialFunctionalTestCase {

    private static Logger LOGGER = LoggerFactory.getLogger(TestSpatialProjections.class);

    public TestSpatialProjections(String string) {
        super(string);
    }

    public void prepareTest() {
        super.prepareTest();
        try {
            dataSourceUtils.insertTestData(testData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Logger getLogger() {
        return LOGGER;
    }

    public void testProjections() throws Exception {
        extent();
    }

    public void extent() throws SQLException {
        //This is now guarded by explictly checking for dialect.
        //SpatialDialect should have method so that we can ask explictly if this is supported.
        if (!getDialect().getClass().getSimpleName().equals("PostgisDialect")) return;

        Session session = null;
        Transaction tx = null;
        try {
            session = openSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("select extent(geom) from GeomEntity");
            List result = query.list();
            Polygon pg = (Polygon)result.get(0);
            assertNotNull(pg);
            assertEquals(0.0d, pg.getEnvelopeInternal().getMinX(), 1E-6);
            assertEquals(0.0d, pg.getEnvelopeInternal().getMinY(),1E-6);
            assertEquals(150000.0d, pg.getEnvelopeInternal().getMaxX(),1E-6);
            assertEquals(200000.0d, pg.getEnvelopeInternal().getMaxY(),1E-6);
        } finally {
            if (tx != null) tx.rollback();
            if (session != null) session.close();
        }

    }
}
