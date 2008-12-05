/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
 * Copyright © 2008 Geovise BVBA
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
package org.hibernatespatial.readers;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernatespatial.criterion.SpatialFilter;
import org.hibernatespatial.criterion.SpatialRestrictions;
import org.hibernatespatial.helper.FinderException;
import org.hibernatespatial.helper.GeometryPropertyFinder;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A {@link FeatureReader} that uses the {@link FeatureAdapter} 
 * to dynamically adapt retrieved objects to the Feature interface.
 * 
 * @author Karel Maesen
 *
 */
public class BasicFeatureReader implements FeatureReader {
	
	private Session session = null;
	private ScrollableResults results = null; 
	private final ClassMetadata metadata;
	
	public BasicFeatureReader(Class entityClass, SessionFactory sf, Geometry filterGeom, String attributeFilter) throws FinderException {
		this.session = sf.openSession();		
		this.metadata = sf.getClassMetadata(entityClass);
		String geomProp;
		GeometryPropertyFinder gp = new GeometryPropertyFinder();
		geomProp = gp.find(this.metadata);
		Criteria crit = this.session.createCriteria(entityClass);
		if (filterGeom != null) {
			SpatialFilter filter = SpatialRestrictions.filter(geomProp,
					filterGeom);
			crit.add(filter);
		}
		if (attributeFilter != null) {
			crit.add(Restrictions.sqlRestriction(attributeFilter));
		}
		this.results = crit.scroll(ScrollMode.FORWARD_ONLY);
	}


	public void close() {
		this.results.close();
		this.results = null;
		this.session.close();

	}

	public boolean hasNext() {
		return this.results.next();
	}

	public Feature next()  {
		Object[] currentRow = this.results.get();
		if (currentRow == null) {
			this.close();
			throw new RuntimeException("Reading beyond the Scrollable Results.");
		}
		Object f =  currentRow[0];
		return FeatureAdapter.adapt(f, this.metadata);
	}


}
