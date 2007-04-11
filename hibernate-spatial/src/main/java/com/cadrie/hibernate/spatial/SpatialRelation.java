/**
 * $Id$
 *
 * This file is part of MAJAS (Mapping with Asynchronous JavaScript and ASVG). a
 * framework for Rich Internet GIS Applications.
 *
 * Copyright  © 2007 DFC Software Engineering, Belgium
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
 * @author Karel Maesen, K.U.Leuven R&D Divisie SADL
 * 
 * These spatial relations are all defined in "OpenGIS Simple Feature
 * Specification for SQL, Rev. 1.1" of the Open Geospatial Consortium (OGC).
 * 
 */
public interface SpatialRelation {

    public static int EQUALS = 0;

    public static int DISJOINT = 1;

    public static int TOUCHES = 2;

    public static int CROSSES = 3;

    public static int WITHIN = 4;

    public static int OVERLAPS = 5;

    public static int CONTAINS = 6;

    public static int INTERSECTS = 7;

}