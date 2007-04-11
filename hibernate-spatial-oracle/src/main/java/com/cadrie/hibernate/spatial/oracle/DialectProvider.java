/**
 * $Id$
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

package com.cadrie.hibernate.spatial.oracle;

import java.util.Map;

import com.cadrie.hibernate.spatial.SpatialDialect;
import com.cadrie.hibernate.spatial.spi.SpatialDialectProvider;

/**
 * Oracle10g DialectProvider.
 * 
 */
public class DialectProvider implements SpatialDialectProvider {

    /*
     * (non-Javadoc)
     * 
     * @see com.cadrie.hibernate.spatial.spi.SpatialDialectProvider#createSpatialDialect(java.lang.String,
     *      java.util.Map)
     */
    public final SpatialDialect createSpatialDialect(final String dialect,
            final Map map) {

        if (dialect.equals(OracleSpatial10gDialect.class.getCanonicalName())
                || dialect.equals("org.hibernate.dialect.Oracle9Dialect")
                || dialect.equals(OracleSpatial10gDialect.SHORT_NAME)) {
            return new OracleSpatial10gDialect();
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.cadrie.hibernate.spatial.spi.SpatialDialectProvider#getDefaultDialect()
     */
    public final SpatialDialect getDefaultDialect() {
        return new OracleSpatial10gDialect();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.cadrie.hibernate.spatial.spi.SpatialDialectProvider#getSupportedDialects()
     */
    public final String[] getSupportedDialects() {
        return new String[] { OracleSpatial10gDialect.class.getCanonicalName() };
    }
}
