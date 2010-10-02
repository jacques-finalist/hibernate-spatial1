package org.hibernatespatial.testsuite;

import org.hibernate.dialect.Dialect;
import org.hibernatespatial.test.TestSupportFactory;


/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Sep 30, 2010
 */
public class TestSupportFactories {

    private static TestSupportFactories instance = new TestSupportFactories();

    public static TestSupportFactories instance(){
        return instance;
    }

    private TestSupportFactories(){}


    public TestSupportFactory getTestSupportFactory(Dialect dialect) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (dialect == null) throw new IllegalArgumentException("Dialect argument is required.");
        String testSupportFactoryClassName = getSupportFactoryClassName(dialect);
        return instantiate(testSupportFactoryClassName);

    }

    private  TestSupportFactory instantiate(String testSupportFactoryClassName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader cloader = getClassLoader();
        Class<TestSupportFactory> cl = (Class<TestSupportFactory>)(cloader.loadClass(testSupportFactoryClassName));
        return cl.newInstance();
    }

    private ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }

    private static String getSupportFactoryClassName(Dialect dialect){
        String canonicalName = dialect.getClass().getCanonicalName();
        if ("org.hibernatespatial.postgis.PostgisDialect".equals(canonicalName)) {
            return "org.hibernatespatial.postgis.PostgisTestSupportFactory";
        }
        throw new IllegalArgumentException("Dialect not known in test suite");
    }

}

