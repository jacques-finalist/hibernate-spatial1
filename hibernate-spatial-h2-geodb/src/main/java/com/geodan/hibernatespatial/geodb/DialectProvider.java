/**
 * Copyright 2010 Geodan IT b.v.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.geodan.hibernatespatial.geodb;

import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.spi.SpatialDialectProvider;

/**
 * GeoDB (H2 database with HatBox spatial extension) Dialect Provider.
 * 
 * @author janb
 */
public class DialectProvider implements SpatialDialectProvider {

	/* (non-Javadoc)
	 * @see org.hibernatespatial.spi.SpatialDialectProvider#createSpatialDialect(java.lang.String)
	 */
	public SpatialDialect createSpatialDialect(String dialect) {
		if (dialect.equals(GeoDBDialect.class.getCanonicalName())
				|| dialect.equals("org.hibernate.dialect.H2SQLDialect")
				|| dialect.equals("H2"))
			return new GeoDBDialect();
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.hibernatespatial.spi.SpatialDialectProvider#getDefaultDialect()
	 */
	public SpatialDialect getDefaultDialect() {
		return new GeoDBDialect();
	}

	/* (non-Javadoc)
	 * @see org.hibernatespatial.spi.SpatialDialectProvider#getSupportedDialects()
	 */
	public String[] getSupportedDialects() {
		return new String[] { GeoDBDialect.class.getCanonicalName() };
	}

}
