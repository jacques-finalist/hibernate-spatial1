/**
 * $Id: GeometryUserType.java 79 2007-02-01 18:03:43Z maesenka $
 *
 * This file is part of MAJAS (Mapping with Asynchronous JavaScript and ASVG). a
 * framework for Rich Internet GIS Applications.
 *
 * Copyright © 2007 DFC Software Engineering, Belgium
 * and K.U. Leuven LRD, Spatial Applications Division, Belgium
 *
 * MAJAS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MAJAS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with gGIS; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.cadrie.hibernate.spatial;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

/**
 * This class ensures that Hibernate on top of PostgreSQL/PostGIS can work with
 * the JTS <code>Geometry</code> type.
 * 
 * To properly convert <code>Geometry</code> objects to database specific
 * wrapper objects, acces is needed to a spatially enabled database dialect.
 * This dialect can be specified as a parameter to the type. If no parameter is
 * supplied, the default Dialect will be used (set in HBSpatialExtension).
 */
public class GeometryUserType implements UserType, ParameterizedType {

    private Properties properties = null;

    private SpatialDialect spatialDialect = null;

    private UserType delegate = null;

    public static String DIALECT_PARAM_NAME = "dialect";

    private void configureDialect() {
	if (properties == null) {
	    spatialDialect = HBSpatialExtension.getDefaultSpatialDialect();
	} else {
	    spatialDialect = HBSpatialExtension.createSpatialDialect(properties
		    .getProperty(DIALECT_PARAM_NAME), properties);
	}
	if (spatialDialect == null) {
	    throw new HibernateSpatialException(
		    "No spatial Dialect could be created");
	}
	delegate = spatialDialect.getGeometryUserType();
    }

    /**
         * @param arg0
         * @param arg1
         * @return
         * @throws HibernateException
         * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable,
         *      java.lang.Object)
         */
    public Object assemble(Serializable arg0, Object arg1)
	    throws HibernateException {
	return delegate.assemble(arg0, arg1);
    }

    /**
         * @param arg0
         * @return
         * @throws HibernateException
         * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
         */
    public Object deepCopy(Object arg0) throws HibernateException {
	return delegate.deepCopy(arg0);
    }

    /**
         * @param arg0
         * @return
         * @throws HibernateException
         * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
         */
    public Serializable disassemble(Object arg0) throws HibernateException {
	return delegate.disassemble(arg0);
    }

    /**
         * @param arg0
         * @param arg1
         * @return
         * @throws HibernateException
         * @see org.hibernate.usertype.UserType#equals(java.lang.Object,
         *      java.lang.Object)
         */
    public boolean equals(Object arg0, Object arg1) throws HibernateException {
	return delegate.equals(arg0, arg1);
    }

    /**
         * @param obj
         * @return
         * @see java.lang.Object#equals(java.lang.Object)
         */
    public boolean equals(Object obj) {
	return delegate.equals(obj);
    }

    /**
         * @return
         * @see java.lang.Object#hashCode()
         */
    public int hashCode() {
	return delegate.hashCode();
    }

    /**
         * @param arg0
         * @return
         * @throws HibernateException
         * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
         */
    public int hashCode(Object arg0) throws HibernateException {
	return delegate.hashCode(arg0);
    }

    /**
         * @return
         * @see org.hibernate.usertype.UserType#isMutable()
         */
    public boolean isMutable() {
	return delegate.isMutable();
    }

    /**
         * @param arg0
         * @param arg1
         * @param arg2
         * @return
         * @throws HibernateException
         * @throws SQLException
         * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet,
         *      java.lang.String[], java.lang.Object)
         */
    public Object nullSafeGet(ResultSet arg0, String[] arg1, Object arg2)
	    throws HibernateException, SQLException {
	return delegate.nullSafeGet(arg0, arg1, arg2);
    }

    /**
         * @param arg0
         * @param arg1
         * @param arg2
         * @throws HibernateException
         * @throws SQLException
         * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement,
         *      java.lang.Object, int)
         */
    public void nullSafeSet(PreparedStatement arg0, Object arg1, int arg2)
	    throws HibernateException, SQLException {
	delegate.nullSafeSet(arg0, arg1, arg2);
    }

    /**
         * @param arg0
         * @param arg1
         * @param arg2
         * @return
         * @throws HibernateException
         * @see org.hibernate.usertype.UserType#replace(java.lang.Object,
         *      java.lang.Object, java.lang.Object)
         */
    public Object replace(Object arg0, Object arg1, Object arg2)
	    throws HibernateException {
	return delegate.replace(arg0, arg1, arg2);
    }

    /**
         * @return
         * @see org.hibernate.usertype.UserType#returnedClass()
         */
    public Class returnedClass() {
	return delegate.returnedClass();
    }

    /**
         * @return
         * @see org.hibernate.usertype.UserType#sqlTypes()
         */
    public int[] sqlTypes() {
	return delegate.sqlTypes();
    }

    /**
         * @return
         * @see java.lang.Object#toString()
         */
    public String toString() {
	return delegate.toString();
    }

    public void setParameterValues(Properties properties) {
	this.properties = properties;
	configureDialect();
    }

}
