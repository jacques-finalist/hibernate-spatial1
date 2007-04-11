package com.cadrie.hibernate.spatial.oracle.test.model;

import com.vividsolutions.jts.geom.Geometry;

public class TestGeom {

	private long id;

	private String description;

	private Geometry geom;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
