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

import org.hibernatespatial.GeometryUserType;

/**
 * @author Karel Maesen
 * 
 * 
 */
public class AttributeInfo {

	private String columnName;

	private String fieldName;

	private String hibernateType;

	private boolean isIdentifier;

	public boolean isGeometry() {
		return this.hibernateType.equalsIgnoreCase(GeometryUserType.class
				.getCanonicalName());
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
		result = PRIME * result
				+ ((columnName == null) ? 0 : columnName.hashCode());
		result = PRIME * result
				+ ((fieldName == null) ? 0 : fieldName.hashCode());
		result = PRIME * result
				+ ((hibernateType == null) ? 0 : hibernateType.hashCode());
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
