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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>TableMetaData</code> instances hold metadata on a specific database
 * table.
 * 
 * @author Karel Maesen, Geovise BVBA (http://www.geovise.com/)
 */
public class TableMetaData {

	private String name;

	private List<ColumnMetaData> columns = new ArrayList<ColumnMetaData>();

	/**
	 * Adds the metadata for the specified table a <code>TableMetaData</code>
	 * instance.
	 * 
	 * @param tableName -
	 *            name for which metadata is loaded
	 * @param dmd -
	 *            JDBC database metadata object
	 * @return a tableMetaData instance for the specified table.
	 * @throws SQLException
	 * @throws TableNotFoundException
	 */
	public static TableMetaData load(String tableName, DatabaseMetaData dmd)
			throws SQLException, TableNotFoundException {
		ResultSet rs = null;
		// read the data
		try {
			rs = dmd.getColumns(null, null, tableName, null);
			TableMetaData tmd = new TableMetaData();
			boolean isLoaded = false;
			while (rs.next()) {
				if (tmd.getName() == null) {
					tmd.setName((String) rs.getObject("TABLE_NAME"));
				}
				ColumnMetaData column = new ColumnMetaData();
				column.setName(rs.getString("COLUMN_NAME"));
				column.setDbType(rs.getString("TYPE_NAME"));
				column.setJavaType(rs.getInt("DATA_TYPE"));
				tmd.addColumn(column);
				isLoaded = true;
			}
			rs.close();
			if (!isLoaded) {
				throw new TableNotFoundException(tableName);
			}

			// locate the primary key
			rs = dmd.getPrimaryKeys(null, null, tableName);
			// TODO -- improve this.
			// We should check whether or not a suitable pkey exists

			boolean hasPkey = rs.next();
			if (hasPkey) {
				String pkn = rs.getString("COLUMN_NAME");
				for (ColumnMetaData c : tmd.getColumns()) {
					if (c.getName().equals(pkn)) {
						c.setPkey(true);
						break;
					}
				}
			}
			return tmd;
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * @return the columns
	 */
	public List<ColumnMetaData> getColumns() {
		return columns;
	}

	private void addColumn(ColumnMetaData column) {
		this.columns.add(column);
	}

	public ColumnMetaData getPrimaryKeyColumn() throws PKeyNotFoundException {

		for (ColumnMetaData c : getColumns()) {
			if (c.isPkey()) {
				return c;
			}
		}
		throw new PKeyNotFoundException(getName());
	}

	/**
	 * @return the table name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name of the table
	 */
	private void setName(String name) {
		this.name = name;
	}

}