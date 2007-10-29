/**
 * $Id$
 *
 * This file is part of Spatial Hibernate, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
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
 * For more information, visit: http://www.cadrie.com/
 */

package org.hibernatespatial.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.hibernatespatial.GeometryUserType;
import org.hibernatespatial.SpatialRelation;
import org.hibernatespatial.criterion.SpatialRestrictions;
import org.hibernatespatial.test.model.LineStringEntity;
import org.hibernatespatial.test.model.MultiLineStringEntity;
import org.hibernatespatial.test.model.PolygonEntity;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

public class TestSpatialQueries {

	private static final String MODEL_PACKAGE = "org.hibernatespatial.test.model";

	private static final Log log = LogFactory.getLog(TestSpatialQueries.class
			.getCanonicalName());

	private SessionFactory factory;

	private Connection conn;

	private Geometry jtsFilter = null;

	private String filterPolygonString = "POLYGON((0.0 0.0, 25000.0 0.0, 25000.0 25000.0, 0.0 25000.0, 0.0 0.0))";

	// TODO: complete test framework with "MultiPointEntity",
	// "MultiPolygonEntity", "PointEntity", "MultiLineStringEntity"
	// The entities and tables arrays must correspond.
	private static final String[] entities = new String[] { "LineStringEntity",
			"PolygonEntity" };

	private static final String[] tables = new String[] { "linestringtest",
			"polygontest" };

	// The unit tests for Spatial queries should
	// exercise the spatial relation expression in both select-
	// and where-clause.

	// Note: an Geometry literal passed as a parameter
	// should be passed as the second operand because
	// Oracle (native) spatial functions/operators
	// expect a spatial index to be defined on the
	// first operand.
	//

	// TODO: send message to logger when entering a test.

	/**
	 * Allow users access to the SessionFactory
	 * 
	 * @return the sessionfactory
	 */
	public SessionFactory getSessionFactory() {
		return this.factory;
	}

	public void setUpBeforeClass(Connection conn) throws SQLException,
			ClassNotFoundException, ParseException {

		if (conn == null)
			throw new RuntimeException("Empty connection passed.");

		this.conn = conn;

		// set up hibernate and register persistent entities
		Configuration config = new Configuration();
		config.configure();
		config.addClass(LineStringEntity.class);
		config.addClass(PolygonEntity.class);
		config.addClass(MultiLineStringEntity.class);

		// build the session factory
		factory = config.buildSessionFactory();

		// convert WKT string for filter to proper Geometry
		WKTReader reader = new WKTReader();
		jtsFilter = reader.read(filterPolygonString);
		jtsFilter.setSRID(31370);
	}

