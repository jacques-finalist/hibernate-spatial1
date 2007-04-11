/**
 * $Id$
 *
 * This file is part of MAJAS (Mapping with Asynchronous JavaScript and ASVG). a
 * framework for Rich Internet GIS Applications.
 *
 * Copyright  © 2007 DFC Software Engineering, Belgium
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

import org.hibernate.usertype.UserType;

/**
 * @author Karel Maesen, K.U.Leuven R&D Divisie SADL
 * @version $Id$
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

    public UserType getGeometryUserType();

}
