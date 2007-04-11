package com.cadrie.hibernate.spatial.test.model;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;

/**
 * Test class for testing Points
 */
public class MultiPointEntity {

    // Fields
    private static final long serialVersionUID = 1L;

    private long id;

    private String name;

    private MultiPoint geometry;

    // Constructors

    /** default constructor */
    public MultiPointEntity() {
    }

    /** minimal constructor */
    public MultiPointEntity(long id) {
        this.id = id;
    }

    /** full constructor */
    public MultiPointEntity(long id, String name, Geometry geom) {
        this.id = id;
        this.name = name;
        this.geometry = (MultiPoint) geom;
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
        this.geometry = (MultiPoint) geom;
    }

}
