package com.cadrie.hibernate.spatial.test.model;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * Test class for testing LineStrings
 */
public class LineStringEntity implements java.io.Serializable {

    // Fields

    private static final long serialVersionUID = 1L;

    private long id;

    private String name;

    private LineString geometry;

    // Constructors

    /** default constructor */
    public LineStringEntity() {
    }

    /** minimal constructor */
    public LineStringEntity(long id) {
        this.id = id;
    }

    /** full constructor */
    public LineStringEntity(long id, String name, Geometry geom) {
        this.id = id;
        this.name = name;
        this.geometry = (LineString) geom;
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
        this.geometry = (LineString) geom;
    }

}
