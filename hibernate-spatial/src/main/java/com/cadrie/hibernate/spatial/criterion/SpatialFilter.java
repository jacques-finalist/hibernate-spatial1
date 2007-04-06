/**
 * $Id: SpatialFilter.java 80 2007-02-01 18:04:02Z maesenka $
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

package com.cadrie.hibernate.spatial.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.TypedValue;

import com.cadrie.hibernate.spatial.SpatialDialect;
import com.vividsolutions.jts.geom.Geometry;

/**
 * An implementation for a simple spatial filter. This <code>Criterion</code>
 * restricts the resultset to those features whose bounding box overlaps the
 * filter geometry. It is intended for quick, but inexact spatial queries.
 * 
 */
public class SpatialFilter implements Criterion {

    private static final long serialVersionUID = 1L;

    private String propertyName = null;

    private Geometry filter = null;

    public SpatialFilter(String propertyName, Geometry filter) {
	this.propertyName = propertyName;
	this.filter = filter;
    }

    public TypedValue[] getTypedValues(Criteria criteria,
	    CriteriaQuery criteriaQuery) throws HibernateException {
	return new TypedValue[] { criteriaQuery.getTypedValue(criteria,
		propertyName, filter) };
    }

    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
	    throws HibernateException {
	SessionFactoryImplementor factory = criteriaQuery.getFactory();
	String[] columns = criteriaQuery.getColumnsUsingProjection(criteria,
		this.propertyName);
	Dialect dialect = factory.getDialect();
	if (dialect instanceof SpatialDialect) {
	    SpatialDialect seDialect = (SpatialDialect) dialect;
	    return seDialect.getSpatialFilterExpression(columns[0]);
	} else
	    throw new IllegalStateException(
		    "Dialect must be spatially enabled dialect");

    }

}
