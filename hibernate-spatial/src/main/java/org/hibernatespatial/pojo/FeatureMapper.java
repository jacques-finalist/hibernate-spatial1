package org.hibernatespatial.pojo;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FeatureMapper {

    private final NamingStrategy naming;
    private final TypeMapper typeMapper;

    public FeatureMapper(NamingStrategy naming, TypeMapper typeMapper) {
        this.naming = naming;
        this.typeMapper = typeMapper;
    }

    public ClassInfo createClassInfo(String catalog, String schema, String tableName, DatabaseMetaData dmd) throws TableNotFoundException, PKeyException {

        String className = naming.createClassName(tableName);
        ClassInfo cInfo = new ClassInfo(tableName, className);
        ResultSet rs = readColums(catalog, schema, tableName, dmd, cInfo);
        determinePrimaryKey(catalog, schema, tableName, dmd, cInfo, rs);
        return cInfo;
    }

    private void determinePrimaryKey(String catalog, String schema, String tableName, DatabaseMetaData dmd, ClassInfo cInfo, ResultSet rs) throws CompositePKeyException, PKeyNotFoundException {
        try {
            rs = dmd.getPrimaryKeys(catalog, schema, tableName);
            if(!rs.next()) throw new PKeyNotFoundException(tableName);            
            String pkn = rs.getString("COLUMN_NAME");
            //check whether the primary key is non-composite
            if (rs.next()) throw new CompositePKeyException(tableName);
            setAsIdentifier(cInfo, pkn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                //do nothing
            }
        }
    }

    private ResultSet readColums(String catalog, String schema, String tableName, DatabaseMetaData dmd, ClassInfo cInfo) throws TableNotFoundException {
        ResultSet rs = null;
        boolean empty = true;
        try {
            rs = dmd.getColumns(catalog, schema, tableName, null);
            while (rs.next()) {
                empty = false;
                String colName = rs.getString("COLUMN_NAME");
                String dbType = rs.getString("TYPE_NAME");
                int javaType = rs.getInt("DATA_TYPE");
                addAttribute(cInfo, colName, dbType, javaType);
            }
        } catch (SQLException ex) {
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
        return rs;
    }

    private void setAsIdentifier(ClassInfo cInfo, String pkn) {
        for (AttributeInfo ai : cInfo.getAttributes()) {
            if (ai.getColumnName().equals(pkn)) {
                ai.setIdentifier(true);
                break;
            }
        }
    }

    private void addAttribute(ClassInfo cInfo, String colName, String dbType, int javaType) {
        String hibernateType = null;
        try {
            hibernateType = typeMapper.getHibernateType(dbType, javaType);
        } catch (TypeNotFoundException e) {

        }
        AttributeInfo ai = new AttributeInfo();
        ai.setColumnName(colName);
        ai.setFieldName(naming.createPropertyName(colName));
        ai.setHibernateType(hibernateType);
        ai.setCtClass(typeMapper.getCtClass(dbType, javaType));
        cInfo.addAttribute(ai);
    }

}
