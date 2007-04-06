/**
 * $Id: HBSpatialExtension.java 79 2007-02-01 18:03:43Z maesenka $
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static SpatialDialect defaultSpatialDialect = null;

    private static final Pattern nonCommentPattern = Pattern
	    .compile("^([^#]+)");

    static {

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
		    names.addAll(providerNamesFromReader(new BufferedReader(
			    new InputStreamReader(is))));
		} finally {
		    is.close();
		}
	    }

	    for (String s : names) {
		try {
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

    private static Set<String> providerNamesFromReader(BufferedReader reader)
	    throws IOException {
	Set<String> names = new HashSet<String>();
	String line;
	while ((line = reader.readLine()) != null) {
	    line = line.trim();
	    Matcher m = nonCommentPattern.matcher(line);
	    if (m.find()) {
		names.add(m.group().trim());
	    }
	}
	return names;
    }

}
