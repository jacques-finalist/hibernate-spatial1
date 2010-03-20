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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Criterion;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.criterion.SpatialRestrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: maesenka
 * Date: Mar 18, 2010
 * Time: 10:02:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestSpatialRestrictions {

    private static Logger LOGGER = LoggerFactory.getLogger(TestSpatialRestrictions.class);
    private final AbstractExpectationsFactory expectationsFactory;
    private static SessionFactory factory;

    public TestSpatialRestrictions(AbstractExpectationsFactory expectationsFactory) {
        this.expectationsFactory = expectationsFactory;
    }


    public static void setUpBeforeClass() {
        // set up hibernate and register Spatialtest as a persistent entity
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

    public void test_within() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getWithin(expectationsFactory.getTestPolygon());
        Criterion spatialCriterion = SpatialRestrictions.within("geom", expectationsFactory.getTestPolygon());
        retrieveAndCompare(dbexpected, spatialCriterion);
    }

    public void test_filter() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getFilter(expectationsFactory.getTestPolygon());
        Criterion spatialCriterion = SpatialRestrictions.filter("geom", expectationsFactory.getTestPolygon());
        retrieveAndCompare(dbexpected, spatialCriterion);
    }

    public void test_contains() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getContains(expectationsFactory.getTestPolygon());
        Criterion spatialCriterion = SpatialRestrictions.contains("geom", expectationsFactory.getTestPolygon());
        retrieveAndCompare(dbexpected, spatialCriterion);
    }

    public void test_crosses() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getCrosses(expectationsFactory.getTestPolygon());
        Criterion spatialCriterion = SpatialRestrictions.crosses("geom", expectationsFactory.getTestPolygon());
        retrieveAndCompare(dbexpected, spatialCriterion);
    }

    public void test_touches() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getTouches(expectationsFactory.getTestPolygon());
        Criterion spatialCriterion = SpatialRestrictions.touches("geom", expectationsFactory.getTestPolygon());
        retrieveAndCompare(dbexpected, spatialCriterion);
    }

    public void test_disjoint() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getDisjoint(expectationsFactory.getTestPolygon());
        Criterion spatialCriterion = SpatialRestrictions.disjoint("geom", expectationsFactory.getTestPolygon());
        retrieveAndCompare(dbexpected, spatialCriterion);
    }

    public void test_eq() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getEquals(expectationsFactory.getTestPolygon());
        Criterion spatialCriterion = SpatialRestrictions.eq("geom", expectationsFactory.getTestPolygon());
        retrieveAndCompare(dbexpected, spatialCriterion);
    }

    public void test_intersects() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getIntersects(expectationsFactory.getTestPolygon());
        Criterion spatialCriterion = SpatialRestrictions.intersects("geom", expectationsFactory.getTestPolygon());
        retrieveAndCompare(dbexpected, spatialCriterion);
    }

    public void test_overlaps() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getOverlaps(expectationsFactory.getTestPolygon());
        Criterion spatialCriterion = SpatialRestrictions.overlaps("geom", expectationsFactory.getTestPolygon());
        retrieveAndCompare(dbexpected, spatialCriterion);
    }

    private void retrieveAndCompare(Map<Integer, Boolean> dbexpected, Criterion spatialCriterion) {
        try {
            Session session = factory.getCurrentSession();
            session.beginTransaction();
            Criteria criteria = session.createCriteria(GeomEntity.class);
            criteria.add(spatialCriterion);
            compare(dbexpected, criteria.list());
        } finally {
            //rollback because we have only read-only access.
            factory.getCurrentSession().getTransaction().rollback();
        }
    }

    //TODO -- clean this up!

    private void compare(Map<Integer, Boolean> dbexpected, List list) {
        int cnt = 0;
        for (Integer id : dbexpected.keySet()) {
            if (dbexpected.get(id)) {
                cnt++;
                if (!findInList(id, (List<GeomEntity>) list))
                    fail(String.format("Expected object with id= %d, but not found in result", id));
            }
        }
        assertEquals(cnt, list.size());
        LOGGER.info(String.format("Found %d objects within test polygon.", cnt));
    }

    private boolean findInList(Integer id, List<GeomEntity> list) {
        for (GeomEntity entity : list) {
            if (entity.getId() == id) return true;
        }
        return false;
    }
}
