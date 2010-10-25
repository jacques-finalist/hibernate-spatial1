package org.hibernatespatial.sqlserver;

import org.hibernate.testing.junit.functional.FunctionalTestCase;
import org.hibernatespatial.test.DataSourceUtils;
import org.hibernatespatial.test.SQLExpressionTemplate;
import org.hibernatespatial.test.TestData;
import org.hibernatespatial.test.TestSupport;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 15, 2010
 */
public class SQLServerTestSupport extends TestSupport {


    public TestData createTestData(FunctionalTestCase testcase) {
        return TestData.fromFile("test-data-set.xml");
    }

    public SqlServerExpectationsFactory createExpectationsFactory(DataSourceUtils dataSourceUtils) {
        return new SqlServerExpectationsFactory(dataSourceUtils);
    }

    @Override
    public SQLExpressionTemplate getSQLExpressionTemplate() {
        return new SQLServerExpressionTemplate();
    }
}
