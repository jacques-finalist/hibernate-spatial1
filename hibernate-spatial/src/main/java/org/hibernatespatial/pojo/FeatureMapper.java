package org.hibernatespatial.pojo;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FeatureMapper {

	private final NamingStrategy naming;
	private final TypeMapper typeMapper;
	
	public FeatureMapper (NamingStrategy naming, TypeMapper typeMapper){
		this.naming = naming;
		this.typeMapper = typeMapper;
	}
	
	public ClassInfo createClassInfo(String catalog, String schema, String tableName, DatabaseMetaData dmd) throws TableNotFoundException {	
		
		ResultSet rs = null;
		boolean empty = true;
		String className = naming.createClassName(tableName);
		ClassInfo cInfo = new ClassInfo(tableName, className);
		try {
			rs = dmd.getColumns(catalog, schema, tableName, null);
			while (rs.next()){
				empty = false;
				String colName = rs.getString("COLUMN_NAME");
				String dbType = rs.getString("TYPE_NAME");
				int javaType = rs.getInt("DATA_TYPE");
				AttributeInfo ai = new AttributeInfo();
				ai.setColumnName(colName);
				ai.setFieldName(naming.createPropertyName(colName));
				ai.setHibernateType(typeMapper.getHibernateType(dbType, javaType));
				ai.setCtClass(typeMapper.getCtClass(dbType, javaType));
				cInfo.addAttribute(ai);
			}
		} catch (SQLException ex){
			throw new RuntimeException(ex);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// do nothing
			}
		}
		
		if (empty) {
			throw new TableNotFoundException(tableName);
		}
		
		try  {
			// locate the primary key
			rs = dmd.getPrimaryKeys(catalog, schema, tableName);
			// 	TODO -- improve this.
			// 	We should check whether or not a suitable pkey exists

			boolean hasPkey = rs.next();
			if (hasPkey) {
				String pkn = rs.getString("COLUMN_NAME");
				for (AttributeInfo ai : cInfo.getAttributes()) {
				if (ai.getColumnName().equals(pkn)) {
					ai.setIdentifier(true);
					break;
				}
			}
			}
		}catch (SQLException e){
			throw new RuntimeException(e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				//do nothing
			}
		}
		return cInfo;
	}

}