	public void tearDownAfterClass() {
		this.factory.close();
		try {
			this.conn.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void testHQL(String func, String sqlTemplate, int numArgs)
			throws Exception {
		for (int i = 0; i < entities.length; i++) {
			String entity = entities[i];
			String table = tables[i];
			String sqlString = sqlTemplate.replaceAll("\\$table\\$", table);
			log.info("Testing operator " + func + " for " + entity);
			testHQL(func, entity, sqlString, numArgs);
		}
	}

	private void testHQL(String f, String entity, String sqlString, int numArgs)
			throws Exception {
		Session session = null;

		if (numArgs < 1 || numArgs > 2) {
			throw new IllegalArgumentException(
					"Only one or two arguments accepted in HQL function");
		}

		String funcStr = f + "( l.geometry ";
		for (int i = numArgs; i > 1; i--) {
			funcStr += ", ?";
		}
		funcStr += ")";

		String queryStr = "select " + funcStr + " from " + entity
				+ " as l where l.geometry is not null ";

		try {
			session = factory.openSession();
			Query q = session.createQuery(queryStr);
			Type geometryType = new CustomType(GeometryUserType.class, null);
			if (numArgs > 1) {
				q.setParameter(0, jtsFilter, geometryType);
			}
			int cases = 0;
			for (Iterator it = q.list().iterator(); it.hasNext();) {
				Boolean b = (Boolean) it.next();
				if (b)
					cases++;
			}

			PreparedStatement stmt = conn.prepareStatement(sqlString);
			if (numArgs > 1) {
				stmt.setString(1, this.filterPolygonString);
			}
			ResultSet rs = stmt.executeQuery();
			rs.next();
			int expected = rs.getInt(1);
			assertEquals(expected, cases);
			log.info("num. " + f + " for table " + entity + " = " + cases);

			Query q2 = session.createQuery("from " + entity
					+ " as l where l.geometry is not null and " + funcStr
					+ " = True");
			if (numArgs > 1) {
				q2.setParameter(0, jtsFilter, geometryType);
			}
			List l = q2.list();
			assertEquals(expected, l.size());

		} catch (Exception e) {
			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	private void testRelation(int relation, Class entityClass, String sql)
			throws Exception {
		Session session = null;
		try {
			// apply the filter using Hibernate
			session = factory.openSession();

			Criteria testCriteria = session.createCriteria(entityClass);

			testCriteria.add(SpatialRestrictions.spatialRestriction(relation,
					"geometry", null, jtsFilter));

			List results = testCriteria.list();

			// get the same results using JDBC - SQL directly;
			log.debug("Test SQL:" + sql);
			PreparedStatement stmt = conn.prepareStatement(sql);
			ParameterMetaData metadata = stmt.getParameterMetaData();
			// assume that all parameters are for the filterstring
			for (int i = 1; i <= metadata.getParameterCount(); i++) {
				stmt.setString(i, this.filterPolygonString);
			}
			ResultSet rs = stmt.executeQuery();
			rs.next();
			int expected = rs.getInt(1);
			// test whether they give the same results
			assertEquals(expected, results.size());
		} finally {
			if (session != null)
				session.close();
		}

	}

	private void testRelation(int relation, String sqlTemplate)
			throws Exception {
		for (int i = 0; i < entities.length; i++) {
			String sql = sqlTemplate.replaceAll("\\$table\\$", tables[i]);
			Class entityClass = Class
					.forName(MODEL_PACKAGE + "." + entities[i]);
			testRelation(relation, entityClass, sql);
		}
	}

	public void testFiltering(String sqlTemplate) throws Exception {
		testRelation(SpatialRelation.FILTER, sqlTemplate);
	}

	public void testContains(String sqlTemplate) throws Exception {
		testRelation(SpatialRelation.CONTAINS, sqlTemplate);
	}

	public void testCrosses(String sqlTemplate) throws Exception {
		testRelation(SpatialRelation.CROSSES, sqlTemplate);
	}

	public void testDisjoint(String sqlTemplate) throws Exception {
		testRelation(SpatialRelation.DISJOINT, sqlTemplate);
	}

	public void testEquals(String sqlTemplate) throws Exception {
		testRelation(SpatialRelation.EQUALS, sqlTemplate);
	}

	public void testIntersects(String sqlTemplate) throws Exception {
		testRelation(SpatialRelation.INTERSECTS, sqlTemplate);
	}

	public void testOverlaps(String sqlTemplate) throws Exception {
		testRelation(SpatialRelation.OVERLAPS, sqlTemplate);
	}

	public void testTouches(String sqlTemplate) throws Exception {
		testRelation(SpatialRelation.TOUCHES, sqlTemplate);
	}

	public void testWithin(String sqlTemplate) throws Exception {
		testRelation(SpatialRelation.WITHIN, sqlTemplate);
	}

	private void testHQLAsText(String entity) throws Exception {
		Session session = null;
		WKTReader reader = new WKTReader();

		try {
			session = factory.openSession();

			Query q = session
					.createQuery("select astext(l.geometry), l.geometry  from "
							+ entity + " as l");
			int i = 0;
			for (Iterator it = q.list().iterator(); it.hasNext() && i < 10; i++) {
				Object[] row = (Object[]) it.next();
				String text = (String) row[0];
				Geometry expected = (Geometry) row[1];
				Geometry geom = reader.read(text);
				assertTrue(expected.equalsExact(geom, 0.1));
			}
		} finally {
			if (session != null)
				session.close();
		}
	}

	public void testHQLAsText() throws Exception {
		for (String entity : entities) {
			log.info("Testing AsText for " + entity);
			testHQLAsText(entity);
		}
	}

	private void testHQLAsBinary(String entity) throws Exception {
		Session session = null;
		WKBReader reader = new WKBReader();

		try {
			session = factory.openSession();

			Query q = session
					.createQuery("select asbinary(l.geometry), l.geometry  from "
							+ entity + " as l");
			int i = 0;
			for (Iterator it = q.list().iterator(); it.hasNext() && i < 10; i++) {
				Object[] row = (Object[]) it.next();
				byte[] bytes = (byte[]) row[0];
				Geometry expected = (Geometry) row[1];
				Geometry geom = reader.read(bytes);
				assertTrue(expected.equalsExact(geom, 0.1));
			}
		} finally {
			if (session != null)
				session.close();
		}
	}

	public void testHQLAsBinary() throws Exception {
		for (String entity : entities) {
			log.info("Testing AsBinary for " + entity);
			testHQLAsBinary(entity);
		}
	}

	public void testHQLDimension() throws Exception {
		Session session = factory.openSession();
		Query q = session
				.createQuery("select dimension(l.geometry) from LineStringEntity as l where l.geometry is not null");
		int i = 0;
		for (Iterator it = q.list().iterator(); it.hasNext() && i < 10; i++) {
			Integer dim = (Integer) it.next();
			assertEquals(new Integer(1), dim);
		}

		q = session
				.createQuery("select dimension(p.geometry) from PolygonEntity as p where p.geometry is not null");
		i = 0;
		for (Iterator it = q.list().iterator(); it.hasNext() && i < 10; i++) {
			Integer dim = (Integer) it.next();
			assertEquals(new Integer(2), dim);
		}

		session.close();
	}

	public void testHQLOverlaps(String sqlTemplate) throws Exception {
		testHQL("overlaps", sqlTemplate, 2);
	}

	// TODO : investigate whether relate function in Oracle and Postgis are
	// equivalent.
	public void testHQLRelateLineString() throws Exception {
		Session session = factory.openSession();

		Query q = session
				.createQuery("select count(*) from LineStringEntity l where relate(l.geometry, ?, 'TT*******') = true and l.geometry is not null");
		Type geometryType = new CustomType(GeometryUserType.class, null);
		q.setParameter(0, jtsFilter, geometryType);
		Long cnt = (Long) q.list().get(0);
		System.out.println("Related by 'TT*******' pattern = " + cnt);
	}

	public void testHQLIntersectsLineString(String sqlString) throws Exception {
		testHQL("intersects", sqlString, 2);

	}

	public void testHQLSRID() throws Exception {
		Session session = factory.openSession();
		Query q = session
				.createQuery("select srid(l.geometry) from LineStringEntity as l where l.geometry is not null");
		int i = 0;
		for (Iterator it = q.list().iterator(); it.hasNext(); i++) {
			Integer srid = (Integer) it.next();
			assertEquals(31370, srid.intValue());
		}
		session.close();
	}

	public void testHQLGeometryType() throws Exception {
		System.out.println("Testing GeometryType");
		Session session = factory.openSession();
		Query q = session
				.createQuery("select geometrytype(l.geometry) from LineStringEntity as l where l.geometry is not null");
		int i = 0;
		for (Iterator it = q.list().iterator(); it.hasNext(); i++) {
			String gt = (String) it.next();
			assertEquals("LINESTRING", gt);
		}
		q = session
				.createQuery("select geometrytype(p.geometry) from PolygonEntity as p where p is not null");
		i = 0;
		for (Iterator it = q.list().iterator(); it.hasNext(); i++) {
			String gt = (String) it.next();
			assertEquals("POLYGON", gt);
		}
		session.close();

	}

	public void testHQLEnvelope() throws Exception {
		Session session = factory.openSession();
		testHQLEnvelope("LineStringEntity", session);
		testHQLEnvelope("PolygonEntity", session);
		session.close();
	}

	// TODO: use testHQLGeomOperation
	private void testHQLEnvelope(String entityName, Session session) {
		Query q = session
				.createQuery("select envelope(e.geometry), e.geometry from "
						+ entityName + " as e where e.geometry is not null");
		int i = 0;
		for (Iterator it = q.list().iterator(); it.hasNext() && i < 10; i++) {
			// get the database-calculated envelope
			Object[] geoms = (Object[]) it.next();
			Geometry env = (Geometry) geoms[0];
			env.normalize();

			// calculate the envelope with JTS
			Geometry g = (Geometry) geoms[1];
			Geometry env2 = g.getEnvelope();
			env2.normalize();

			// precision is only 0.01 (1 cm)
			assertTrue(env2.equalsExact(env, 0.01));
		}
	}

	public void testHQLIsEmpty(String sql) throws Exception {
		testHQL("isempty", sql, 1);
	}

	public void testHQLIsSimple(String sql) throws Exception {
		testHQL("issimple", sql, 1);
	}

	public void testHQLDistance() throws Exception {
		Session session = factory.openSession();
		Query q = session
				.createQuery("select distance(l.geometry, ?), l.geometry from LineStringEntity as l where l.geometry is not null");
		Type geometryType = new CustomType(GeometryUserType.class, null);
		q.setParameter(0, jtsFilter, geometryType);
		for (Iterator it = q.list().iterator(); it.hasNext();) {
			Object[] objs = (Object[]) it.next();
			Double distance = (Double) objs[0];
			Geometry geom = (Geometry) objs[1];
			assertEquals(geom.distance(jtsFilter), distance, 0.003);
		}
		session.close();
	}

	public void testHQLDisjoint(String sqlTemplate) throws Exception {
		testHQL("disjoint", sqlTemplate, 2);
	}

	private void testHQLGeomOperation(String operation, Method jtsOper,
			Object[] args) throws Exception {
		for (int i = 0; i < entities.length; i++) {
			// TODO: Add unit tests for multilinestrings.
			// currently, multilinestrings are to widespread, so that operating
			// on them becomes too expensive
			if (entities[i].equalsIgnoreCase("MultiLineStringEntity")) {
				continue;
			}
			testHQLGeomOperation(operation, jtsOper, entities[i], args);
		}
	}

	private void testHQLGeomOperation(String operation, Method jtsOper,
			String entity, Object[] args) throws Exception {
		Session session = null;

		try {
			session = factory.openSession();
			String opStr = operation + "( geometry";
			for (int i = 0; i < args.length; i++) {
				opStr += ", ?";
			}
			opStr += ")";
			String queryStr = "select geometry, " + opStr + " from " + entity
					+ " where geometry is not null";
			log.debug("HQL query str:" + queryStr);
			Query q = session.createQuery(queryStr);

			for (int idxp = 0; idxp < args.length; idxp++) {
				if (args[idxp] instanceof Geometry) {
					Type type = new CustomType(GeometryUserType.class, null);
					q.setParameter(idxp, args[idxp], type);
				} else {
					q.setParameter(idxp, args[idxp]);
				}

			}

			int cnt = 0;
			for (Iterator it = q.list().iterator(); it.hasNext();) {
				Object[] objs = (Object[]) it.next();
				Geometry geom = (Geometry) objs[0];
				Geometry opresult = (Geometry) objs[1];
				opresult.normalize();

				Object[] jtsOperArgs = new Object[args.length];
				for (int idx = 0; idx < args.length; idx++) {
					jtsOperArgs[idx] = args[idx];
				}
				Geometry jtsOpResult = (Geometry) jtsOper.invoke(geom,
						jtsOperArgs);
				jtsOpResult.normalize();
				// take into account that JTS algorithms and database
				// implemented functions my
				// differ, so results will only be approximately coincident.
				// here we test whether the two are coincident up to 10%
				if (!approximateCoincident(opresult, jtsOpResult, 0.1)) {
					cnt++;
					log.debug("Unequal results for " + operation + " on geom: "
							+ geom + "\n\t db  result = " + opresult
							+ "\n\t jts result = " + jtsOpResult);
					break;
				}
			}
			assertTrue(cnt == 0);
			log.debug("Number of unequal results for operation " + operation
					+ "  = " + cnt);
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				// harmless here.
			}
		}
	}

	public void testHQLBoundary() throws Exception {
		Method jtsOper = Geometry.class.getDeclaredMethod("getBoundary");
		testHQLGeomOperation("boundary", jtsOper, new Object[] {});
	}

	public void testHQLBuffer() throws Exception {
		Method jtsOper = Geometry.class.getDeclaredMethod("buffer",
				new Class[] { Double.TYPE });
		testHQLGeomOperation("buffer", jtsOper, new Object[] { new Double(10) });
	}

	public void testHQLConvexHull() throws Exception {
		Method jtsOper = Geometry.class.getDeclaredMethod("convexHull");
		testHQLGeomOperation("convexHull", jtsOper, new Object[] {});
	}

	public void testHQLDifference() throws Exception {
		Method jtsOper = Geometry.class.getDeclaredMethod("difference",
				new Class[] { Geometry.class });
		testHQLGeomOperation("difference", jtsOper, new Object[] { jtsFilter });
	}

	public void testHQLIntersection() throws Exception {
		Method jtsOper = Geometry.class.getDeclaredMethod("intersection",
				new Class[] { Geometry.class });
		testHQLGeomOperation("intersection", jtsOper,
				new Object[] { jtsFilter });
	}

	public void testHQLSymDifference() throws Exception {
		Method jtsOper = Geometry.class.getDeclaredMethod("symDifference",
				new Class[] { Geometry.class });
		testHQLGeomOperation("symDifference", jtsOper,
				new Object[] { jtsFilter });
	}

	public void testHQLUnion() throws Exception {
		Method jtsOper = Geometry.class.getDeclaredMethod("union",
				new Class[] { Geometry.class });
		testHQLGeomOperation("geomunion", jtsOper, new Object[] { jtsFilter });
	}

	/**
	 * Used to test if two geometries are approximately co-incident.
	 * 
	 * The geometries are approximately co-incident when their bounding boxes
	 * are equal within a tolerance value, and when their areas or lengths are
	 * equal within the specified relative tolerance.
	 * 
	 * @param g1
	 * @param g2
	 * @param tolerance
	 *            acceptable relative error in
	 * @return
	 */
	private boolean approximateCoincident(Geometry g1, Geometry g2,
			double tolerance) {
		if (g1 instanceof GeometryCollection
				&& g2 instanceof GeometryCollection) {
			GeometryCollection gc1 = (GeometryCollection) g1;
			GeometryCollection gc2 = (GeometryCollection) g2;
			if (gc1.getNumGeometries() != gc2.getNumGeometries()) {
				return false;
			}
			for (int i = 0; i < gc1.getNumGeometries(); i++) {
				boolean approx = approximateCoincident(gc1.getGeometryN(i), gc2
						.getGeometryN(i), tolerance);
				if (!approx) {
					return approx;
				}
			}
			return true;

		} else {
			// Envelopes of geometries should overlap
			Geometry e1 = g1.getEnvelope();
			Geometry e2 = g2.getEnvelope();

			boolean coincident = e1.equalsExact(e2, tolerance);
			if (!coincident) {
				return false;
			} else {
				boolean approxPointSetSize = false;
				if (g1.getDimension() == 0 && g2.getDimension() == 0) {
					return true;
				} else if (g1.getDimension() == 1 && g2.getDimension() == 1) {
					approxPointSetSize = (Math.abs(g1.getLength()
							- g2.getLength()) / g1.getLength()) < tolerance;
				} else {
					approxPointSetSize = (Math.abs(g1.getArea() - g2.getArea()) / g1
							.getArea()) < tolerance;
				}
				return approxPointSetSize;
			}

		}

	}

}
