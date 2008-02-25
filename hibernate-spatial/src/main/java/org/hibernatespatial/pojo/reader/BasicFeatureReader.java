package org.hibernatespatial.pojo.reader;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernatespatial.criterion.SpatialFilter;
import org.hibernatespatial.criterion.SpatialRestrictions;
import org.hibernatespatial.helper.FinderException;
import org.hibernatespatial.helper.HSClassMetadata;

import com.vividsolutions.jts.geom.Geometry;

public class BasicFeatureReader implements FeatureReader {
	
	private Session session = null;
	private ScrollableResults results = null; 
	private final HSClassMetadata metadata;
	
	public BasicFeatureReader(Class entityClass, SessionFactory sf, Geometry filterGeom, String attributeFilter) throws FinderException {
		this.session = sf.openSession();		
		this.metadata = new HSClassMetadata(sf.getClassMetadata(entityClass));
		String geomProp;
		geomProp = this.metadata.getGeometryPropertyName();
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
