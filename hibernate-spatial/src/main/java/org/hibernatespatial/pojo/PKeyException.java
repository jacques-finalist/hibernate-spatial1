package org.hibernatespatial.pojo;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jul 28, 2010
 */
public class PKeyException extends Exception {

    public PKeyException(String msg) {
        super(msg);
    }

    public PKeyException(Throwable cause) {
        super(cause);
    }

    public PKeyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
