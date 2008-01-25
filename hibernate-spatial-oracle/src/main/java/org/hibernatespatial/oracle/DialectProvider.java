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
package org.hibernatespatial.oracle;

import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.spi.SpatialDialectProvider;

/**
 * Oracle10g DialectProvider.
 * 
 * @author Karel Maesen
 */
public class DialectProvider implements SpatialDialectProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernatespatial.spi.SpatialDialectProvider#createSpatialDialect(java.lang.String,
	 *      java.util.Map)
	 */
	public final SpatialDialect createSpatialDialect(final String dialect) {

		if (dialect.equals(OracleSpatial10gDialect.class.getCanonicalName())
				|| dialect.equals("org.hibernate.dialect.Oracle10gDialect")
				|| dialect.equals(OracleSpatial10gDialect.SHORT_NAME)) {
			return new OracleSpatial10gDialect();
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernatespatial.spi.SpatialDialectProvider#getDefaultDialect()
	 */
	public final SpatialDialect getDefaultDialect() {
		return new OracleSpatial10gDialect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernatespatial.spi.SpatialDialectProvider#getSupportedDialects()
	 */
	public final String[] getSupportedDialects() {
		return new String[] { OracleSpatial10gDialect.class.getCanonicalName() };
	}
}
