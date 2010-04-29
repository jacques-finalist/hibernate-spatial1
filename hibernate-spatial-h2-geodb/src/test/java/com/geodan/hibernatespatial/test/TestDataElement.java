package com.geodan.hibernatespatial.test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.junit.Ignore;

import com.vividsolutions.jts.geom.Geometry;

@Entity
@Table(name = "GEOMTEST")
@Ignore
public class TestDataElement {
	
	@Id
	@Column(name = "ID")
	private int id;
	
	@Column(name = "TYPE")
	private String type;
	
	@Column(name = "GEOM")
	@org.hibernate.annotations.Type(type = "org.hibernatespatial.GeometryUserType")
	private Geometry geom;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

	@Override
	public String toString() {
		return "TestDataElement [id=" + id + ", type=" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((geom == null) ? 0 : geom.hashCode());
		result = prime * result + id;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestDataElement other = (TestDataElement) obj;
		if (geom == null) {
			if (other.geom != null)
				return false;
		} else if (!geom.equals(other.geom))
			return false;
		if (id != other.id)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
