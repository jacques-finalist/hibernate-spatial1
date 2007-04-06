package com.cadrie.hibernate.spatial.test.model;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * Test class for testing Points
 */
public class MultiPolygonEntity {

        // Fields
        private static final long serialVersionUID = 1L;

        private long id;

        private String name;

        private MultiPolygon geometry;

        // Constructors

        /** default constructor */
        public MultiPolygonEntity() {
        }

        /** minimal constructor */
        public MultiPolygonEntity(long id) {
    	this.id = id;
        }

        /** full constructor */
        public MultiPolygonEntity(long id, String name, Geometry geom) {
    	this.id = id;
    	this.name = name;
    	this.geometry = (MultiPolygon) geom;
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
    	this.geometry = (MultiPolygon) geom;
        }



}
