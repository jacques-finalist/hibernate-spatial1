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

package com.cadrie.hibernate.spatial.spi;

import java.util.Map;

import com.cadrie.hibernate.spatial.SpatialDialect;

/**
 * Interface that is implemented by a SpatialDialect Provider.
 * 
 * A <class>SpatialDialectProvider</class> creates a SpatialDialect for one or
 * more database systems. These databases are identified by a dialect string.
 * Usually this is the fully qualified class name of a
 * <code>org.hibernate.dialect.Dialect</code> or <code>SpatialDialect</code>
 * implementation
 * 
 */

public interface SpatialDialectProvider {

    /**
     * create Spatial Dialect with the provided name.
     * 
     * @param dialect
     *            Name of the dialect to create.
     * @param map
     *            A map of properties for use by the provider when creating the
     *            dialect.
     * @return a SpatialDialect
     */
    public SpatialDialect createSpatialDialect(String dialect, Map map);

    /**
     * Returns the default dialect for this provider.
     * 
     * @return The Default Dialect provided by the implementation.
     * 
     * Implementations should never return null for this method.
     */
    public SpatialDialect getDefaultDialect();

    /**
     * Returns the Dialect names
     * 
     * This method must return the canonical class names of the Spatialdialect
     * implementations that this provider provides.
     * 
     * @return array of dialect names.
     */
    public String[] getSupportedDialects();

}
