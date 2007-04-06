package com.cadrie.hibernate.spatial.test.model;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * Test class for testing Points
 */
public class PointEntity {

        // Fields
        private static final long serialVersionUID = 1L;

        private long id;

        private String name;

        private Point geometry;

        // Constructors

        /** default constructor */
        public PointEntity() {
        }

        /** minimal constructor */
        public PointEntity(long id) {
    	this.id = id;
        }

        /** full constructor */
        public PointEntity(long id, String name, Geometry geom) {
    	this.id = id;
    	this.name = name;
    	this.geometry = (Point) geom;
        }

        // Property accessors
        public long getId() {
    	return this.id;
        }

        public void setId(long id) {
    	this.id = id;
        }

        public String getName() {
    	return this.name;
        }

        public void setName(String name) {
    	this.name = name;
        }

        public Geometry getGeometry() {
    	return this.geometry;
        }

        public void setGeometry(Geometry geom) {
    	this.geometry = (Point) geom;
        }



}
