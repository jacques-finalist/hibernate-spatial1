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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import org.dom4j.Document;

/**
 * @author Karel Maesen, Geovise BVBA
 * 
 */
public class POJOUtility {

	private final Connection conn;

	private final ClassInfoMap classInfoMap = new ClassInfoMap();

	private final POJOGenerator pojoGenerator;

	private final MappingsGenerator mappingGenerator;

	public POJOUtility(Connection conn, String packageName,
			TypeMapper typeMapper) {

		this.conn = conn;
		this.pojoGenerator = new POJOGenerator(packageName, typeMapper);
		this.mappingGenerator = new MappingsGenerator(packageName);
	}

	// TODO simplify the checked exceptions
	public Document map(Collection<String> tableNames) throws SQLException,
			TableNotFoundException, CannotCompileException, NotFoundException {
		List<TableMetaData> tmds = new ArrayList<TableMetaData>();
		DatabaseMetaData dmd = conn.getMetaData();
		for (String tableName : tableNames) {
			ClassInfo pojoInfo = classInfoMap.getClassInfo(tableName);
			if (pojoInfo != null) {
				continue;
			}
			TableMetaData tmd = TableMetaData.load(tableName, dmd);

			tmds.add(tmd);

			pojoInfo = this.pojoGenerator.createClassInfo(tmd);
			classInfoMap.add(tmd.getName(), pojoInfo);
		}
		try {
			this.mappingGenerator.load(tmds, this.classInfoMap);
		} catch (PKeyNotFoundException e) {
			throw new RuntimeException(e);
		}
		return this.mappingGenerator.getMappingsDoc();
	}

	public ClassInfoMap getClassInfoMap() {
		return this.classInfoMap;
	}

	// TODO -- change this so as to test if the input string is a
	// valid packagename
	private boolean isJavaIdentifier(String in) {
		int n = in.length();
		if (n == 0)
			return false;
		if (!Character.isJavaIdentifierStart(in.charAt(0)))
			return false;
		for (int i = 1; i < n; i++)
			if (!Character.isJavaIdentifierPart(in.charAt(i)))
				return false;
		return true;
	}

}
