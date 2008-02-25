package org.hibernatespatial.pojo;

/**
 * This Exception is thrown when the POJO Utility
 * cannot locate a Geometry-valued attribute.
 * 
 * @author Karel Maesen, Geovise BVBA
 *
 */
public class GeometryNotFoundException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String basemsg = "No Geometry found.";
	
	public GeometryNotFoundException(){
		super(basemsg);
	}

	public GeometryNotFoundException(String msg) {
		super(basemsg + ":" + msg);
	}

	public GeometryNotFoundException(Throwable cause) {
		super(cause);
	}

	public GeometryNotFoundException(String msg, Throwable cause) {
		super(basemsg + ":" + msg, cause);
	}

}
