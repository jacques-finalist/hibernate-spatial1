/**
 *
 */
package org.hibernatespatial.geodb;

import geodb.GeoDB;
import org.hibernatespatial.test.DataSourceUtils;
import org.hibernatespatial.test.SQLExpressionTemplate;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Extension of the {@link DataSourceUtils} class which sets up an in-memory
 * GeoDB database. The specified SQL file is used to generate a schema in the
 * database.
 *
 * @author Jan Boonen, Geodan IT b.v.
 */
public class GeoDBDataSourceUtils extends DataSourceUtils {

    public GeoDBDataSourceUtils(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPass,
                                SQLExpressionTemplate sqlExpressionTemplate)
            throws SQLException, IOException {
        super(jdbcDriver, jdbcUrl, jdbcUser, jdbcPass, sqlExpressionTemplate);
        Connection conn = this.getConnection();
        GeoDB.InitGeoDB(conn);
    }

}
