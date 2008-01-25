/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
 * Copyright © 2007 Geovise BVBA
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
package org.hibernatespatial.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.cfg.Configuration;

/**
 * Configuration information for the Hibernate Spatial Extension.
 * 
 * @author Karel Maesen
 * 
 * 
 */
public class HSConfiguration extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(HSConfiguration.class);

	private String source;

	private HSProperty[] HSProperties;

	public HSConfiguration() {
		HSProperties = HSProperty.values();
	}

	public String getDefaultDialect() {
		return getProperty(HSProperty.DEFAULT_DIALECT.toString());
	}

	public void setDefaultDialect(String dialect) {
		setProperty(HSProperty.DEFAULT_DIALECT, dialect);
	}

	public String getPrecisionModel() {
		return getProperty(HSProperty.PRECISION_MODEL.toString());
	}

	public void setPrecisionModel(String precisionModel) {
		setProperty(HSProperty.PRECISION_MODEL, precisionModel);
	}

	public String getProperty(HSProperty property) {
		return getProperty(property.toString());
	}

	public void setProperty(HSProperty property, String value) {
		setProperty(property.toString(), value);
	}

	public HSConfiguration configure(Configuration hibernateConfig) {
		String dialect = hibernateConfig.getProperty("hibernate.dialect");
		setProperty(HSProperty.DEFAULT_DIALECT, dialect);
		return this;
	}

	public HSConfiguration configure() {
		return configure("hibernate-spatial.cfg.xml");
	}

	public HSConfiguration configure(File resource) {
		this.source = resource.getName();
		logger.info("configuring from file: " + resource.getName());
		try {
			return doConfigure(new FileInputStream(resource));
		} catch (FileNotFoundException e) {
			logger.warn("could not find file: " + resource + ".\nCause:"
					+ e.getMessage());
		} catch (DocumentException e) {
			logger.warn("Failed to load configuration file :" + resource
					+ ".\nCause:" + e.getMessage());
		}
		return null;
	}

	/**
	 * The source file or URL for this configuration.
	 * 
	 * @return The source name (file or URL).
	 */
	public String getSource() {
		return this.source;
	}

	public HSConfiguration configure(String resource) {
		logger.info("configuring from resource: " + resource);
		this.source = resource;
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		InputStream stream = null;
		try {
			stream = classLoader.getResourceAsStream(resource);
			return doConfigure(stream);
		} catch (Exception e) {
			logger.warn("Failed to load configuration file :" + resource);
		}
		return null;
	}

	private HSConfiguration doConfigure(InputStream stream)
			throws DocumentException {
		try {
			SAXReader reader = new SAXReader();
			Document configDoc = reader.read(stream);
			Element root = configDoc.getRootElement();
			for (HSProperty hsprop : HSProperties) {
				Element propEl = root.element(hsprop.toString().toLowerCase());
				if (propEl != null) {
					setProperty(hsprop, propEl.getText());
				}
			}
		} finally {
			try {
				stream.close();
			} catch (Exception e) {
			} // Can't do anything about this.
		}
		return this;
	}

}
