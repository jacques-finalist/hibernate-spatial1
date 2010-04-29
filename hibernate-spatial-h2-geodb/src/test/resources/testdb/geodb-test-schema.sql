CREATE TABLE GEOMTEST
(
	ID integer identity primary key,
	TYPE varchar(50),
	GEOM blob
);

CALL CreateSpatialIndex(null, 'GEOMTEST', 'GEOM', '4326');