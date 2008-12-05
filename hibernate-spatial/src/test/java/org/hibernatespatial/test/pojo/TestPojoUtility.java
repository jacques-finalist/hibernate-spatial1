package org.hibernatespatial.test.pojo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.pojo.AutoMapper;

public class TestPojoUtility {

	private  SessionFactory sessionFactory;
	private AutoMapper pojoUtil;
	
	public void setUpBeforeClass(HSConfiguration hsconfig, Connection conn) throws SQLException {
		
		
		
		HBSpatialExtension.setConfiguration(hsconfig);
		List<String> tables = new ArrayList<String>();
		tables.add("linestringtest");
		tables.add("multilinestringtest");
		tables.add("polygontest");
		Document mappingdocument;
		try {
			mappingdocument = AutoMapper.map(conn, null, null, tables);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		Configuration config = new Configuration();
		config.addXML(mappingdocument.asXML());
		this.sessionFactory = config.configure().buildSessionFactory();		
		
	}
	
	public SessionFactory getSessionFactory(){
		return this.sessionFactory;
	}
	
	public AutoMapper getPOJOUtility(){
		return this.pojoUtil;
	}
}
