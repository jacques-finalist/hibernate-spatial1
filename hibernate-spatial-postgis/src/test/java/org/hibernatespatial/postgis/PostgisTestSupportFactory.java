package org.hibernatespatial.postgis;

import org.hibernate.cfg.Configuration;
import org.hibernate.testing.junit.functional.FunctionalTestCase;
import org.hibernatespatial.test.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Sep 30, 2010
 */
public class PostgisTestSupportFactory implements TestSupportFactory {


    public DataSourceUtils createDataSourceUtil(Configuration configuration) {
        String jdbcUrl = configuration.getProperty("hibernate.connection.url");
        String jdbcUser = configuration.getProperty("hibernate.connection.username");
        String jdbcPass = configuration.getProperty("hibernate.connection.password");
        String jdbcDriver = configuration.getProperty("hibernate.connection.driver_class");
        return new DataSourceUtils(jdbcDriver, jdbcUrl, jdbcUser, jdbcPass, new PostgisExpressionTemplate());
    }

    public TestData createTestData(FunctionalTestCase testcase) {
        if (testcase.getClass().getCanonicalName().contains("TestSpatialFunctions") ||
                testcase.getClass().getCanonicalName().contains("TestSpatialRestrictions")) {
            return TestData.fromFile("postgis-functions-test.xml");
        }
        return TestData.fromFile("test-data-set.xml");
    }

    public GeometryEquality createGeometryEquality() {
        return new GeometryEquality();
    }

    public AbstractExpectationsFactory createExpectationsFactory(DataSourceUtils dataSourceUtils) {
        return new PostgisExpectationsFactory(dataSourceUtils);
    }


}
