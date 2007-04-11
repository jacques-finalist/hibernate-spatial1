package com.cadrie.hibernate.spatial.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import com.cadrie.hibernate.spatial.GeometryUserType;
import com.cadrie.hibernate.spatial.criterion.SpatialRestrictions;
import com.cadrie.hibernate.spatial.test.model.LineStringEntity;
import com.cadrie.hibernate.spatial.test.model.MultiLineStringEntity;
import com.cadrie.hibernate.spatial.test.model.PolygonEntity;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import static org.junit.Assert.*;

public class TestSpatialQueries {

    private SessionFactory factory;

    private Connection conn;

    private Geometry jtsFilter = null;

    private String filterPolygonString = "POLYGON((0.0 0.0, 25000.0 0.0, 25000.0 25000.0, 0.0 25000.0, 0.0 0.0))";

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

    public void testLineStringFiltering(String sql) throws Exception {

        Session session = null;
        try {
            // apply the filter using Hibernate
            session = factory.openSession();

            Criteria testCriteria = session
                    .createCriteria(LineStringEntity.class);

            testCriteria
                    .add(SpatialRestrictions.filter("geometry", jtsFilter));

            List results = testCriteria.list();

            // get the same results using JDBC - SQL directly;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, this.filterPolygonString);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int expected = rs.getInt(1);
            System.out.println("overlap geometries = " + results.size());
            // test whether they give the same results
            assertEquals(expected, results.size());
        } finally {
            if (session != null)
                session.close();
        }

    }

