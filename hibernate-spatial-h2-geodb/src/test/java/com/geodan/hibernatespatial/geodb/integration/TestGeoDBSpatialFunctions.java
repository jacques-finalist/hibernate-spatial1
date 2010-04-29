package com.geodan.hibernatespatial.geodb.integration;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Criteria;
import org.hibernatespatial.criterion.SpatialRestrictions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.geodan.hibernatespatial.test.TestDataElement;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/geodb-test-context.xml" })
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
