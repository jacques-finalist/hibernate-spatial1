package org.hibernatespatial.pojo.reader;


/**
 * A <code>FeatureReader</code> provides a means to iterate over <code>Feature</code>s 
 * in a database through Hibernate Spatial.
 * 
 * @author maesenka
 *
 */
public interface FeatureReader {
	
	public boolean hasNext();
		
	public Feature next();

	public void close();

}
