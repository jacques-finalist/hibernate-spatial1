package org.hibernatespatial.pojo;

/**
 * This Exception is thrown when the POJO Utility
 * cannot locate a primary key.
 * 
 * @author Karel Maesen, Geovise BVBA
 *
 */
public class TableNotFoundException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String basemsg = "Table not found";
	
	public TableNotFoundException(){
		super(basemsg);
	}

	public TableNotFoundException(String msg) {
		super(basemsg + ": " + msg);
	}

	public TableNotFoundException(Throwable cause) {
		super(cause);
	}

	public TableNotFoundException(String msg, Throwable cause) {
		super(basemsg + ": " + msg, cause);
	}

}
