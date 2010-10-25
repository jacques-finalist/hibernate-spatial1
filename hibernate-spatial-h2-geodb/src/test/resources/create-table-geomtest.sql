CREATE DOMAIN geometry AS blob;

--
-- creates the test table
--
-- CREATE TABLE GEOMTEST
-- (
-- id int PRIMARY KEY NOT NULL,
-- type varchar(50),
-- geom geometry
-- ) ;

-- create spatial index idx_geomentity_spatial on GEOMTEST (geom);
CALL CreateSpatialIndex(null, 'GEOMTEST', 'GEOM', '4326');
