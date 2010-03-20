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

package org.hibernatespatial.test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test class verifies whether the <code>Geometry</code>s retrieved
 * are equal to the <code>Geometry</code>s stored.
 */
public class TestStoreRetrieve {

    private static Logger LOGGER = LoggerFactory.getLogger(TestStoreRetrieve.class);
    private final TestData testData;
    private final DataSourceUtils dataSourceUtils;
    private final SessionFactory factory;

    public TestStoreRetrieve(DataSourceUtils dataSourceUtils, TestData testData) {
        this.testData = testData;
        this.dataSourceUtils = dataSourceUtils;
        LOGGER.info("Setting up Hibernate");
        Configuration config = new Configuration();
        config.configure();
        config.addClass(GeomEntity.class);

        //configure Hibernate Spatial based on this config
        HSConfiguration hsc = new HSConfiguration();
        hsc.configure(config);
        HBSpatialExtension.setConfiguration(hsc);

        // build the session factory
        factory = config.buildSessionFactory();

        LOGGER.info("Hibernate set-up complete.");


    }

    public void setUp() throws SQLException {
        dataSourceUtils.deleteTestData();
    }

    public void test_load_retrieve() throws ParseException {
        Map<Integer, GeomEntity> stored = new HashMap<Integer, GeomEntity>();
        storeTestObjects(stored);
        retrieveAndCompare(stored);
    }

    private void retrieveAndCompare(Map<Integer, GeomEntity> stored) {
        try {
            Session session = factory.getCurrentSession();
            session.beginTransaction();
            Criteria criteria = session.createCriteria(GeomEntity.class);
            List<GeomEntity> retrieved = criteria.list();
            assertEquals(stored.size(), retrieved.size());
            for (GeomEntity retrievedEntity : retrieved) {
                Geometry retrievedGeometry = retrievedEntity.getGeom();
                GeomEntity storedEntity = stored.get(retrievedEntity.getId());
                Geometry storedGeometry = storedEntity.getGeom();
                assertTrue(GeometryEquality.test(storedGeometry, retrievedGeometry));
            }
        } finally {
            factory.getCurrentSession().getTransaction().rollback();
        }
    }

    private void storeTestObjects(Map<Integer, GeomEntity> stored) {
        try {
            Session session = factory.getCurrentSession();
            session.beginTransaction();
            for (TestDataElement element : testData) {
                GeomEntity entity = GeomEntity.createFrom(element);
                stored.put(entity.getId(), entity);
                session.save(entity);
            }
            factory.getCurrentSession().getTransaction().commit();
        } catch (Exception e) {
            factory.getCurrentSession().getTransaction().rollback();
        }
    }


}