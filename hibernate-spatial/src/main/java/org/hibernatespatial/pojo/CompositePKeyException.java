package org.hibernatespatial.pojo;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jul 28, 2010
 */
public class CompositePKeyException extends PKeyException {

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final String basemsg = "Table has composite primary key";

	public CompositePKeyException() {
		super(basemsg);
	}

	public CompositePKeyException(String msg) {
		super(basemsg + ":" + msg);
	}

	public CompositePKeyException(Throwable cause) {
		super(cause);
	}

	public CompositePKeyException(String msg, Throwable cause) {
		super(basemsg + ":" + msg, cause);
	}


}
