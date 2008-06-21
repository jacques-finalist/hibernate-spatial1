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
package org.hibernatespatial.helper;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.property.Getter;
import org.hibernate.type.Type;
import org.hibernate.util.ReflectHelper;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A wrapper class around Hibernate's <code>ClassMetadata</code>. It adds
 * methods for getting the Geometry property and the getters for the geometry
 * and identifier propreties.
 * 
 * @author Karel Maesen
 * 
 */
public class HSClassMetadata implements ClassMetadata {

	private final ClassMetadata metadata;

	private FinderStrategy<String, ClassMetadata> geomPropertyFinder = new FindGeomProperty();

	public HSClassMetadata(ClassMetadata metadata) {
		this.metadata = metadata;
	}

	public void setGeomPropertyFinder(
			FinderStrategy<String, ClassMetadata> finder) {
		this.geomPropertyFinder = finder;
	}

	public String getGeometryPropertyName() throws FinderException {
		return this.geomPropertyFinder.find(this.metadata);
	}

	public Method getGeomGetter() {
		try {
			String prop = getGeometryPropertyName();
			return getGetterFor(prop);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Method getIdGetter() {
		String prop = getIdentifierPropertyName();
		return getGetterFor(prop);
	}

	public Method getGetterFor(String property) {
		Class cl = this.metadata.getMappedClass(EntityMode.POJO);
		Getter getter = ReflectHelper.getGetter(cl, property);
		return getter.getMethod();
	}

	// delegate methods

	public String getEntityName() {
		return metadata.getEntityName();
	}

	public Serializable getIdentifier(Object entity, EntityMode entityMode)
			throws HibernateException {
		return metadata.getIdentifier(entity, entityMode);
	}

	public String getIdentifierPropertyName() {
		return metadata.getIdentifierPropertyName();
	}

	public Type getIdentifierType() {
		return metadata.getIdentifierType();
	}

	public Class getMappedClass(EntityMode entityMode) {
		return metadata.getMappedClass(entityMode);
	}

	public int[] getNaturalIdentifierProperties() {
		return metadata.getNaturalIdentifierProperties();
	}

	public boolean[] getPropertyLaziness() {
		return metadata.getPropertyLaziness();
	}

	public String[] getPropertyNames() {
		return metadata.getPropertyNames();
	}

	public boolean[] getPropertyNullability() {
		return metadata.getPropertyNullability();
	}

	public Type getPropertyType(String propertyName) throws HibernateException {
		return metadata.getPropertyType(propertyName);
	}

	public Type[] getPropertyTypes() {
		return metadata.getPropertyTypes();
	}

	public Object getPropertyValue(Object object, String propertyName,
			EntityMode entityMode) throws HibernateException {
		return metadata.getPropertyValue(object, propertyName, entityMode);
	}

	public Object[] getPropertyValues(Object entity, EntityMode entityMode)
			throws HibernateException {
		return metadata.getPropertyValues(entity, entityMode);
	}

	public Object[] getPropertyValuesToInsert(Object entity, Map mergeMap,
			SessionImplementor session) throws HibernateException {
		return metadata.getPropertyValuesToInsert(entity, mergeMap, session);
	}

	public Object getVersion(Object object, EntityMode entityMode)
			throws HibernateException {
		return metadata.getVersion(object, entityMode);
	}

	public int getVersionProperty() {
		return metadata.getVersionProperty();
	}

	public boolean hasIdentifierProperty() {
		return metadata.hasIdentifierProperty();
	}

	public boolean hasNaturalIdentifier() {
		return metadata.hasNaturalIdentifier();
	}

	public boolean hasProxy() {
		return metadata.hasProxy();
	}

	public boolean hasSubclasses() {
		return metadata.hasSubclasses();
	}

	public boolean implementsLifecycle(EntityMode entityMode) {
		return metadata.implementsLifecycle(entityMode);
	}

	public boolean implementsValidatable(EntityMode entityMode) {
		return metadata.implementsValidatable(entityMode);
	}

	public Object instantiate(Serializable id, EntityMode entityMode)
			throws HibernateException {
		return metadata.instantiate(id, entityMode);
	}

	public boolean isInherited() {
		return metadata.isInherited();
	}

	public boolean isMutable() {
		return metadata.isMutable();
	}

	public boolean isVersioned() {
		return metadata.isVersioned();
	}

	public void setIdentifier(Object object, Serializable id,
			EntityMode entityMode) throws HibernateException {
		metadata.setIdentifier(object, id, entityMode);
	}

	public void setPropertyValue(Object object, String propertyName,
			Object value, EntityMode entityMode) throws HibernateException {
		metadata.setPropertyValue(object, propertyName, value, entityMode);
	}

	public void setPropertyValues(Object object, Object[] values,
			EntityMode entityMode) throws HibernateException {
		metadata.setPropertyValues(object, values, entityMode);
	}

	/**
	 * This <code>FinderStrategy</code> implementation returns the first
	 * geometry-valued property.
	 * 
	 */
	private static class FindGeomProperty implements
			FinderStrategy<String, ClassMetadata> {

		public String find(ClassMetadata metadata) throws FinderException {
			for (String prop : metadata.getPropertyNames()) {
				Type type = metadata.getPropertyType(prop);

				if (Geometry.class.isAssignableFrom(type.getReturnedClass())) {
					return prop;
				}
			}
			throw new FinderException(
					"Could not find a Geometry-valued property in "
							+ metadata.getEntityName());
		}
	}

}
