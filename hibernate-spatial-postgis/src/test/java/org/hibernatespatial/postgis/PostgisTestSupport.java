package org.hibernatespatial.postgis;


import org.hibernate.testing.junit.functional.FunctionalTestCase;
import org.hibernatespatial.test.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Sep 30, 2010
 */
public class PostgisTestSupport extends TestSupport {


    public TestData createTestData(FunctionalTestCase testcase) {
        if (testcase.getClass().getCanonicalName().contains("TestSpatialFunctions") ||
                testcase.getClass().getCanonicalName().contains("TestSpatialRestrictions")) {
            return TestData.fromFile("postgis-functions-test.xml");
        }
        return TestData.fromFile("test-data-set.xml");
    }

    public AbstractExpectationsFactory createExpectationsFactory(DataSourceUtils dataSourceUtils) {
        return new PostgisExpectationsFactory(dataSourceUtils);
    }

    @Override
    public SQLExpressionTemplate getSQLExpressionTemplate() {
        return new PostgisExpressionTemplate();
    }


}
