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
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This test class verifies whether the <code>Geometry</code>s retrieved
 * are equal to the <code>Geometry</code>s stored.
 */
public class TestStoreRetrieve {

    private static Logger LOGGER = LoggerFactory.getLogger(TestStoreRetrieve.class);
    private final TestData testData;
    private final DataSourceUtils dataSourceUtils;
    private final GeometryEquality geometryEquality;
    private final SessionFactory factory;

    public TestStoreRetrieve(DataSourceUtils dataSourceUtils, TestData testData, GeometryEquality geometryEquality) {
        this.geometryEquality = geometryEquality;
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

    public TestStoreRetrieve(DataSourceUtils dataSourceUtils, TestData testData) {
        this(dataSourceUtils, testData, new GeometryEquality());
    }

    public void setUp() throws SQLException {
        dataSourceUtils.deleteTestData();
    }

    public void test_store_retrieve() throws ParseException {
        Map<Integer, GeomEntity> stored = new HashMap<Integer, GeomEntity>();
        storeTestObjects(stored);
        retrieveAndCompare(stored);
    }

    public void test_store_retrieve_null_geometry() {
        storeNullGeometry();
        retrieveNullGeometry();
    }

    private void retrieveAndCompare(Map<Integer, GeomEntity> stored) {
        int id = -1;
        try {
            Session session = factory.getCurrentSession();
            session.beginTransaction();
            for (GeomEntity storedEntity : stored.values()) {
                id = storedEntity.getId();
                GeomEntity retrievedEntity = (GeomEntity) session.get(GeomEntity.class, id);
                Geometry retrievedGeometry = retrievedEntity.getGeom();
                Geometry storedGeometry = storedEntity.getGeom();
                String msg = createFailureMessage(storedEntity.getId(), storedGeometry, retrievedGeometry);
                assertTrue(msg, geometryEquality.test(storedGeometry, retrievedGeometry));
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure on case: %d", id), e);
        }
        finally {
            factory.getCurrentSession().getTransaction().rollback();
        }
    }

    private String createFailureMessage(int id, Geometry storedGeometry, Geometry retrievedGeometry) {
        String expectedText = (storedGeometry != null ? storedGeometry.toText() : "NULL");
        String retrievedText = (retrievedGeometry != null ? retrievedGeometry.toText() : "NULL");
        return String.format("Equality test failed for %d.\nExpected: %s\nReceived:%s", id, expectedText, retrievedText);
    }

    private void storeTestObjects(Map<Integer, GeomEntity> stored) {
        Session session = null;
        Transaction tx = null;
        int id = -1;
        try {
            session = factory.openSession();
            // Every test instance is committed seperately
            // to improve feedback in case of test failure
            for (TestDataElement element : testData) {
                id = element.id;
                tx = session.beginTransaction();
                GeomEntity entity = GeomEntity.createFrom(element);
                stored.put(entity.getId(), entity);
                session.save(entity);
                tx.commit();
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed storing test object with id:" + id, e);
        } finally {
            if (session != null) session.close();
        }
    }

    private void storeNullGeometry() {
        GeomEntity entity = null;
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            entity = new GeomEntity();
            entity.setId(1);
            entity.setType("NULL OBJECT");
            session.save(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed storing test object with id:" + entity.getId(), e);
        } finally {
            if (session != null) session.close();
        }
    }


    private void retrieveNullGeometry() {
        try {
            Session session = factory.getCurrentSession();
            session.beginTransaction();
            Criteria criteria = session.createCriteria(GeomEntity.class);
            List<GeomEntity> retrieved = criteria.list();
            assertEquals("Expected exactly one result", 1, retrieved.size());
            GeomEntity entity = retrieved.get(0);
            assertNull(entity.getGeom());
        } finally {
            factory.getCurrentSession().getTransaction().rollback();
        }
    }
}