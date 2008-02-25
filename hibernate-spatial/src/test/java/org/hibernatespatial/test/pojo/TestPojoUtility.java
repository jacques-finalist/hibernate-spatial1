package org.hibernatespatial.test.pojo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import org.dom4j.Document;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.pojo.POJOUtility;
import org.hibernatespatial.pojo.TypeMapper;

public class TestPojoUtility {

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
	
	public SessionFactory getSessionFactory(){
		return this.sessionFactory;
	}
	
	public POJOUtility getPOJOUtility(){
		return this.pojoUtil;
	}
}