    public void testPolygonFiltering(String sql) throws Exception {
        // apply the filter using Hibernate
        Session session = null;
        try {
            session = factory.openSession();

            Criteria testCriteria = session
                    .createCriteria(PolygonEntity.class);

            testCriteria
                    .add(SpatialRestrictions.filter("geometry", jtsFilter));

            List results = testCriteria.list();

            // get the same results using JDBC - SQL directly;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, this.filterPolygonString);
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

    public void testMultiLineStringFiltering(String sql) throws Exception {
        // apply the filter using Hibernate
        Session session = null;

        try {
            session = factory.openSession();

            Criteria testCriteria = session
                    .createCriteria(MultiLineStringEntity.class);

            testCriteria
                    .add(SpatialRestrictions.filter("geometry", jtsFilter));

            List results = testCriteria.list();

            // get the same results using JDBC - SQL directly;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, this.filterPolygonString);
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

    public void testHQLAsTextLineString() throws Exception {
        Session session = null;

        try {
            session = factory.openSession();

            Query q = session
                    .createQuery("select astext(l.geometry) from LineStringEntity as l");
            int i = 0;
            for (Iterator it = q.list().iterator(); it.hasNext() && i < 10; i++) {
                String s = (String) it.next();
                System.out.println(s);
            }
        } finally {
            if (session != null)
                session.close();
        }
    }

    public void testHQLDimensionLineString() throws Exception {
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

    public void testHQLOverlapsLineString(String sql) throws Exception {
        Session session = factory.openSession();
        Query q = session
                .createQuery("select overlaps(?,l.geometry) from LineStringEntity as l where l.geometry is not null");
        Type GeometryType = new CustomType(GeometryUserType.class, null);
        q.setParameter(0, jtsFilter, GeometryType);
        int numOverlapping = 0;
        for (Iterator it = q.list().iterator(); it.hasNext();) {
            Boolean b = (Boolean) it.next();
            if (b)
                numOverlapping++;
        }

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, this.filterPolygonString);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int expected = rs.getInt(1);
        assertEquals(expected, numOverlapping);
        System.out.println("num. overlapping = " + numOverlapping);
        session.close();

    }

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
        Session session = factory.openSession();
        Query q = session
                .createQuery("select intersects(?,l.geometry) from LineStringEntity as l where l.geometry is not null");
        Type geometryType = new CustomType(GeometryUserType.class, null);
        q.setParameter(0, jtsFilter, geometryType);
        long intersects = 0;
        for (Iterator it = q.list().iterator(); it.hasNext();) {
            Boolean b = (Boolean) it.next();
            if (b)
                intersects++;
        }

        q = session
                .createQuery("select count(*) from LineStringEntity as l where intersects(l.geometry, ?) = true");
        q.setParameter(0, jtsFilter, geometryType);
        long altIntersects = (Long) q.list().get(0);
        assertEquals(intersects, altIntersects);

        PreparedStatement stmt = conn.prepareStatement(sqlString);
        stmt.setString(1, this.filterPolygonString);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int expected = rs.getInt(1);
        assertEquals(expected, intersects);
        System.out.println("num. intersects = " + intersects);

        Query q2 = session
                .createQuery("from LineStringEntity as l where intersects(?,l.geometry) = True");
        q2.setParameter(0, jtsFilter, geometryType);
        List l = q2.list();
        assertEquals(expected, l.size());

        session.close();

    }

    public void testHQLSRID() throws Exception {
        Session session = factory.openSession();
        Query q = session
                .createQuery("select srid(l.geometry) from LineStringEntity as l where l.geometry is not null");
        int i = 0;
        for (Iterator it = q.list().iterator(); it.hasNext(); i++) {
            Integer srid = (Integer) it.next();
            assertEquals(31370, srid.intValue());
            if (i == 0)
                System.out.println("srid: " + srid);
        }
        session.close();
    }

    public void testHGLGeometryType() throws Exception {
        System.out.println("Testing GeometryType");
        Session session = factory.openSession();
        Query q = session
                .createQuery("select geometrytype(l.geometry) from LineStringEntity as l where l.geometry is not null");
        int i = 0;
        for (Iterator it = q.list().iterator(); it.hasNext(); i++) {
            String gt = (String) it.next();
            // if (i < 10)
            // System.out.println(gt);
            assertEquals("LINESTRING", gt);
        }
        q = session
                .createQuery("select geometrytype(p.geometry) from PolygonEntity as p where p is not null");
        i = 0;
        for (Iterator it = q.list().iterator(); it.hasNext(); i++) {
            String gt = (String) it.next();
            if (i < 10)
                System.out.println(gt);
            assertEquals("POLYGON", gt);
        }
        session.close();

    }

    // TODO - test with database connection, because the getEnvelope()
    // result differs from
    // database envelope or MBR functions.
    public void testHQLEnvelope() throws Exception {
        Session session = factory.openSession();
        testHQLEnvelope("LineStringEntity", session);
        testHQLEnvelope("PolygonEntity", session);
        session.close();
    }

    private void testHQLEnvelope(String entityName, Session session) {
        Query q = session
                .createQuery("select envelope(e.geometry), e.geometry from "
                        + entityName + " as e where e.geometry is not null");
        System.out.println("Envelope test for " + entityName);
        System.out.println("------------------------------------");
        int i = 0;
        for (Iterator it = q.list().iterator(); it.hasNext() && i < 10; i++) {
            Object[] geoms = (Object[]) it.next();
            Geometry env = (Geometry) geoms[0];
            Geometry g = (Geometry) geoms[1];
            System.out.println("Env. = " + env.toText());
            System.out.println(" PEnv = " + g.getEnvelope().toText());

            assertTrue(g.getEnvelope().equalsExact(env));
        }
    }

    public void testHQLIsEmpty(String sql) throws Exception {
        Session session = factory.openSession();
        Query q = session
                .createQuery("select l.id, isempty(l.geometry) from LineStringEntity as l where l.geometry is not null");
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (Iterator it = q.list().iterator(); it.hasNext();) {
            Object[] objs = (Object[]) it.next();
            long id = (Long) objs[0];
            Boolean isEmpty = (Boolean) objs[1];
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            Boolean expected = rs.getBoolean(1);
            rs.close();
            assertEquals(expected, isEmpty);
        }
        pstmt.close();
        session.close();
    }

    public void testHQLIsSimple(String sql) throws Exception {
        Session session = factory.openSession();
        Query q = session
                .createQuery("select l.id, issimple(l.geometry) from LineStringEntity as l where l.geometry is not null");
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (Iterator it = q.list().iterator(); it.hasNext();) {
            Object[] objs = (Object[]) it.next();
            long id = (Long) objs[0];
            Boolean isSimple = (Boolean) objs[1];
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            Boolean expected = rs.getBoolean(1);
            rs.close();
            assertEquals(expected, isSimple);
        }
        pstmt.close();
        session.close();
    }

    public void testHQLBoundary() throws Exception {
        Session session = factory.openSession();
        Query q = session
                .createQuery("select p.geometry, boundary(p.geometry) from PolygonEntity as p where p.geometry is not null");

        for (Iterator it = q.list().iterator(); it.hasNext();) {
            Object[] objs = (Object[]) it.next();
            Geometry geom = (Geometry) objs[0];
            Geometry bound = (Geometry) objs[1];
            assertTrue(geom.getBoundary().equals(bound));
        }
        session.close();
    }

    public void testHQLAsBinary(String sql) throws Exception {
        Session session = factory.openSession();
        Query q = session
                .createQuery("select id, l.geometry, asbinary(l.geometry) from LineStringEntity as l where l.geometry is not null");
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (Iterator it = q.list().iterator(); it.hasNext();) {
            Object[] objs = (Object[]) it.next();
            long id = (Long) objs[0];
            byte[] wkb = (byte[]) objs[2];
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            Object obj = rs.getBytes(1);

            byte[] expected = (byte[]) obj;
            rs.close();
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], wkb[i]);
            }
        }
        pstmt.close();
        session.close();
    }

