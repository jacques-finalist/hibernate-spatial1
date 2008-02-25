package org.hibernatespatial.pojo;


/**
 * The <code>ColumnMetaData</code> collects information w.r.t.
 * a column in a table, including all information
 * necessary to derive a suitable POJO and Hibernate Mapping
 * for the table.
 * 
 * @author Karel Maesen, Geovise BVBA (http://www.geovise.com/)
 */
public class ColumnMetaData{
	private String name;
	private int javaType;//java.sql.Types
	private String dbType; 
	private boolean pkey = false;
	

	/**
	 * @return the dbType
	 */
	public String getDbType() {
		return dbType;
	}
	/**
	 * @param dbType the dbType to set
	 */
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	/**
	 * @return the javaType
	 */
	public int getJavaType() {
		return javaType;
	}
	/**
	 * @param javaType the javaType to set
	 */
	public void setJavaType(int javaType) {
		this.javaType = javaType;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPkey(boolean ispk){
		this.pkey = ispk;
	}
	
	public boolean isPkey(){
		return this.pkey;
	}
	
	
}