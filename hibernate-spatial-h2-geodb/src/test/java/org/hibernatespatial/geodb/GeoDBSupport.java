package org.hibernatespatial.geodb;

import org.hibernate.cfg.Configuration;
import org.hibernate.testing.junit.functional.FunctionalTestCase;
import org.hibernatespatial.test.*;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 2, 2010
 */
public class GeoDBSupport extends TestSupport {


    public DataSourceUtils createDataSourceUtil(Configuration configuration) {
        super.createDataSourceUtil(configuration);
        try {
            return new GeoDBDataSourceUtils(driver(), url(), user(), passwd(), getSQLExpressionTemplate());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TestData createTestData(FunctionalTestCase testcase) {
        return TestData.fromFile("test-geodb-data-set.xml");
    }

    public GeometryEquality createGeometryEquality() {
        return new GeoDBGeometryEquality();
    }

    public AbstractExpectationsFactory createExpectationsFactory(DataSourceUtils dataSourceUtils) {
        return new GeoDBNoSRIDExpectationsFactory((GeoDBDataSourceUtils) dataSourceUtils);
    }

    public SQLExpressionTemplate getSQLExpressionTemplate() {
        return new GeoDBExpressionTemplate();
    }


}

