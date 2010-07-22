/**
 * 
 */
package org.hibernatespatial.geodb;

import geodb.GeoDB;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernatespatial.test.DataSourceUtils;
import org.hibernatespatial.test.SQLExpressionTemplate;

/**
 * Extension of the {@link DataSourceUtils} class which sets up an in-memory
 * GeoDB database. The specified SQL file is used to generate a schema in the
 * database.
 * 
 * @author Jan Boonen, Geodan IT b.v.
 */
public class GeoDBDataSourceUtils extends DataSourceUtils {

	public GeoDBDataSourceUtils(String propertyFile,
			SQLExpressionTemplate sqlExpressionTemplate, File schemaFile)
			throws SQLException, IOException {
		super(propertyFile, sqlExpressionTemplate);
		Connection conn = this.createConnection();
		GeoDB.InitGeoDB(conn);
		executeSql(parseSqlIn(schemaFile), conn);
	}

	// utility method to read a .sql txt input stream
	private String parseSqlIn(File file) throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));

			StringWriter sw = new StringWriter();
			BufferedWriter writer = new BufferedWriter(sw);

			for (int c = reader.read(); c != -1; c = reader.read()) {
				writer.write(c);
			}
			writer.flush();
			return sw.toString();

		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// utility method to run the parsed sql
	private void executeSql(String sql, Connection connection)
			throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute(sql);
	}
}
