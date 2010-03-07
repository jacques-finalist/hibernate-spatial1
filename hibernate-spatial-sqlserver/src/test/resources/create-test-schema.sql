CREATE TABLE "HBS"."dbo"."geomentity"
(
id int PRIMARY KEY NOT NULL,
type varchar(50),
geom geometry
)
GO
CREATE UNIQUE INDEX geom_test_idx ON geomtest(id)
GO

create table "HBS"."dbo"."pointtest"
(
id int primary key not null,
name varchar(50),
geom geometry
)
GO

create table "HBS"."dbo"."linestringtest"
(
id int primary key not null,
name varchar(50),
geom geometry
)
GO

create table "HBS"."dbo"."multilinestringtest"
(
id int primary key not null,
name varchar(50),
geom geometry
)
GO

create table "HBS"."dbo"."polygontest"
(
id int primary key not null,
name varchar(50),
geom geometry
)
GO

create table "HBS"."dbo"."mlinestringtest"
(
id int primary key not null,
name varchar(50),
geom geometry
)
GO

create table "HBS"."dbo"."multimlinestringtest"
(
id int primary key not null,
name varchar(50),
geom geometry
)
GO







