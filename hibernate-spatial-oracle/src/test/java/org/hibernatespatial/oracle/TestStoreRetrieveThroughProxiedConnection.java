//package org.hibernatespatial.oracle;
//
//import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.io.ParseException;
//import org.hibernate.Criteria;
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
//import org.hibernate.Transaction;
//import org.hibernate.cfg.Configuration;
//import org.hibernatespatial.HBSpatialExtension;
//import org.hibernatespatial.cfg.HSConfiguration;
//import org.hibernatespatial.test.*;
//import org.junit.Before;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//
///**
// * @author Karel Maesen, Geovise BVBA
// *         creation-date: Jun 30, 2010
// *
// * This class is used to test whether
// * wrapping the OracleConnection object in
// * a generic JDBC Connection implementation
// * doesn't affect serializing/deserializing
// * SDOGeometry objects
// *
// */
//public class TestStoreRetrieveThroughProxiedConnection extends TestSDOGeometryStoreRetrieve {
//
//   private static Logger LOGGER = LoggerFactory.getLogger(TestStoreRetrieve.class);
//    private final TestData testData;
//    private final SDODataSourceUtils dataSourceUtils;
//    private final GeometryEquality geometryEquality;
//    private final SessionFactory factory;
//
//    public TestStoreRetrieveThroughProxiedConnection() {
//        dataSourceUtils = new SDODataSourceUtils("hibernate-spatial-oracle10g-test.properties");
//        testData = TestData.fromFile("test-sdo-geometry-data-set.xml", new SDOTestDataReader());
//
//              //drop index, set geometry metadata to 4D
//              try {
//                  dataSourceUtils.setGeomMetaDataTo4D();
//                  dataSourceUtils.dropIndex();
//              } catch (SQLException e) {
//                  // ignore these errors (usually it's just that there is no index)
//              }
//
//        this.geometryEquality = new GeometryEquality();
//
//        LOGGER.info("Setting up Hibernate");
//        Configuration config = new Configuration();
//        config.configure();
//        config.addClass(GeomEntity.class);
//
//        //configure Hibernate Spatial based on this config
//        HSConfiguration hsc = new HSConfiguration();
//        hsc.configure(config);
//        HBSpatialExtension.setConfiguration(hsc);
//
//        // build the session factory
//        factory = config.buildSessionFactory();
//
//        LOGGER.info("Hibernate set-up complete.");
//
//
//    }
//
//
//
//
//    @Before
//    public void setUp() throws SQLException {
//        dataSourceUtils.deleteTestData();
//    }
//
//    @Test
//    public void test_store_retrieve() throws ParseException {
//        Map<Integer, GeomEntity> stored = new HashMap<Integer, GeomEntity>();
//        storeTestObjects(stored);
//        retrieveAndCompare(stored);
//    }
//
//    @Test
//    public void test_store_retrieve_null_geometry() throws SQLException {
//        storeNullGeometry();
//        retrieveNullGeometry();
//    }
//
//    private void retrieveAndCompare(Map<Integer, GeomEntity> stored) {
//        int id = -1;
//        try {
//            Session session = openSession();
//            session.beginTransaction();
//            for (GeomEntity storedEntity : stored.values()) {
//                id = storedEntity.getId();
//                GeomEntity retrievedEntity = (GeomEntity) session.get(GeomEntity.class, id);
//                Geometry retrievedGeometry = retrievedEntity.getGeom();
//                Geometry storedGeometry = storedEntity.getGeom();
//                String msg = createFailureMessage(storedEntity.getId(), storedGeometry, retrievedGeometry);
//                assertTrue(msg, geometryEquality.test(storedGeometry, retrievedGeometry));
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(String.format("Failure on case: %d", id), e);
//        }
//        finally {
//            factory.getCurrentSession().getTransaction().rollback();
//        }
//    }
//
//    private Session openSession() throws SQLException {
//        Connection conn = dataSourceUtils.getConnection();
//        Connection proxiedConn = new JDBCConnectionProxy(conn);
//        Session session = factory.openSession(proxiedConn);
//        return session;
//    }
//
//    private String createFailureMessage(int id, Geometry storedGeometry, Geometry retrievedGeometry) {
//        String expectedText = (storedGeometry != null ? storedGeometry.toText() : "NULL");
//        String retrievedText = (retrievedGeometry != null ? retrievedGeometry.toText() : "NULL");
//        return String.format("Equality test failed for %d.\nExpected: %s\nReceived:%s", id, expectedText, retrievedText);
//    }
//
//    private void storeTestObjects(Map<Integer, GeomEntity> stored) {
//        Session session = null;
//        Transaction tx = null;
//        int id = -1;
//        try {
//            session = openSession();
//            // Every test instance is committed seperately
//            // to improve feedback in case of test failure
//            for (TestDataElement element : testData) {
//                id = element.id;
//                tx = session.beginTransaction();
//                GeomEntity entity = GeomEntity.createFrom(element);
//                stored.put(entity.getId(), entity);
//                session.save(entity);
//                tx.commit();
//            }
//        } catch (Exception e) {
//            if (tx != null) tx.rollback();
//            throw new RuntimeException("Failed storing test object with id:" + id, e);
//        } finally {
//            if (session != null) session.close();
//        }
//    }
//
//    private void storeNullGeometry() {
//        GeomEntity entity = null;
//        Session session = null;
//        Transaction tx = null;
//        try {
//            session = openSession();
//            tx = session.beginTransaction();
//            entity = new GeomEntity();
//            entity.setId(1);
//            entity.setType("NULL OBJECT");
//            session.save(entity);
//            tx.commit();
//        } catch (Exception e) {
//            if (tx != null) tx.rollback();
//            throw new RuntimeException("Failed storing test object with id:" + entity.getId(), e);
//        } finally {
//            if (session != null) session.close();
//        }
//    }
//
//
//    private void retrieveNullGeometry() throws SQLException {
//        Session session = null;
//        try {
//            session = openSession();
//            session.beginTransaction();
//            Criteria criteria = session.createCriteria(GeomEntity.class);
//            List<GeomEntity> retrieved = criteria.list();
//            assertEquals("Expected exactly one result", 1, retrieved.size());
//            GeomEntity entity = retrieved.get(0);
//            assertNull(entity.getGeom());
//        } finally {
//            session.getTransaction().rollback();
//        }
//    }
//
//
//}
