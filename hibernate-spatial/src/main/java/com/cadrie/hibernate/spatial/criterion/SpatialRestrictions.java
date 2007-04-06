/**
 * $Id: SpatialRestrictions.java 80 2007-02-01 18:04:02Z maesenka $
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

import com.cadrie.hibernate.spatial.SpatialRelation;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Static Factory Class for creating spatial criterion types.
 * 
 * <p>
 * The criterion types created by this class implement the spatial query
 * expressions of the OpenGIS Simple Features Specification for SQL, Revision
 * 1.1.
 * 
 * In addition, it provides for a simple spatial <code>filter</code> that
 * works mostly using the spatial index. This corresponds to the Oracle
 * Spatial's "SDO_FILTER" function, or the "&&" operator of PostGIS.
 * </p>
 * 
 * 
 * 
 */
public class SpatialRestrictions {

    SpatialRestrictions() {
    }

    public static SpatialRelateExpression eq(String propertyName,
	    Geometry filter, Geometry value) {
	return new SpatialRelateExpression(propertyName, filter, value,
		SpatialRelation.EQUALS);
    }

    public static SpatialRelateExpression within(String propertyName,
	    Geometry filter, Geometry value) {
	return new SpatialRelateExpression(propertyName, filter, value,
		SpatialRelation.WITHIN);
    }

    public static SpatialRelateExpression contains(String propertyName,
	    Geometry filter, Geometry value) {
	return new SpatialRelateExpression(propertyName, filter, value,
		SpatialRelation.CONTAINS);
    }

    public static SpatialRelateExpression crosses(String propertyName,
	    Geometry filter, Geometry value) {
	return new SpatialRelateExpression(propertyName, filter, value,
		SpatialRelation.CROSSES);
    }

    public static SpatialRelateExpression disjoint(String propertyName,
	    Geometry filter, Geometry value) {
	return new SpatialRelateExpression(propertyName, filter, value,
		SpatialRelation.DISJOINT);
    }

    public static SpatialRelateExpression intersects(String propertyName,
	    Geometry filter, Geometry value) {
	return new SpatialRelateExpression(propertyName, filter, value,
		SpatialRelation.INTERSECTS);
    }

    public static SpatialRelateExpression overlaps(String propertyName,
	    Geometry filter, Geometry value) {
	return new SpatialRelateExpression(propertyName, filter, value,
		SpatialRelation.OVERLAPS);
    }

    public static SpatialRelateExpression touches(String propertyName,
	    Geometry filter, Geometry value) {
	return new SpatialRelateExpression(propertyName, filter, value,
		SpatialRelation.TOUCHES);
    }

    public static SpatialFilter filter(String propertyName, Geometry filter) {
	return new SpatialFilter(propertyName, filter);
    }

}
