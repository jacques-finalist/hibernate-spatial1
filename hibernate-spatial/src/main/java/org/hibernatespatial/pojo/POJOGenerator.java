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

import java.util.HashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/**
 * A generator that creates POJO classes that correspond to database tables.
 * 
 * 
 * @author Karel Maesen, Geovise BVBA (http://www.geovise.com/)
 */
public class POJOGenerator {

	private final ClassPool pool = ClassPool.getDefault();

	private NamingStrategy naming = new SimpleNamingStrategy();

	private final String packageName;

	private final TypeMapper typeMapper;

	public POJOGenerator(String packageName, TypeMapper typeMapper) {
		this.packageName = packageName;
		this.typeMapper = typeMapper;
	}

	public void setNamingStrategy(NamingStrategy strategy) {
		this.naming = strategy;
	}

	public ClassInfo createClassInfo(TableMetaData tmd)
			throws CannotCompileException, NotFoundException {
		String classname = packageName + "."
				+ naming.createClassName(tmd.getName());
		CtClass pojo = pool.makeClass(classname);
		ClassInfo classInfo = new ClassInfo(tmd.getName(), classname);
		Map<String, String> fldGetterMap = new HashMap<String, String>();
		for (ColumnMetaData cmd : tmd.getColumns()) {
			CtField field = createField(cmd, pojo);
			CtMethod getter = createGetterMethod(field);
			CtMethod setter = createSetterMethod(field);
			pojo.addField(field);
			pojo.addMethod(getter);
			pojo.addMethod(setter);
			AttributeInfo ai = new AttributeInfo();
			ai.setColumnName(cmd.getName());
			ai.setFieldName(field.getName());
			ai.setHibernateType(typeMapper.getHibernateType(cmd.getDbType(),
					cmd.getJavaType()));
			ai.setIdentifier(cmd.isPkey());
			classInfo.addAttribute(ai);
			fldGetterMap.put(field.getName(), getter.getName());

		}
		classInfo.setPOJOClass(pojo.toClass());
		return classInfo;
	}

	public String getPackageName() {
		return this.packageName;
	}

	private CtMethod createGetterMethod(CtField field)
			throws CannotCompileException {
		String fn = field.getName();
		return CtNewMethod.getter(this.naming.createGetterName(fn), field);
	}

	private CtMethod createSetterMethod(CtField field)
			throws CannotCompileException {
		String fn = field.getName();
		return CtNewMethod.setter(naming.createSetterName(fn), field);
	}

	private CtField createField(ColumnMetaData cmd, CtClass declaring)
			throws CannotCompileException {
		CtClass type = typeMapper
				.getCtClass(cmd.getDbType(), cmd.getJavaType());
		if (type == null) {
			throw new RuntimeException("Failed to find type for column "
					+ cmd.getName());
		}
		CtField f = new CtField(type, naming.createPropertyName(cmd.getName()),
				declaring);
		return f;
	}

}
