/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
 * Copyright © 2007 Geovise BVBA
 * Copyright © 2007 K.U. Leuven LRD, Spatial Applications Division, Belgium
 *
 * This work was partially supported by the European Commission, 
 * under the 6th Framework Programme, contract IST-2-004688-STP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, visit: http://www.hibernatespatial.org/
 */
package org.hibernatespatial.test;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.mgeom.MCoordinate;
import org.hibernatespatial.mgeom.MGeometryFactory;
import org.hibernatespatial.mgeom.MLineString;
import org.hibernatespatial.mgeom.MultiMLineString;
import org.hibernatespatial.test.model.LineStringEntity;
import org.hibernatespatial.test.model.MLineStringEntity;
import org.hibernatespatial.test.model.MultiLineStringEntity;
import org.hibernatespatial.test.model.MultiMLineStringEntity;
import org.hibernatespatial.test.model.PointEntity;
import org.hibernatespatial.test.model.PolygonEntity;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * @author maesenka
 * 
 */
public class TestCRUD extends TestCase {

	private SessionFactory factory;

	private final static int COORDARRAY_LENGTH = 100;

	private MGeometryFactory geomFactory = new MGeometryFactory(
			new PrecisionModel(PrecisionModel.FLOATING), 31370);

	public void setUpBeforeClass() throws Exception {

		try{
			// set up hibernate and register Spatialtest as a persistent entity
		System.out.println("Setting up Hibernate");
		Configuration config = new Configuration();
		config.configure();
		config.addClass(LineStringEntity.class);
		config.addClass(PolygonEntity.class);
		config.addClass(MultiLineStringEntity.class);
		config.addClass(PointEntity.class);
		config.addClass(MLineStringEntity.class);
		config.addClass(MultiMLineStringEntity.class);

		//configure Hibernate Spatial based on this config
		HSConfiguration hsc = new HSConfiguration();
		hsc.configure(config);
		HBSpatialExtension.setConfiguration(hsc);
		
		// build the session factory
		// Settings settings = config.buildSettings();
		// SpatialExtension.setDefaultSpatialDialect((SpatialDialect)settings.getDialect());
		factory = config.buildSessionFactory();
		
		
		} catch (Exception e){
			System.err.println("Failed to configure Hibernate." + e.getMessage());
			throw e;
		}
		System.out.println("Hibernate set-up complete.");
	}

	public void tearDownAfterClass() {
		factory.close();
		factory = null;
	}

	private long saveObject(Object obj) throws Exception {
		Session session = factory.openSession();

		Transaction tx = null;
		long id = -1;
		try {
			tx = session.beginTransaction();
			session.save(obj);

			if (obj instanceof LineStringEntity){
				id = ((LineStringEntity) obj).getId();
			} else if (obj instanceof PointEntity){
				id = ((PointEntity)obj).getId();
			} else if (obj instanceof MLineStringEntity){
				id = ((MLineStringEntity)obj).getId();
			} else if (obj instanceof MultiMLineStringEntity){
				id = ((MultiMLineStringEntity)obj).getId();
			} else {
				throw new RuntimeException("can't save object of this type");				
			}
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} finally {
			session.close();
		}
		return id;
	}

	private Object retrieveObject(Class clazz, long id) {
		Session session = factory.openSession();
		Object obj = session.get(clazz, id);
		session.close();
		return obj;
	}

	public void testSaveLineStringEntity() throws Exception {
		testSaveLineStringEntity(2);
	}
	
	public void testSaveLineStringEntity(int dim) throws Exception{
		
		LineStringEntity line = new LineStringEntity();
		Coordinate[] coordinates = new Coordinate[COORDARRAY_LENGTH];

		double startx = 4319.0;
		double starty = 53255.0;
		double startz = 125.0;

		for (int i = 0; i < COORDARRAY_LENGTH; i++) {
			if (dim == 2){
				coordinates[i] = new Coordinate(startx + i, starty + i);
			} else if(dim == 3) {
				coordinates[i] = new Coordinate(startx + i, starty + i, startz + i);
			} else {
				throw new RuntimeException("Dimension not supported.");
			}
		}

		Geometry geom = geomFactory.createLineString(coordinates);
		line.setGeometry(geom);
		line.setName("Added by TestCRUD");
		long id = saveObject(line);
		LineStringEntity retrieved = (LineStringEntity) retrieveObject(
				LineStringEntity.class, id);
		// check if we retrieve all the same stuff
		assertTrue(line.getGeometry().equals(retrieved.getGeometry()));
		// assertEquals(line.getGeometry(),
		// retrieved.getGeometry()); DOES NOT WORK:
		// AssertEquals doesn' t work because in JTS 1.7 Geometry has a
		// method with signature boolean equals(Geometry) which does NOT
		// override
		// equals(Object). This last method is called from assertEquals.

		assertEquals(line.getId(), retrieved.getId());
		assertEquals(line.getName(), retrieved.getName());
	}
	

