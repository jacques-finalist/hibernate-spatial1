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

package com.cadrie.hibernate.spatial;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cadrie.hibernate.spatial.helper.PropertyFileReader;
import com.cadrie.hibernate.spatial.spi.SpatialDialectProvider;

/**
 * This is the bootstrap class that is used to get an
 * <code>SpatialDialect</code>.
 * 
 * It also provides a default <code>SpatialDialect</code>.
 * <code>GeometryUserTypes</code>s that do not have a <code>dialect</code>
 * parameter use this default.
 * 
 * The default <code>SpatialDialect</code> will be the first one that is
 * returned by the <code>getDefaultDialect</code> method of the provider at
 * least if it is non null.
 * 
 * @author Karel Maesen
 */
public class HBSpatialExtension {

    protected static Set<SpatialDialectProvider> providers = new HashSet<SpatialDialectProvider>();

    private static final Log log = LogFactory.getLog(HBSpatialExtension.class);

    private static SpatialDialect defaultSpatialDialect = null;

    private static final String DIALECT_PROP_NAME = "hibernate.spatial.dialect";

    static {

        log.info("Initializing HBSpatialExtension");
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = null;
        try {
            resources = loader.getResources("META-INF/services/"
                    + SpatialDialectProvider.class.getName());
            Set<String> names = new HashSet<String>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                InputStream is = url.openStream();
                try {
                    names.addAll(providerNamesFromReader(is));
                } finally {
                    is.close();
                }
            }

            for (String s : names) {
                try {
                    log.info("Attempting to load Hibernate Spatial Provider "
                            + s);
                    SpatialDialectProvider provider = (SpatialDialectProvider) loader
                            .loadClass(s).newInstance();
                    // if no Default SpatialDialect, we ask the provider to
                    // set one.
                    if (defaultSpatialDialect == null)
                        setDefaultSpatialDialect(provider.getDefaultDialect());
                    providers.add(provider);
                } catch (Exception e) {
                    throw new HibernateSpatialException(
                            "Problem loading provider class", e);
                }
            }
        } catch (IOException e) {
            throw new HibernateSpatialException("No "
                    + SpatialDialectProvider.class.getName()
                    + " found in META-INF/services", e);
        }
        // check if there is a system property
        // 
        String dialectProp = System.getProperty(DIALECT_PROP_NAME);
        if (dialectProp != null) {
            log.info("Spatial Dialect configured as system property: "
                    + dialectProp);
            boolean found = false;
            search: for (SpatialDialectProvider provider : providers) {
                for (String dialect : provider.getSupportedDialects()) {
                    if (dialect.equals(dialectProp)) {
                        defaultSpatialDialect = provider.createSpatialDialect(
                                dialectProp, null);
                        found = true;
                        break search;
                    }
                }
            }
            if (!found)
                log
                        .warn("Spatial dialect "
                                + dialectProp
                                + " configured as sytem property, but dialect not found");
        }

        log.info("Hibernate Spatial configured. Using dialect: "
                + defaultSpatialDialect.getClass().getCanonicalName());

    }

    /**
     * Make sure nobody can instantiate this class
     */
    private HBSpatialExtension() {
    }

    /**
     * @param dialect
     */
    public static void setDefaultSpatialDialect(SpatialDialect dialect) {
        defaultSpatialDialect = dialect;
    }

    public static SpatialDialect getDefaultSpatialDialect() {
        return defaultSpatialDialect;
    }

    public static SpatialDialect createSpatialDialect(String dialectName,
            Map properties) {
        SpatialDialect dialect = null;
        for (SpatialDialectProvider provider : providers) {
            dialect = provider.createSpatialDialect(dialectName, properties);
            if (dialect != null) {
                break;
            }
        }
        if (dialect == null) {
            throw new HibernateSpatialException(
                    "No SpatialDialect provider for persistenceUnit "
                            + dialectName);
        }
        return dialect;
    }

    // Helper methods

    private static Set<String> providerNamesFromReader(InputStream is)
            throws IOException {
        PropertyFileReader reader = new PropertyFileReader(is);
        return reader.getNonCommentLines();
    }

}
