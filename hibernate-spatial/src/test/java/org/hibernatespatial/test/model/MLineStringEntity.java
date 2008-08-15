package org.hibernatespatial.test.model;
import org.hibernatespatial.mgeom.MLineString;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * Test calss
 */
public class MLineStringEntity implements java.io.Serializable {

	// Fields

	private static final long serialVersionUID = 1L;

	private long id;

	private String name;

	private MLineString geometry;

	// Constructors

	/** default constructor */
	public MLineStringEntity() {
	}

	/** minimal constructor */
	public MLineStringEntity(long id) {
		this.id = id;
	}

	/** full constructor */
	public MLineStringEntity(long id, String name, Geometry geom) {
		this.id = id;
		this.name = name;
		this.geometry = (MLineString) geom;
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

	public void setGeometry(MLineString geom) {
		this.geometry = geom;
	}

}