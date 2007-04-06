package com.cadrie.hibernate.spatial.oracle.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class TestAdHoc {

    /**
         * @param args
     * @throws Exception 
         */
    public static void main(String[] args) throws Exception {
	String DBURL = "jdbc:oracle:thin:@//10.33.211.85/ORCL";
	String DBNAME = "spatialtest";
	String DBPASSWD = "spatialtest";

	String sdo_geom_filter = "SDO_GEOMETRY("
		+ " 2003,"
		+ " 31370,"
		+ " NULL,"
		+ " SDO_ELEM_INFO_ARRAY(1,1003,1), "
		+ " SDO_ORDINATE_ARRAY(0,0, 25000,0, 25000,25000, 0,25000, 0,0))";

	Connection conn  = null;
	Statement stmt = null;
	ResultSet rs = null;
	try {
	    Class.forName("oracle.jdbc.driver.OracleDriver");
	    conn = DriverManager.getConnection(DBURL, DBNAME, DBPASSWD);
	    
	    stmt = conn.createStatement();
	    //rs = stmt.executeQuery("select id, geom from multilinestringtest");
	    rs = stmt.executeQuery("select * from user_sdo_geom_metadata");
	    while (rs.next()){
		String name = rs.getString(1);
		oracle.sql.ARRAY dimInfo = (oracle.sql.ARRAY)rs.getObject(3);
		Object[] dims = (Object[])dimInfo.getArray();
		STRUCT dimX = (STRUCT)dims[0];
		BigDecimal tolerance1 = (BigDecimal)dimX.getAttributes()[3];
		System.out.println(name + " : " + tolerance1);
	    }
	    
	    
	    JGeometry jgeom;
	} catch (Exception e) {
	    e.printStackTrace();
	}finally{
	    rs.close();
	    stmt.close();
	    conn.close();
	}

	
	

    }

}
