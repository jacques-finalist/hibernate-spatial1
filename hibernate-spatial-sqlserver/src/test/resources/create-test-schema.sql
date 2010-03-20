CREATE TABLE "HBS"."dbo"."geomtest"
(
id int PRIMARY KEY NOT NULL,
type varchar(50),
geom geometry
)
GO
CREATE UNIQUE INDEX geom_test_idx ON geomtest(id)
GO








