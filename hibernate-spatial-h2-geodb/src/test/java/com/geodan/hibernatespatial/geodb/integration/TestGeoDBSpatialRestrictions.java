package com.geodan.hibernatespatial.geodb.integration;

import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/geodb-test-context.xml" })
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
