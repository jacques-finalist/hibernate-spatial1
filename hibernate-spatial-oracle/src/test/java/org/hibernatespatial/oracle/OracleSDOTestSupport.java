package org.hibernatespatial.oracle;

import org.hibernate.cfg.Configuration;
import org.hibernate.testing.junit.functional.FunctionalTestCase;
import org.hibernatespatial.test.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 22, 2010
 */
public class OracleSDOTestSupport extends TestSupport {

    @Override
    public TestData createTestData(FunctionalTestCase testcase) {
        return TestData.fromFile("test-sdo-geometry-data-set-2D.xml", new SDOTestDataReader());
    }

    @Override
    public AbstractExpectationsFactory createExpectationsFactory(DataSourceUtils dataSourceUtils) {
        return new SDOGeometryExpectationsFactory(dataSourceUtils);
    }

    @Override
    public SQLExpressionTemplate getSQLExpressionTemplate() {
        return new SDOGeometryExpressionTemplate();
    }

    @Override
    public DataSourceUtils createDataSourceUtil(Configuration configuration) {
        this.configuration = configuration;
        return new SDODataSourceUtils(driver(), url(), user(), passwd(), getSQLExpressionTemplate());
    }
}
