/**
 * $Id: HibernateSpatialException.java 79 2007-02-01 18:03:43Z maesenka $
 *
 * This file is part of MAJAS (Mapping with Asynchronous JavaScript and ASVG). a
 * framework for Rich Internet GIS Applications.
 *
 * Copyright © 2007 DFC Software Engineering, Belgium
 * and K.U. Leuven LRD, Spatial Applications Division, Belgium
 *
 * MAJAS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MAJAS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with gGIS; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.cadrie.hibernate.spatial;

/**
 * 
 */
public class HibernateSpatialException extends RuntimeException {

    /**
         * generated serialVersionUID
         */
    private static final long serialVersionUID = -2153256823661407568L;

    public HibernateSpatialException(String msg) {
	super(msg);
    }

    public HibernateSpatialException(Throwable cause) {
	super(cause);
    }

    public HibernateSpatialException(String msg, Throwable cause) {
	super(msg, cause);
    }

}
