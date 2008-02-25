/**
 * 
 */
package org.hibernatespatial.postgis.test.pojo;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.pojo.ClassInfo;
import org.hibernatespatial.pojo.ClassInfoMap;
import org.hibernatespatial.pojo.POJOUtility;
import org.hibernatespatial.test.pojo.TestPojoUtility;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Karel Maesen
 *
 */
public class TestPostgisPojoUtility {
	
	private static TestPojoUtility delegate;
	private static String dbUrl;
	private static String DBNAME = "test";
	private static HSConfiguration config;
	private static Connection conn;
	private static String[] tableNames = new String[] {"linestringtest", "multilinestringtest", "polygontest"};
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

	/**
	 * Do we find the classes?
	 */
	@Test
	public void testTables(){
		POJOUtility pojoUtil = delegate.getPOJOUtility();
		ClassInfoMap cim = pojoUtil.getClassInfoMap();
		for (int i = 0; i < tableNames.length; i++){
			ClassInfo csi = cim.getClassInfo(tableNames[i]);
			assertNotNull(csi);
			assertNotNull(csi.getPOJOClass());
		}
	}
	
	@Test
	public void testList(){
		
		POJOUtility pojoUtil = delegate.getPOJOUtility();
		ClassInfoMap cim = pojoUtil.getClassInfoMap();
		
		Session session = delegate.getSessionFactory().openSession();
		
		try {
			
			for (int i = 0; i < tableNames.length; i++){
				ClassInfo csi = cim.getClassInfo(tableNames[i]);
				Class entityClass = csi.getPOJOClass();
				Criteria c = session.createCriteria(entityClass);
				List results = c.list();
				assertTrue(results.size() > 1);
			}
			
		} catch(Exception e){
			throw new RuntimeException(e);
		} finally{
			session.close();
		}
		
	}
	
}
