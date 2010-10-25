package org.hibernatespatial.mysql;

import org.hibernate.testing.junit.functional.FunctionalTestCase;
import org.hibernatespatial.test.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 18, 2010
 */
public class MySQLTestSupport extends TestSupport {

    @Override
    public TestData createTestData(FunctionalTestCase testcase) {
        if (testcase.getClass().getCanonicalName().contains("TestSpatialFunctions") ||
                testcase.getClass().getCanonicalName().contains("TestSpatialRestrictions")) {
            return TestData.fromFile("test-mysql-functions-data-set.xml");
        }
        return TestData.fromFile("test-data-set.xml");
    }

    @Override
    public AbstractExpectationsFactory createExpectationsFactory(DataSourceUtils dataSourceUtils) {
        return new MySQLExpectationsFactory(dataSourceUtils);
    }

    @Override
    public GeometryEquality createGeometryEquality() {
        return new MySQLGeometryEquality();
    }

    @Override
    public SQLExpressionTemplate getSQLExpressionTemplate() {
        return new MySQLExpressionTemplate();
    }
}
