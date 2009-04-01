/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
 * Copyright © 2008 Geovise BVBA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, visit: http://www.hibernatespatial.org/
 */
package org.hibernatespatial.pojo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.Document;
import org.hibernatespatial.HBSpatialExtension;

/**
 * @author Karel Maesen, Geovise BVBA
 * 
 */
public class AutoMapper {

	protected final static Logger logger = LoggerFactory.getLogger(AutoMapper.class);
	
	protected final static String PACKAGE_NAME = "org.hibernatespatial.features.generated";
	
	private static Map<TableName, Class<?>> tableClassMap = new HashMap<TableName, Class<?>>(); 
	
	public static synchronized Document map(Connection conn, String catalog, String schema, Collection<String> tableNames) throws SQLException {
		NamingStrategy naming = new SimpleNamingStrategy();
		TypeMapper typeMapper = new TypeMapper(HBSpatialExtension.getDefaultSpatialDialect().getDbGeometryTypeName());
		DatabaseMetaData dmd = conn.getMetaData();
		FeatureMapper fMapper = new FeatureMapper(naming, typeMapper);	
		FeatureClassGenerator fGenerator = new FeatureClassGenerator(PACKAGE_NAME,naming);
		List<ClassInfo> cInfos = new ArrayList<ClassInfo>();
		for (String tableName : tableNames) {
			logger.info("Generating class info for table " + tableName + " in catalog/schema " + catalog + "/" + schema);
			
			//TODO -- test if we haven't already mapped the table
			
			ClassInfo cInfo;
			try {
				cInfo = fMapper.createClassInfo(catalog, schema, tableName, dmd);
				logger.info("Generating class " + cInfo.getClassName() + " for table " + tableName);
				Class <?> clazz = fGenerator.generate(cInfo);
				tableClassMap.put(new TableName(catalog, schema, tableName), clazz);
				cInfos.add(cInfo);
			} catch (TableNotFoundException e) {
				logger.warn(e.getMessage());
			}
		}
		logger.info("Generating Hibernate Mapping file");
		MappingsGenerator mappingGenerator = new MappingsGenerator(PACKAGE_NAME);	
		try {
			mappingGenerator.load(cInfos);
		} catch (PKeyNotFoundException e) {
			throw new RuntimeException(e);
		}
		return mappingGenerator.getMappingsDoc();
	}
	
	public static Class<?> getClass(String catalog, String schema, String tableName){
		TableName tbn = new TableName(catalog, schema, tableName);
		return tableClassMap.get(tbn);
	}
	
	public static List<String[]> getMappedTables(){
		List<String[]> list = new ArrayList<String[]>();
		for (TableName tbn : tableClassMap.keySet()){
			String[] sa = new String[3];
			sa[0] = tbn.catalog;
			sa[1] = tbn.schema;
			sa[2] = tbn.tableName;
			list.add(sa);
		}
		return list;
	}
	
	private static class TableName {
		String catalog;
		String schema;
		String tableName;
		
		private TableName(String catalog, String schema, String tableName){
			this.catalog = catalog;
			this.schema = schema;
			this.tableName = tableName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((catalog == null) ? 0 : catalog.hashCode());
			result = prime * result
					+ ((schema == null) ? 0 : schema.hashCode());
			result = prime * result
					+ ((tableName == null) ? 0 : tableName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof TableName))
				return false;
			TableName other = (TableName) obj;
			if (catalog == null) {
				if (other.catalog != null)
					return false;
			} else if (!catalog.equals(other.catalog))
				return false;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			if (tableName == null) {
				if (other.tableName != null)
					return false;
			} else if (!tableName.equals(other.tableName))
				return false;
			return true;
		}
		
		
		
	}
}
