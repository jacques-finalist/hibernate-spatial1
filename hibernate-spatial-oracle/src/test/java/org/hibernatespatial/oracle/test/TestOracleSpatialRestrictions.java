package org.hibernatespatial.oracle.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.oracle.criterion.OracleSpatialAggregate;
import org.hibernatespatial.oracle.criterion.OracleSpatialProjections;
import org.hibernatespatial.oracle.criterion.OracleSpatialRestrictions;
import org.hibernatespatial.oracle.criterion.RelationshipMask;
import org.hibernatespatial.test.model.LineStringEntity;
import org.hibernatespatial.test.model.PointEntity;
import org.hibernatespatial.test.model.PolygonEntity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

public class TestOracleSpatialRestrictions {
	
	private final static Logger logger = Logger.getLogger(TestOracleSpatialRestrictions.class);

	private final static String DBURL = "jdbc:oracle:thin:@localhost/xe";

	private final static String DBNAME = "hbs";

	private final static String DBPASSWD = "hbs";

	private static Connection conn;
	
	private static SessionFactory sessionFactory=null;
	
	private static Geometry geom2;

	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(DBURL, DBNAME, DBPASSWD);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InstantiationError();
		}

	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// set up hibernate and register persistent entities
		Configuration config = new Configuration();
		config.configure();
		config.addClass(PointEntity.class);		
		config.addClass(PolygonEntity.class);
		config.addClass(LineStringEntity.class);
		sessionFactory = config.buildSessionFactory();
		
		//create the geom2 object
		GeometryFactory gf = new GeometryFactory();
		LinearRing shell = gf.createLinearRing(new Coordinate[]{
				new Coordinate(0, 0),
				new Coordinate(25000, 0),
				new Coordinate(25000, 25000),
				new Coordinate(0, 25000),
				new Coordinate(0,0)
			});
		geom2 = gf.createPolygon(shell, null);
		geom2.setSRID(31370);
		}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		sessionFactory.close();
		try {
			conn.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void voidTestSDOFilter(){
		Session session = sessionFactory.openSession();
		
		Criteria crit = session.createCriteria(PointEntity.class);
		crit.add(OracleSpatialRestrictions.SDOFilter("geometry", geom2, null));
		List result = crit.list();
		System.out.println("filter result:" + result.size());
		session.close();
	}
	
	@Test
	public void voidTestSDONN(){
		Session session = sessionFactory.openSession();
		
		Criteria crit = session.createCriteria(PointEntity.class);
		crit.add(OracleSpatialRestrictions.SDONN("geometry", geom2, 1000., 5, null));
		List result = crit.list();
		System.out.println("filter result:" + result.size());
		session.close();
	}
	
	@Test
	public void voidTestSDORelate(){
		Session session = sessionFactory.openSession();
		
		Criteria crit = session.createCriteria(PointEntity.class);
		//first with one mask
		RelationshipMask[] masks = new RelationshipMask[]{RelationshipMask.INSIDE};
		crit.add(OracleSpatialRestrictions.SDORelate("geometry", geom2, masks,null, null));
		List result = crit.list();
		System.out.println("filter result:" + result.size());
		
		crit = session.createCriteria(PointEntity.class);
		masks = new RelationshipMask[2];
		masks[0] = RelationshipMask.INSIDE;
		masks[1] = RelationshipMask.ON;
		crit.add(OracleSpatialRestrictions.SDORelate("geometry", geom2, masks,null, null));
		result = crit.list();
		System.out.println("filter result:" + result.size());
		
		session.close();
	}	
	
	
	private void testProjection(Class entityClass, int projection, String sql) throws Exception {
		Session session = null;
		try {
			// apply the projection using Hibernate Criteria
			session = sessionFactory.openSession();
			Criteria testCriteria = session.createCriteria(entityClass);
			testCriteria.setProjection(OracleSpatialProjections.projection(projection, "geometry"));

			List results = testCriteria.list();
			Geometry g = (Geometry)results.get(0);
			double expected;
			double result;
			if (entityClass  == PolygonEntity.class){
				result = g.getArea();	
			} else {
				result = g.getLength();
			}
			
			//using HQL
			String hql = "select ";
			switch(projection){
			case OracleSpatialAggregate.CENTROID:
					hql += "centroid";
					break;
			case OracleSpatialAggregate.CONVEXHULL:
					hql += "aggr_convexhull";
					break;
			case OracleSpatialAggregate.CONCAT_LINES:
					hql += "concat_lines";
					break;
			case OracleSpatialAggregate.UNION:
					hql += "aggr_union";
					break;
			}
			hql += "(geometry) from " + entityClass.getSimpleName();
			logger.debug("HQL is:" + hql);
			Query q = session.createQuery(hql);
			Geometry g2 = (Geometry)q.list().get(0);
			assertTrue(g.equals(g2));
			
			// get the same results using JDBC - SQL directly;
			logger.debug("Test SQL:" + sql);
			PreparedStatement stmt = conn.prepareStatement(sql);
		
			ResultSet rs = stmt.executeQuery();
			rs.next();
			expected = rs.getDouble(1);
			// test whether they give the same results
			logger.info("checking for equality  - expected: " + expected + ", result: + " + result);
			assertEquals(expected, result, .1);
		} finally {
			if (session != null)
				session.close();
		}		
		
	}
	
	@Test
	public void testCentroid() throws Exception{
		String sql = "select SDO_GEOM.SDO_AREA(SDO_AGGR_CENTROID(SDOAGGRTYPE(geom, 0.005)), .001) from polygontest";
		testProjection(PolygonEntity.class, OracleSpatialAggregate.CENTROID, sql);
	}
	
	@Test
	public void testUnion() throws Exception {
		String sql = "select SDO_GEOM.SDO_AREA(SDO_AGGR_UNION(SDOAGGRTYPE(geom, 0.005)), .001) from polygontest";
		testProjection(PolygonEntity.class, OracleSpatialAggregate.UNION, sql);
	}
	
	@Test
	public void testConcatlines() throws Exception {
		String sql = "select SDO_GEOM.SDO_LENGTH(SDO_AGGR_CONCAT_LINES(geom), 0.001) from linestringtest";
		testProjection(LineStringEntity.class, OracleSpatialAggregate.CONCAT_LINES, sql);
	}
	
	@Test
	public void testConvexHull() throws Exception {
		String sql = "select SDO_GEOM.SDO_AREA(SDO_AGGR_CONVEXHULL(SDOAGGRTYPE(geom, 0.005)), .001) from polygontest";
		testProjection(PolygonEntity.class, OracleSpatialAggregate.CONVEXHULL, sql);
	}
	
	
}
