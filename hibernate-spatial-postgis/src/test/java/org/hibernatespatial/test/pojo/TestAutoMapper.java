/**
 *
 */
package org.hibernatespatial.test.pojo;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.pojo.AutoMapper;
import org.hibernatespatial.postgis.PostgisExpressionTemplate;
import org.hibernatespatial.test.DataSourceUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen
 */
public class TestAutoMapper {

    private static TestPojoUtility delegate;

    private static String dbUrl;

    private static String DBNAME = "test";

    private static HSConfiguration config;

    private static Connection conn;

    static {

        config = new HSConfiguration();
        config.configure();

        dbUrl = "jdbc:postgresql://localhost:5432/" + DBNAME;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(dbUrl, "postgres", "");

        } catch (Exception e) {
            e.printStackTrace();
        }
        delegate = new TestPojoUtility();
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        delegate.setUpBeforeClass(config, conn);
    }


    @Test
    public void testList() throws SQLException {
        DataSourceUtils dataSourceUtils = new DataSourceUtils("hibernate-spatial-postgis-test.properties", new PostgisExpressionTemplate());
        dataSourceUtils.insertTestData();
        Session session = delegate.getSessionFactory().openSession();
        try {

            List<String[]> tables = AutoMapper.getMappedTables();

            for (String[] tncomp : tables) {
                Class entityClass = AutoMapper.getClass(tncomp[0], tncomp[1], tncomp[2]);
                Criteria c = session.createCriteria(entityClass);
                List results = c.list();
                assertTrue(results.size() > 1);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        dataSourceUtils.deleteTestData();

    }

}
