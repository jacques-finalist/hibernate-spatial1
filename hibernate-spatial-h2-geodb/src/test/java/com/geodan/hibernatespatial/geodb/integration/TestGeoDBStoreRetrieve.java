package com.geodan.hibernatespatial.geodb.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import geodb.GeoDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.geodan.hibernatespatial.test.TestDataElement;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/geodb-test-context.xml" })
public class TestGeoDBStoreRetrieve {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	private static final Long MAX_RESULTS = 17L;

	@Resource
	private HibernateTemplate hibernateTemplate;

	@Resource
	private JdbcTemplate jdbcTemplate;
	
	private static Point point;
	
	private static LineString line;
	
	private static Polygon polygon;
	
	@BeforeClass
	public static void setup() throws ParseException {
		double xCoord = 52.25;
		double yCoord = 2.53;
		point = (Point) new WKTReader().read("POINT(" + xCoord + " " + yCoord + ")");
		
	}

	@Test
	public void testFindAll() {
		List<TestDataElement> data = hibernateTemplate.loadAll(TestDataElement.class);
		assertNotNull(data);
		assertNotNull(data.get(0));
	}

	@Test
	public void testCount() {
		Long count = (Long) hibernateTemplate.getSessionFactory().getCurrentSession().createQuery("select count(t) from TestDataElement t").uniqueResult();
		LOGGER.debug("Found {} records.", count);
		assertEquals(MAX_RESULTS, count);
	}
	
	@Test
	public void testFindById() {
		TestDataElement data = hibernateTemplate.load(TestDataElement.class, 2);
		assertNotNull(data);
		assertEquals(2, data.getId());
		assertEquals("POINT", data.getType());
		assertTrue(data.getGeom().equals(point));
	}

	@Test
	public void testInsertPoint() throws ParseException {
		TestDataElement record = new TestDataElement();
		record.setId(9999);
		record.setType("POINT");
		record.setGeom(point);
		hibernateTemplate.saveOrUpdate(record);
		hibernateTemplate.flush();

		TestDataElement jdbcRecord = (TestDataElement) jdbcTemplate.queryForObject("select * from GEOMTEST where id = 9999", new TestDataElementRowMapper());
		
		assertEquals(MAX_RESULTS + 1, hibernateTemplate.loadAll(TestDataElement.class).size());
		assertEquals(9999, jdbcRecord.getId());
		assertEquals("POINT", jdbcRecord.getType());
		assertTrue(jdbcRecord.getGeom().equals(point));
	}
	
	public void testInsertMultiPoint() {
		
	}

	public void testInsertLineString() {
		
	}
	
	public void testInsertMultiLineString() {
		
	}
	
	public void testInsertPolygon() {
		
		
	}
	
	public void testInsertMultiPolygon() {
		
	}
	
	public void testInsertGeometryCollection() {
		
	}
	
	public void testUpdateRecord() {
		
	}
	
	public void testDeleteRecord() {
		
	}
	
	private class TestDataElementRowMapper implements ParameterizedRowMapper<TestDataElement> {

		public TestDataElement mapRow(ResultSet rs, int rowNum) throws SQLException {
			return mapTestDataElement(rs);
		}
		
	}
	
	private TestDataElement mapTestDataElement(ResultSet rs) throws SQLException {
		TestDataElement record = new TestDataElement();
		record.setId(rs.getInt("ID"));
		record.setType(rs.getString("TYPE"));
		record.setGeom(GeoDB.gFromWKB(rs.getBytes("GEOM")));
		return record;
	}
}
