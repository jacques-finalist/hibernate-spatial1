package org.hibernatespatial.pojo;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;

import org.hibernatespatial.GeometryUserType;

/**
 * The <code>TypeMapper</code> maps a pair consisting of java.sql.Type, and a database type name 
 * to a CtClass (a representation of a java type used by the javassist class building tools)
 * and to a Hibernate type (used when creating a mapping file).
 * 
 * @author Karel Maesen, Geovise BVBA (http://www.geovise.com/)
 */
public class TypeMapper {
	
	private final static String GEOMETRY_USER_TYPE = GeometryUserType.class.getCanonicalName();
	
	private  List<TMEntry> entries = new ArrayList<TMEntry>();
	private String dbGeomType = "";
	private CtClass ctGeom;
	
	//TODO -- create entires for all constants defined in java.sql.Types
	public TypeMapper (String dbGeomType) {
		
		//first set the type to use for the geometry
		this.dbGeomType = dbGeomType;
		
		ClassPool pool  = ClassPool.getDefault();
		//ensure that we can load the JTS classes.
		pool.insertClassPath(new ClassClassPath(this.getClass()));	
		
		CtClass ctString = null;
		CtClass ctDate = null;		
		try{
			ctString = pool.get("java.lang.String");
			ctDate = pool.get("java.util.Date");
			ctGeom = pool.get("com.vividsolutions.jts.geom.Geometry");
		} catch(Exception e){
			throw new RuntimeException(e);
		}

		
		entries.add(new TMEntry(
					Types.BIGINT,
					"integer",
					CtClass.longType));
		
		entries.add(new TMEntry(
				Types.SMALLINT,
				"integer",
				CtClass.intType));		
		
		entries.add(new TMEntry(
				Types.BOOLEAN,
				"boolean",
				CtClass.booleanType));		
		
		entries.add(new TMEntry(
				Types.CHAR,
				"string",
				ctString));
		
		entries.add(new TMEntry(
				Types.DATE,
				"date",
				ctDate));
		
		entries.add(new TMEntry(
				Types.DECIMAL,
				"double",
				CtClass.doubleType));
		
		entries.add(new TMEntry(
				Types.DOUBLE,
				"double",
				CtClass.doubleType));
		
		entries.add(new TMEntry(
				Types.NUMERIC,
				"double",
				CtClass.doubleType));

		entries.add(new TMEntry(
				Types.FLOAT,
				"double",
				CtClass.doubleType));

		entries.add(new TMEntry(
				Types.INTEGER,
				"long",
				CtClass.longType));

		entries.add(new TMEntry(
				Types.VARCHAR,
				"string",
				ctString));
	}
	
	public CtClass getCtClass(String dbType, int sqlType){
		if (dbType.equalsIgnoreCase(this.dbGeomType)){
			return this.ctGeom;
		}
		for (TMEntry entry : entries){
			if (entry.javaType == sqlType){
				return entry.ctClass;
			}
		}
		return null;
	}
	
	public String getHibernateType(String dbType, int sqlType){
		if (dbType.equalsIgnoreCase(this.dbGeomType)){
			return GEOMETRY_USER_TYPE;
		}
		for (TMEntry entry : entries){
			if (entry.javaType == sqlType){
				return entry.hibernateTypeName;
			}
		}
		return null;
		
	}
	
	public int[] getMappedSQLTypes(){
		int l = this.entries.size();
		int[] types = new int[l];
		for(int i = 0; i < this.entries.size(); i++){
			types[i] = this.entries.get(i).javaType;
		}
		return types;
	}
	
	public void addTypeMapping(int sqlType, String hibernateType, CtClass ctClass){
		this.entries.add(new TMEntry(sqlType, hibernateType, ctClass));
	}
	
	public void removeTypeMapping(int sqlType){
		TMEntry tm = null;
		for (TMEntry t : this.entries){
			if (t.javaType == sqlType){
				tm = t;
				break;
			}
		}
		if (tm != null){
			this.entries.remove(tm);
		}
	}
	
	private static class TMEntry{
		protected int javaType = 0;
		protected String hibernateTypeName = "";
		protected CtClass ctClass;
		
		protected TMEntry(int jt, String ht, CtClass jc){
			this.javaType = jt;
			this.hibernateTypeName = ht;
			this.ctClass = jc;
		}
	}
	
	
	
	

}
