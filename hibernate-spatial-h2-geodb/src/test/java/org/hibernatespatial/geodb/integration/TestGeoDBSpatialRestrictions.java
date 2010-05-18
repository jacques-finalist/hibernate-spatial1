/*
 * $Id:$
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

package org.hibernatespatial.geodb.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.junit.Assert.assertTrue;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/geodb-test-context.xml"})
public class TestGeoDBSpatialRestrictions {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Resource
    private HibernateTemplate hibernateTemplate;

    @Resource
    private JdbcTemplate jdbcTemplate;

    //TODO Remove when at least one test method is implemented
    @Test
    public void testSkeleton() {
        assertTrue(true);
    }

    public void testContains() {

    }

    public void testCrosses() {

    }

    public void testDisjoint() {

    }

    public void testEquals() {

    }

    public void testFilter() {

    }

    public void testIntersects() {

    }

    public void testOverlaps() {

    }

    public void testTouches() {

    }

    public void testWithin() {

    }
}
