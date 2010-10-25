package org.hibernatespatial.testsuite;

import org.hibernate.dialect.Dialect;
import org.hibernatespatial.test.TestSupport;


/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Sep 30, 2010
 */
public class TestSupportFactories {

    private static TestSupportFactories instance = new TestSupportFactories();

    public static TestSupportFactories instance() {
        return instance;
    }

    private TestSupportFactories() {
    }


    public TestSupport getTestSupportFactory(Dialect dialect) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (dialect == null) throw new IllegalArgumentException("Dialect argument is required.");
        String testSupportFactoryClassName = getSupportFactoryClassName(dialect);
        return instantiate(testSupportFactoryClassName);

    }

    private TestSupport instantiate(String testSupportFactoryClassName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader cloader = getClassLoader();
        Class<TestSupport> cl = (Class<TestSupport>) (cloader.loadClass(testSupportFactoryClassName));
        return cl.newInstance();
    }

    private ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }

    private static String getSupportFactoryClassName(Dialect dialect) {
        String canonicalName = dialect.getClass().getCanonicalName();
        if ("org.hibernatespatial.postgis.PostgisDialect".equals(canonicalName)) {
            return "org.hibernatespatial.postgis.PostgisTestSupport";
        }
        if ("org.hibernatespatial.geodb.GeoDBDialect".equals(canonicalName)) {
            return "org.hibernatespatial.geodb.GeoDBSupport";
        }
        if ("org.hibernatespatial.sqlserver.SQLServerSpatialDialect".equals(canonicalName)) {
            return "org.hibernatespatial.sqlserver.SQLServerTestSupport";
        }
        if ("org.hibernatespatial.mysql.MySQLSpatialDialect".equals(canonicalName)) {
            return "org.hibernatespatial.mysql.MySQLTestSupport";
        }
        if ("org.hibernatespatial.oracle.OracleSpatial10gDialect".equals(canonicalName)) {
            return "org.hibernatespatial.oracle.OracleSDOTestSupport";
        }
        throw new IllegalArgumentException("Dialect not known in test suite");
    }

}

