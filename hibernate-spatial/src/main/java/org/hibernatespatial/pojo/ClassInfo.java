package org.hibernatespatial.pojo;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo {
	
	private final String className;
	private final String tableName;
	private Class pojoClass;
	private final List<AttributeInfo> attributes = new ArrayList<AttributeInfo>();
	
	public ClassInfo(String tableName, String className){
		this.className = className;
		this.tableName = tableName;		
	}
	
	public AttributeInfo getIdAttribute() throws PKeyNotFoundException{
		for (AttributeInfo ai : getAttributes()){
			if (ai.isIdentifier()){
				return ai;				
			}
		}
		throw new PKeyNotFoundException();
	}
	
	public AttributeInfo getGeomAttribute() throws GeometryNotFoundException{
		for (AttributeInfo ai : getAttributes()){
			if (ai.isGeometry()){
				return ai;				
			}
		}
		throw new GeometryNotFoundException();
	}
	
	public List<AttributeInfo> getAttributes() {
		return attributes;
	}

	public String getClassName() {
		return className;
	}


	public String getTableName() {
		return tableName;
	}

	public Class getPOJOClass(){
		return this.pojoClass;
	}
	
	public void setPOJOClass(Class clazz){
		this.pojoClass = clazz;
	}
	
	public void addAttribute(AttributeInfo ai){
		this.attributes.add(ai);
	}
	
	public void removeAttribute(AttributeInfo ai){
		this.attributes.remove(ai);	
	}
	
	public void clearAttributes(){
		this.attributes.clear();
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = PRIME * result + ((className == null) ? 0 : className.hashCode());
		result = PRIME * result + ((tableName == null) ? 0 : tableName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ClassInfo other = (ClassInfo) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}
	
	

}