	private MLineString createMLineString(double startx, double starty, double startz, double startm){
		
		MCoordinate[] coordinates = new MCoordinate[COORDARRAY_LENGTH];


		for (int i = 0; i < COORDARRAY_LENGTH; i++) {
				coordinates[i] = new MCoordinate(startx + i, starty + i, startz + i, startm +i);
		}

		MLineString geom = geomFactory.createMLineString(coordinates);
		return geom;
	}
	
	
	
	private void assertEquality(Geometry expected, Geometry retrieved){
		assertTrue(expected.equals(retrieved));
		Coordinate[] rCoords = retrieved.getCoordinates();
		Coordinate[] eCoords = expected.getCoordinates();
		for (int idx = 0; idx < rCoords.length; idx++){
			MCoordinate rco = (MCoordinate)rCoords[idx];
			MCoordinate eco = (MCoordinate)eCoords[idx];
			assertEquals("z value not equal", eco.z, rco.z, 0.000001);			
			assertEquals("m value not equal", eco.z, rco.z, 0.000001);			
		}
	}
	
	
	public void testSaveMLineStringEntity() throws Exception{
		MLineStringEntity mline = new MLineStringEntity();

		double startx = 4319.0;
		double starty = 53255.0;
		double startz = 125.0;
		double startm = 0.0;
		MLineString geom = createMLineString(startx, starty, startz, startm);
		mline.setGeometry(geom);
		mline.setName("Added by TestCRUD");
		long id = saveObject(mline);
		MLineStringEntity retrieved = (MLineStringEntity) retrieveObject(
				MLineStringEntity.class, id);
		// check if we retrieve all the same stuff
		assertEquality(geom, retrieved.getGeometry());
		assertEquals(mline.getId(), retrieved.getId());
		assertEquals(mline.getName(), retrieved.getName());
	}
	
	public void testSaveMultiMLineStringEntity() throws Exception {
		MultiMLineStringEntity entity = new MultiMLineStringEntity();
		double startx = 4000.0;
		double starty = 4000.0;
		double startz = 10.0;
		double startm = 0.0;
		MLineString[] mlines = new MLineString[5];
		for (int i = 0; i < 5; i++){
			startx += i * COORDARRAY_LENGTH + 10.0;
			starty += i * COORDARRAY_LENGTH + 10.0;
			startz += i * COORDARRAY_LENGTH + 10.0;
			startm += i * COORDARRAY_LENGTH;
			mlines[i] = createMLineString(startx, starty, startz, startm);			
		}
		MultiMLineString multiMLine = geomFactory.createMultiMLineString(mlines);
		entity.setGeometry(multiMLine);
		entity.setName("Added by TestCRUD");
		long id = saveObject(entity);
		MultiMLineStringEntity retrievedEntity = (MultiMLineStringEntity)retrieveObject( MultiMLineStringEntity.class, id);
		MultiMLineString retrievedGeom = (MultiMLineString)retrievedEntity.getGeometry();
		for (int i = 0; i < retrievedGeom.getNumGeometries(); i++){
			assertEquality(multiMLine.getGeometryN(i), retrievedGeom.getGeometryN(i));
		}
		assertEquals(entity.getId(), retrievedEntity.getId());
		assertEquals(entity.getName(), retrievedEntity.getName());
	}
	

	public void testSaveNullLineStringEntity() throws Exception {
		LineStringEntity line = new LineStringEntity();

		line.setGeometry(null);
		line.setName("Null geom Added by TestCRUD");
		long id = saveObject(line);

		// System.out.println("id: " + id);

		LineStringEntity retrieved = (LineStringEntity) retrieveObject(
				LineStringEntity.class, id);
		// check if we retrieve a null geometry
		assertNull(retrieved.getGeometry());

	}
	
	
	public void testSavePoint(int dim) throws Exception{
		
		double x = 4319.0;
		double y = 53255.0;
		double z = 125.0;
		Coordinate c = null;
		switch (dim){
		case 2:
			c = new Coordinate(x,y);
			break;
		case 3:
			c = new Coordinate(x,y,z);
			break;
		default:
			throw new RuntimeException("Dimension not supported.");
		}
			
		PointEntity pt = new PointEntity();
		Geometry geom = geomFactory.createPoint(c);
		geom.setSRID(31370);		
		pt.setGeometry(geom);
		pt.setName("Added by TestCRUD");
		long id = saveObject(pt);
		PointEntity retrieved = (PointEntity) retrieveObject(
				PointEntity.class, id);
		// check if we retrieve all the same stuff
		assertTrue(pt.getGeometry().equals(retrieved.getGeometry()));
		// assertEquals(line.getGeometry(),
		// retrieved.getGeometry()); DOES NOT WORK:
		// AssertEquals doesn' t work because in JTS 1.7 Geometry has a
		// method with signature boolean equals(Geometry) which does NOT
		// override
		// equals(Object). This last method is called from assertEquals.

		assertEquals(pt.getId(), retrieved.getId());
		assertEquals(pt.getName(), retrieved.getName());
	}
}
