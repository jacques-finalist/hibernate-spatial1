package org.hibernatespatial.postgis.test.pojo.reader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.helper.FinderException;
import org.hibernatespatial.test.pojo.reader.TestBasicFeatureReader;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPostgisBasicFeatureReader {
	private static TestBasicFeatureReader delegate;

	private static String dbUrl;

	private static String DBNAME = "test";

	private static HSConfiguration config;

	private static Connection conn;

	private static String[] tableNames = new String[] { "linestringtest",
			"multilinestringtest", "polygontest" };
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
		delegate = new TestBasicFeatureReader();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		delegate.setUpBeforeClass(config, conn);
	}

	@Test
	public void testReaderNoFilters() throws FinderException {
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select count(*) from linestringtest");
			rs.next();
			Integer expected = rs.getInt(1);
			delegate.testReaderNoFilters(expected);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {

		}
	}

}