    public void testHQLDistance() throws Exception {
        Session session = factory.openSession();

        Query q = session
                .createQuery("select distance(l.geometry, ?), l.geometry from LineStringEntity as l where l.geometry is not null");
        Type geometryType = new CustomType(GeometryUserType.class, null);
        q.setParameter(0, jtsFilter, geometryType);
        int i = 0;
        for (Iterator it = q.list().iterator(); it.hasNext() && i++ < 100;) {
            Object[] objs = (Object[]) it.next();
            Double distance = (Double) objs[0];
            Geometry geom = (Geometry) objs[1];
            System.out.println("Distance= " + distance);
            assertEquals(geom.distance(jtsFilter), distance, 0.003);
        }

        session.close();
    }

    public void testHQLBuffer() throws Exception {
        Session session = factory.openSession();

        Query q = session
                .createQuery("select p.geometry, buffer(p.geometry, 10.0) from PolygonEntity as p where p.geometry is not null");
        int i = 0;
        int cnt = 0;
        for (Iterator it = q.list().iterator(); it.hasNext() && i++ < 100;) {
            Object[] objs = (Object[]) it.next();
            Geometry geom = (Geometry) objs[0];
            Geometry buffer = (Geometry) objs[1];

            Geometry jtsBuffer = geom.buffer(10.0);

            buffer.normalize();
            jtsBuffer.normalize();
            if (approximateCoincident(jtsBuffer, buffer, 0.05))
                cnt++;
            else
                System.out.println("Unequal buffer for geom: " + geom
                        + "\n\t dbbuffer = " + buffer + "\n\t jtsbuffer = "
                        + jtsBuffer);
        }
        assertTrue(cnt > 0);
        System.out.println("Number of equal buffers= " + cnt);
        session.close();

    }

    public void testHQLConvexHull() throws Exception {
        Session session = factory.openSession();

        Query q = session
                .createQuery("select m.geometry, convexhull(m.geometry) from MultiLineStringEntity as m where m.geometry is not null");
        int i = 0;
        int cnt = 0;
        for (Iterator it = q.list().iterator(); it.hasNext() && i++ < 100;) {
            Object[] objs = (Object[]) it.next();
            Geometry geom = (Geometry) objs[0];
            Geometry cvh = (Geometry) objs[1];

            // constraint on convexity
            assertTrue("geometry not in its convex hull", cvh.contains(geom));
            cvh.normalize();

            Geometry jtsConvexHull = geom.convexHull();
            jtsConvexHull.normalize();
            if (jtsConvexHull.equalsExact(cvh, 0.5))
                cnt++;
            else
                System.out.println("Unequal convex hull for geom: " + geom
                        + "\n\t db  conv. hull = " + cvh
                        + "\n\t jts conv. hull = " + jtsConvexHull);
        }
        assertTrue(cnt > 0);
        System.out.println("Number of equal convex hulls = " + cnt);
        session.close();

    }

    public void testHQLDifference() throws Exception {
        Session session = factory.openSession();

        Query q = session
                .createQuery("select e.id, e.geometry, difference(e.geometry, ?) from PolygonEntity as e where e.geometry is not null");
        Type geometryType = new CustomType(GeometryUserType.class, null);
        q.setParameter(0, jtsFilter, geometryType);
        int i = 0;
        int cnt = 0;
        int cntEmpty = 0;
        for (Iterator it = q.list().iterator(); it.hasNext() && i++ < 100;) {
            Object[] objs = (Object[]) it.next();
            Geometry geom = (Geometry) objs[1];
            Geometry diff = (Geometry) objs[2];
            // some databases give a null object if the difference is the
            // null-set
            if (diff == null || diff.isEmpty()) {
                cntEmpty++;
                continue;
            }
            diff.normalize();

            Geometry jtsDiff = geom.difference(jtsFilter);
            jtsDiff.normalize();
            if (jtsDiff.equalsExact(diff, 0.5))
                cnt++;
            else
                System.out.println("Unequal difference for geom: " + geom
                        + "\n\t db  difference = " + diff
                        + "\n\t jts difference = " + jtsDiff);
        }
        assertTrue(cnt > 0);
        System.out.println("Number of equal differences = " + cnt);
        System.out.println("Number of empty or null difference results = "
                + cntEmpty);
        session.close();

    }

