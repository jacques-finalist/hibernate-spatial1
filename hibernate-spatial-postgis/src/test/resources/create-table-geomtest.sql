--
-- creates the test table
--
CREATE TABLE "geomtest"
(
id int PRIMARY KEY NOT NULL,
type varchar(50),
geom geometry
);

create index idx_geomentity_spatial on geomtest using gist(geom);
