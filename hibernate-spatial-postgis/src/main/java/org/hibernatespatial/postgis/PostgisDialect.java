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
package org.hibernatespatial.postgis;


import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.CustomType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.usertype.UserType;
import org.hibernatespatial.SpatialAggregate;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.SpatialFunction;
import org.hibernatespatial.SpatialRelation;

/**
 * Extends the PostgreSQLDialect by also including information on spatial
 * operators, constructors and processing functions.
 *
 * @author Karel Maesen
 */
public class PostgisDialect extends PostgreSQLDialect implements SpatialDialect {

    private static final Type geometryCustomType = new CustomType(new PGGeometryUserType(), new String[]{"postgis_geometry"});

    public PostgisDialect() {
        super();
        registerColumnType(java.sql.Types.STRUCT, "geometry");

        // registering OGC functions
        // (spec_simplefeatures_sql_99-04.pdf)

        // section 2.1.1.1
        // Registerfunction calls for registering geometry functions:
        // first argument is the OGC standard functionname, second the name as
        // it occurs in the spatial dialect
        registerFunction("dimension", new StandardSQLFunction("dimension",
                StandardBasicTypes.INTEGER));
        registerFunction("geometrytype", new StandardSQLFunction(
                "geometrytype", StandardBasicTypes.STRING));
        registerFunction("srid", new StandardSQLFunction("srid",
                StandardBasicTypes.INTEGER));
        registerFunction("envelope", new StandardSQLFunction("envelope",
                geometryCustomType));
        registerFunction("astext", new StandardSQLFunction("astext",
                StandardBasicTypes.STRING));
        registerFunction("asbinary", new StandardSQLFunction("asbinary",
                StandardBasicTypes.BINARY));
        registerFunction("isempty", new StandardSQLFunction("isempty",
                StandardBasicTypes.BOOLEAN));
        registerFunction("issimple", new StandardSQLFunction("issimple",
                StandardBasicTypes.BOOLEAN));
        registerFunction("boundary", new StandardSQLFunction("boundary",
                geometryCustomType));

        // Register functions for spatial relation constructs
        registerFunction("overlaps", new StandardSQLFunction("overlaps",
                StandardBasicTypes.BOOLEAN));
        registerFunction("intersects", new StandardSQLFunction("intersects",
                StandardBasicTypes.BOOLEAN));
        registerFunction("equals", new StandardSQLFunction("equals",
                StandardBasicTypes.BOOLEAN));
        registerFunction("contains", new StandardSQLFunction("contains",
                StandardBasicTypes.BOOLEAN));
        registerFunction("crosses", new StandardSQLFunction("crosses",
                StandardBasicTypes.BOOLEAN));
        registerFunction("disjoint", new StandardSQLFunction("disjoint",
                StandardBasicTypes.BOOLEAN));
        registerFunction("touches", new StandardSQLFunction("touches",
                StandardBasicTypes.BOOLEAN));
        registerFunction("within", new StandardSQLFunction("within",
                StandardBasicTypes.BOOLEAN));
        registerFunction("relate", new StandardSQLFunction("relate",
                StandardBasicTypes.BOOLEAN));

        // register the spatial analysis functions
        registerFunction("distance", new StandardSQLFunction("distance",
                StandardBasicTypes.DOUBLE));
        registerFunction("buffer", new StandardSQLFunction("buffer",
                geometryCustomType));
        registerFunction("convexhull", new StandardSQLFunction("convexhull",
                geometryCustomType));
        registerFunction("difference", new StandardSQLFunction("difference",
                geometryCustomType));
        registerFunction("intersection", new StandardSQLFunction(
                "intersection", geometryCustomType));
        registerFunction("symdifference",
                new StandardSQLFunction("symdifference", geometryCustomType));
        registerFunction("geomunion", new StandardSQLFunction("geomunion",
                geometryCustomType));

        //register Spatial Aggregate funciton
        registerFunction("extent", new StandardSQLFunction("extent",
                geometryCustomType));

    }

    /*
      * (non-Javadoc)
      *
      * @see org.walkonweb.spatial.dialect.SpatialEnabledDialect#getSpatialRelateExpression(java.lang.String,
      *      int, boolean)
      */

    public String getSpatialRelateSQL(String columnName, int spatialRelation,
                                      boolean hasFilter) {
        switch (spatialRelation) {
            case SpatialRelation.WITHIN:
                return hasFilter ? "(" + columnName + " && ?  AND   within("
                        + columnName + ", ?))" : " within(" + columnName + ",?)";
            case SpatialRelation.CONTAINS:
                return hasFilter ? "(" + columnName + " && ? AND contains("
                        + columnName + ", ?))" : " contains(" + columnName + ", ?)";
            case SpatialRelation.CROSSES:
                return hasFilter ? "(" + columnName + " && ? AND crosses("
                        + columnName + ", ?))" : " crosses(" + columnName + ", ?)";
            case SpatialRelation.OVERLAPS:
                return hasFilter ? "(" + columnName + " && ? AND overlaps("
                        + columnName + ", ?))" : " overlaps(" + columnName + ", ?)";
            case SpatialRelation.DISJOINT:
                return hasFilter ? "(" + columnName + " && ? AND disjoint("
                        + columnName + ", ?))" : " disjoint(" + columnName + ", ?)";
            case SpatialRelation.INTERSECTS:
                return hasFilter ? "(" + columnName + " && ? AND intersects("
                        + columnName + ", ?))" : " intersects(" + columnName
                        + ", ?)";
            case SpatialRelation.TOUCHES:
                return hasFilter ? "(" + columnName + " && ? AND touches("
                        + columnName + ", ?))" : " touches(" + columnName + ", ?)";
            case SpatialRelation.EQUALS:
                return hasFilter ? "(" + columnName + " && ? AND equals("
                        + columnName + ", ?))" : " equals(" + columnName + ", ?)";
            default:
                throw new IllegalArgumentException(
                        "Spatial relation is not known by this dialect");
        }

    }

    /*
      * (non-Javadoc)
      *
      * @see org.walkonweb.spatial.dialect.SpatialEnabledDialect#getSpatialFilterExpression(java.lang.String)
      */

    public String getSpatialFilterExpression(String columnName) {
        return "(" + columnName + " && ? ) ";
    }

    /*
      * (non-Javadoc)
      *
      * @see org.hibernatespatial.SpatialDialect#getGeometryUserType()
      */

    public UserType getGeometryUserType() {
        return new PGGeometryUserType();
    }

    /*
      * (non-Javadoc)
      *
      * @see org.hibernatespatial.SpatialDialect#getSpatialAggregateSQL(java.lang.String,
      *      int)
      */

    public String getSpatialAggregateSQL(String columnName, int aggregation) {
        switch (aggregation) {
            case SpatialAggregate.EXTENT:
                StringBuilder stbuf = new StringBuilder();
                stbuf.append("extent(").append(columnName).append(")");
                return stbuf.toString();
            default:
                throw new IllegalArgumentException("Aggregation of type "
                        + aggregation + " are not supported by this dialect");
        }
    }

    public String getDbGeometryTypeName() {
        return "GEOMETRY";
    }

    public boolean isTwoPhaseFiltering() {
        return true;
    }

    public boolean supports(SpatialFunction function) {
        return true;
    }

}