    public void testHQLIntersection() throws Exception {
        Session session = factory.openSession();

        Query q = session
                .createQuery("select e.id, e.geometry, intersection(e.geometry, ?) from PolygonEntity as e where e.geometry is not null");
        Type geometryType = new CustomType(GeometryUserType.class, null);
        q.setParameter(0, jtsFilter, geometryType);
        int i = 0;
        int cnt = 0;
        int cntEmpty = 0;
        for (Iterator it = q.list().iterator(); it.hasNext() && i++ < 100;) {
            Object[] objs = (Object[]) it.next();
            Geometry geom = (Geometry) objs[1];
            Geometry intersect = (Geometry) objs[2];
            // some databases give a null object if the difference is the
            // null-set
            if (intersect == null || intersect.isEmpty()) {
                cntEmpty++;
                continue;
            }
            intersect.normalize();

            Geometry jtsIntersect = geom.intersection(jtsFilter);
            jtsIntersect.normalize();
            if (jtsIntersect.equalsExact(intersect, 0.5))
                cnt++;
            else
                System.out.println("Unequal intersection for geom: " + geom
                        + "\n\t db  intersect = " + intersect
                        + "\n\t jts intersect = " + jtsIntersect);
        }
        assertTrue(cnt > 0);
        System.out.println("Number of equal intersects = " + cnt);
        System.out.println("Number of empty or null intersect results = "
                + cntEmpty);
        session.close();

    }

    public void testHQLSymDifference() throws Exception {
        Session session = factory.openSession();

        Query q = session
                .createQuery("select e.id, e.geometry, symdifference(e.geometry, ?) from PolygonEntity as e where e.geometry is not null");
        Type geometryType = new CustomType(GeometryUserType.class, null);
        q.setParameter(0, jtsFilter, geometryType);
        int i = 0;
        int cnt = 0;
        int cntEmpty = 0;
        for (Iterator it = q.list().iterator(); it.hasNext() && i++ < 100;) {
            Object[] objs = (Object[]) it.next();
            Long id = (Long) objs[0];
            Geometry geom = (Geometry) objs[1];
            Geometry symDiff = (Geometry) objs[2];
            // some databases give a null object if the difference is the
            // null-set
            if (symDiff == null || symDiff.isEmpty()) {
                cntEmpty++;
                continue;
            }
            symDiff.normalize();

            Geometry jtsSymDiff = geom.symDifference(jtsFilter);
            jtsSymDiff.normalize();
            if (jtsSymDiff.equalsExact(symDiff, 0.5))
                cnt++;
            else
                System.out.println("Unequal symdiff for geom (" + id + "): "
                        + geom + "\n\t db  symdiff = " + symDiff
                        + "\n\t jts symdiff = " + jtsSymDiff);
        }
        assertTrue(cnt > 0);
        System.out.println("Number of equal symdiffs = " + cnt);
        System.out.println("Number of empty or null symdiff results = "
                + cntEmpty);
        session.close();

    }

    public void testHQLUnion() throws Exception {
        Session session = factory.openSession();

        Query q = session
                .createQuery("select e.id, e.geometry, geomunion(e.geometry, ?) from PolygonEntity as e where e.geometry is not null");
        Type geometryType = new CustomType(GeometryUserType.class, null);
        q.setParameter(0, jtsFilter, geometryType);
        int i = 0;
        int cnt = 0;
        int cntEmpty = 0;
        for (Iterator it = q.list().iterator(); it.hasNext() && i++ < 100;) {
            Object[] objs = (Object[]) it.next();
            Long id = (Long) objs[0];
            Geometry geom = (Geometry) objs[1];
            Geometry union = (Geometry) objs[2];
            // some databases give a null object if the difference is the
            // null-set
            if (union == null || union.isEmpty()) {
                cntEmpty++;
                continue;
            }
            union.normalize();

            Geometry jtsUnion = geom.union(jtsFilter);
            jtsUnion.normalize();
            if (jtsUnion.equalsExact(union, 0.5))
                cnt++;
            else
                System.out.println("Unequal union for geom (" + id + "): "
                        + geom + "\n\t db  union = " + union
                        + "\n\t jts union = " + jtsUnion);
        }
        assertTrue(cnt > 0);
        System.out.println("Number of equal unions = " + cnt);
        System.out.println("Number of empty or null union results = "
                + cntEmpty);
        session.close();

    }

    /**
     * Used to test if two geometries are approximately co-incident.
     * 
     * @param g1
     * @param g2
     * @param tolerance
     *            acceptable relative error in
     * @return
     */
    private boolean approximateCoincident(Geometry g1, Geometry g2,
            double tolerance) {
        Geometry symdiff = null;
        if (g1.getDimension() < 2 && g2.getDimension() < 2) {
            g1 = g1.buffer(tolerance);
            g2 = g2.buffer(tolerance);
            symdiff = g1.symDifference(g2).buffer(tolerance);
        } else {
            symdiff = g1.symDifference(g2);
        }
        double relError = symdiff.getArea() / (g1.getArea() + g2.getArea());
        return relError < tolerance;

    }

}
