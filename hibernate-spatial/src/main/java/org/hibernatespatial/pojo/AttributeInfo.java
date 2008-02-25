package org.hibernatespatial.pojo;

import java.lang.reflect.Method;

import org.hibernatespatial.GeometryUserType;

public class AttributeInfo {
	
	private String columnName;
	private String fieldName;
	private String hibernateType;
	private boolean isIdentifier;
	private Method getter;
	
	public boolean isGeometry() {
		return this.hibernateType.equalsIgnoreCase(GeometryUserType.class.getCanonicalName());
	}
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getHibernateType() {
		return hibernateType;
	}
	public void setHibernateType(String hibernateType) {
		this.hibernateType = hibernateType;
	}
	public boolean isIdentifier() {
		return isIdentifier;
	}
	public void setIdentifier(boolean isIdentifier) {
		this.isIdentifier = isIdentifier;
	}


	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = PRIME * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = PRIME * result + ((hibernateType == null) ? 0 : hibernateType.hashCode());
		result = PRIME * result + (isIdentifier ? 1231 : 1237);
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
		final AttributeInfo other = (AttributeInfo) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (hibernateType == null) {
			if (other.hibernateType != null)
				return false;
		} else if (!hibernateType.equals(other.hibernateType))
			return false;
		if (isIdentifier != other.isIdentifier)
			return false;
		return true;
	}
	
	

}
