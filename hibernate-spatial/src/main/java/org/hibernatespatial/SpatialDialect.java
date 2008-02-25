/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
 * Copyright © 2007 Geovise BVBA
 * Copyright © 2007 K.U. Leuven LRD, Spatial Applications Division, Belgium
 *
 * This work was partially supported by the European Commission, 
 * under the 6th Framework Programme, contract IST-2-004688-STP.
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
package org.hibernatespatial;

import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.engine.TypedValue;
import org.hibernate.usertype.UserType;

/**
 * @author Karel Maesen
 * 
 * Describes the features of a spatially enabled dialect.
 * 
 */
public interface SpatialDialect {

	/**
	 * Returns the SQL fragment for the SQL WHERE-clause when parsing
	 * <code>org.walkonweb.spatial.criterion.SpatialRelateExpression</code>s
	 * into prepared statements.
	 * 
	 * If useFilter is specified, then a two-stage spatial query model is
	 * assumed (first stage using only spatial index; second stage performing
	 * exact comparisons between geometries). The returned SQL-fragement in that
	 * case should contains two input parameters. The first for setting the
	 * filter geometry, the second for the test geometry.
	 * 
	 * @param columnName
	 *            The name of the geometry-typed column to which the relation is
	 *            applied
	 * @param spatialRelation
	 *            The type of spatial relation (as defined in
	 *            <code>org.walkonweb.spatial.SpatialRelation</code>).
	 * @param useFilter
	 *            If true, the SpatialRelateExpression uses two-stage query
	 *            model
	 * @return - SQL fragment for use in the SQL WHERE-clause.
	 */
	public String getSpatialRelateSQL(String columnName, int spatialRelation,
			boolean useFilter);
	
	/**
	 * Returns the SQL fragment for the SQL WHERE-expression when parsing
	 * <code>org.walkonweb.spatial.criterion.SpatialFilterExpression</code>s
	 * into prepared statements.
	 * 
	 * 
	 * @param columnName-
	 *            the name of the geometry-typed column to which the filter is
	 *            be applied.
	 * @return
	 */
	public String getSpatialFilterExpression(String columnName);

	/**
	 * @return an instance of the Geometry Usertype that this dialect provides
	 */
	public UserType getGeometryUserType();

	/**
	 * @param columnName
	 *            the name of the Geometry property
	 * @param aggregation
	 *            the type of <code>SpatialAggregate</code>
	 * @return the SQL fragment for the projection
	 */
	public String getSpatialAggregateSQL(String columnName, int aggregation);

	/**
	 * Returns the name of the native database type for storing geometries.
	 * 
	 * @return type name
	 */
	public String getDbGeometryTypeName();

	/**
	 * Does this dialect support explicit two-phase filtering when 
	 * filtering on spatial relations?
	 * 
	 * In two-phase filtering you can form a SQL WHERE-expression
	 * that searches for matching objects in two phases. A first phase
	 * performs a quick bounding box search for neighbouring objects. The
	 * second phase calculates the precise spatial relation between the test 
	 * object and the results of the first phase. 
	 * 
	 * Postgis (up to version ??) supports explicit filtering. Oracle and MySQL don't. 
	 * @return
	 */
	public boolean isTwoPhaseFiltering();
}
