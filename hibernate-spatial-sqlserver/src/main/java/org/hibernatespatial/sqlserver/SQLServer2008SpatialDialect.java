/*
 * $Id:$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2009 Geovise BVBA
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

package org.hibernatespatial.sqlserver;

import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.usertype.UserType;
import org.hibernatespatial.SpatialDialect;

/**
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
public class SQLServer2008SpatialDialect extends SQLServerDialect implements SpatialDialect {

    public String getSpatialRelateSQL(String s, int i, boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getSpatialFilterExpression(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public UserType getGeometryUserType() {
        return new SQLServer2008GeometryUserType();
    }

    public String getSpatialAggregateSQL(String s, int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDbGeometryTypeName() {
        return "GEOMETRY";
    }

    public boolean isTwoPhaseFiltering() {
        return false;
    }
}
