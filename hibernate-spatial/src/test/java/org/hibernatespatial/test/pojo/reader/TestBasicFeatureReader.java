package org.hibernatespatial.test.pojo.reader;

import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.helper.FinderException;
import org.hibernatespatial.pojo.POJOUtility;
import org.hibernatespatial.pojo.TypeMapper;
import org.hibernatespatial.pojo.reader.BasicFeatureReader;
import org.hibernatespatial.pojo.reader.Feature;
import org.hibernatespatial.pojo.reader.FeatureReader;

public class TestBasicFeatureReader {
	
	private  SessionFactory sessionFactory;
	private POJOUtility pojoUtil;
	
	public void setUpBeforeClass(HSConfiguration hsconfig, Connection conn) throws SQLException {
		HBSpatialExtension.setConfiguration(hsconfig);
		SpatialDialect dialect = HBSpatialExtension.getDefaultSpatialDialect();
		
		
		TypeMapper typeMapper = new TypeMapper(dialect.getDbGeometryTypeName());
		this.pojoUtil = new POJOUtility(conn, "test.model", typeMapper);
		List<String> tables = new ArrayList<String>();
		tables.add("linestringtest");
		tables.add("multilinestringtest");
		tables.add("polygontest");
		Document mappingdocument;
		try {
			mappingdocument = pojoUtil.map(tables);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		Configuration config = new Configuration();
		config.addXML(mappingdocument.asXML());
		this.sessionFactory = config.configure().buildSessionFactory();		
		
	}
	
	public void testReaderNoFilters(int expected) throws FinderException {
		Class clazz = this.pojoUtil.getClassInfoMap().getClassInfo("linestringtest").getPOJOClass();
		FeatureReader reader = new BasicFeatureReader(clazz, this.sessionFactory,null, null);
		int count = 0;
		while(reader.hasNext()){
			Feature f = reader.next();
			assertNotNull(f.getId());
			count++;
		}
		assertEquals(expected, count);
		reader.close();	
	}

}
