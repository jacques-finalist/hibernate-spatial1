package org.hibernatespatial.pojo;

/**
 * This Exception is thrown when the POJO Utility
 * cannot locate a primary key.
 * 
 * @author Karel Maesen, Geovise BVBA
 *
 */
public class PKeyNotFoundException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String basemsg = "No primary key found in table";
	
	public PKeyNotFoundException(){
		super(basemsg);
	}

	public PKeyNotFoundException(String msg) {
		super(basemsg + ":" + msg);
	}

	public PKeyNotFoundException(Throwable cause) {
		super(cause);
	}

	public PKeyNotFoundException(String msg, Throwable cause) {
		super(basemsg + ":" + msg, cause);
	}

}
