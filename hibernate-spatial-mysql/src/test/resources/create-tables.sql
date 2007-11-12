-- Change database
USE testhbs

--
-- Create test tables
--

CREATE TABLE linestringtest (
	id DECIMAL(10,0),
	name VARCHAR(50),
	geom geometry
);

CREATE TABLE multilinestringtest(
	id DECIMAL(10,0),
	name VARCHAR(50),
	geom geometry
);

CREATE TABLE polygontest(
	id DECIMAL(10,0),
	name VARCHAR(50),
	geom geometry
);

-- Currently, MySQL requires not-null geometries for spatial indexing.
-- Therefore we disable the spatial index for now.
-- CREATE SPATIAL INDEX ls_sp_idx ON linestringtest (geom);
-- CREATE SPATIAL INDEX mls_sp_idx ON multilinestringtest(geom);
-- CREATE SPATIAL INDEX pl_sp_idx ON polygontest(geom);

 
