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

package org.hibernatespatial.geodb.integration;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.hibernate.Criteria;
import org.hibernatespatial.criterion.SpatialRestrictions;
import org.hibernatespatial.geodb.TestDataElement;
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
import java.util.List;

import static org.junit.Assert.assertNotNull;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/geodb-test-context.xml"})
@SuppressWarnings("restriction")
public class TestGeoDBSpatialFunctions {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    
	@Resource
    private HibernateTemplate hibernateTemplate;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testWithinQuery() throws ParseException {
        Geometry polygon = new WKTReader(new GeometryFactory(new PrecisionModel(), 4326)).read("POLYGON((5.5 52.0, 6.0 52.0, 6.0 53.0, 5.5 53.0, 5.5 52.0))");
        Criteria testCriteria = hibernateTemplate.getSessionFactory().openSession().createCriteria(TestDataElement.class);
        testCriteria.add(SpatialRestrictions.within("geom", polygon));
        List<TestDataElement> results = testCriteria.list();

        assertNotNull(results);
    }

    @Test
    public void testOverlapsQuery() throws ParseException {
        Geometry polygon = new WKTReader(new GeometryFactory(new PrecisionModel(), 4326)).read("POLYGON((5.5 52.0, 6.0 52.0, 6.0 53.0, 5.5 53.0, 5.5 52.0))");
        Criteria testCriteria = hibernateTemplate.getSessionFactory().openSession().createCriteria(TestDataElement.class);
        testCriteria.add(SpatialRestrictions.intersects("geom", polygon));
        List<TestDataElement> results = testCriteria.list();

        assertNotNull(results);
    }
}
