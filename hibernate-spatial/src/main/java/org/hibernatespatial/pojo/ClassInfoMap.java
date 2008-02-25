package org.hibernatespatial.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>ClassInfoMap</code> maps tableNames
 * to <code>ClassInfo</code> instances.
 * 
 * @author Karel Maesen, Geovise BVBA.
 */
public class ClassInfoMap {
	
	private final Map<String, ClassInfo> map = new HashMap<String, ClassInfo>();
	
	public void add(String tableName, ClassInfo cinfo){
		map.put(tableName.toLowerCase(), cinfo);
	}

	public ClassInfo getClassInfo(String tableName){
		return map.get(tableName.toLowerCase());
	}
	
}
