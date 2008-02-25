package org.hibernatespatial.pojo.reader;

import com.vividsolutions.jts.geom.Geometry;

public interface Feature {

	public Object getId();
	
	public Geometry getGeometry();
	
	public Object getAttribute(String name);
	
}
