/**
 * $Id: SpatialDialectProvider.java 81 2007-02-01 18:04:54Z maesenka $
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

package com.cadrie.hibernate.spatial.spi;

import java.util.Map;

import com.cadrie.hibernate.spatial.SpatialDialect;

/**
 * Interface that is implemented by a SpatialDialect Provider.
 * 
 * A <class>SpatialDialectProvider</class> creates a SpatialDialect for one
 * or more database systems. These databases are identified by a dialect string.
 * Usually this is the fully qualified class name of an
 * <code>org.hibernate.dialect.Dialect</code> or <code>SpatialDialect</code>
 * implementation
 * 
 * @author Karel Maesen
 * 
 */

public interface SpatialDialectProvider {

    /**
         * @param dialect
         *                Name of the persistence unit
         * @param map
         *                A map of properties for use by the provider
         * @return the SpatialDialect provided by the provider implementation
         */
    public SpatialDialect createSpatialDialect(String dialect, Map map);

    /**
         * @return The Default Dialect provided by the implementation.
         * 
         * Implementations should never return null for this method.
         */
    public SpatialDialect getDefaultDialect();

}
