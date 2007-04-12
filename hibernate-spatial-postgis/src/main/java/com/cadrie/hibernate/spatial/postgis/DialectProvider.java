/**
 * $Id$
 *
 * This file is part of Spatial Hibernate, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
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
 * For more information, visit: http://www.cadrie.com/
 */
 
package com.cadrie.hibernate.spatial.postgis;

import java.util.Map;

import com.cadrie.hibernate.spatial.SpatialDialect;
import com.cadrie.hibernate.spatial.spi.SpatialDialectProvider;

/**
 * PostGIS DialectProvider
 * 
 * @author Karel Maesen
 */
public class DialectProvider implements SpatialDialectProvider {

    /*
     * (non-Javadoc)
     * 
     * @see com.cadrie.hibernate.spatial.spi.SpatialDialectProvider#createSpatialDialect(java.lang.String,
     *      java.util.Map)
     */
    public SpatialDialect createSpatialDialect(String dialect, Map map) {
        if (dialect.equals(PostgisDialect.class.getCanonicalName())
                || dialect.equals("org.hibernate.dialect.PostgreSQLDialect")
                || dialect.equals("postgis"))
            return new PostgisDialect();
        else
            return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.cadrie.hibernate.spatial.spi.SpatialDialectProvider#getDefaultDialect()
     */
    public SpatialDialect getDefaultDialect() {
        return new PostgisDialect();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.cadrie.hibernate.spatial.spi.SpatialDialectProvider#getSupportedDialects()
     */
    public String[] getSupportedDialects() {
        return new String[] { PostgisDialect.class.getCanonicalName() };
    }

}
