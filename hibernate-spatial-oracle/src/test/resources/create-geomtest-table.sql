create table geomtest (
  id integer primary key,
  type varchar2(50),
  geom SDO_GEOMETRY
)

insert into user_sdo_geom_metadata values (
  'geomtest',
  'geom',
  SDO_DIM_ARRAY(
    SDO_DIM_ELEMENT('X', -180, 180, 0.00001),
    SDO_DIM_ELEMENT('Y', -90, 90, 0.00001)
--     ,SDO_DIM_ELEMENT('Z', -1000, 10000, 0.001),
--     SDO_DIM_ELEMENT('M', -10000, 100000, 0.001)
    ),
  4326);

create index idx_spatial_geomtest on geomtest (geom) indextype is mdsys.spatial_index;