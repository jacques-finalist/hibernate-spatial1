package org.hibernatespatial.pojo;

/**
 * A <code>NamingStrategy</code> determines how to derive
 * suitable class and member names corresponding to 
 * database tables and columns. 
 * 
 * @author Karel Maesen, Geovise BVBA (http://www.geovise.com/)
 */
public interface NamingStrategy {
	
	/**
	 * Create a valid name for a member variable based on the specified input name.
	 * @param base the input name.
	 * @return a valid java identifier for a member variable.
	 */
	public String createPropertyName(String base);
	
	/**
	 * Create a valid name for a setter for the property
	 * @param propertyName
	 * @return valid java identifier for a property setter
	 */
	public String createSetterName(String propertyName);
	
	/**
	 * Create a valid name for a getter for the property
	 * @param propertyName
	 * @return valid java identifier for a property getter
	 */
	public String createGetterName(String propertyName);
	
	/**
	 * Create a valid name for a Java class based on the specified input name.
	 * @param base the input name.
	 * @return a valid java identifier for a class.
	 */	
	public String createClassName(String base);
	

}
