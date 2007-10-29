/*==============================================================*/
/* CREATE LRS TABLES                                            */
/*==============================================================*/


drop table LRS_2D cascade constraints;

drop table LRS_2D_M cascade constraints;

drop table LRS_3D cascade constraints;

drop table LRS_3D_M cascade constraints;

/*==============================================================*/
/* Table: LRS_2D                                                */
/*==============================================================*/
create table LRS_2D  (
   ID                   NUMBER                        not null,
   GEOMETRY             MDSYS.SDO_Geometry,
   DESCRIPTION          VARCHAR(40),
   constraint PK_LRS_2D primary key (ID)
);

/*==============================================================*/
/* Table: LRS_2D_M                                              */
/*==============================================================*/
create table LRS_2D_M  (
   ID                   NUMBER                        not null,
   GEOMETRY             MDSYS.SDO_Geometry,
   DESCRIPTION          VARCHAR(40),
   constraint PK_LRS_2D_M primary key (ID)
);

/*==============================================================*/
/* Table: LRS_3D                                                */
/*==============================================================*/
create table LRS_3D  (
   ID                   NUMBER                        not null,
   GEOMETRY             MDSYS.SDO_Geometry,
   DESCRIPTION          VARCHAR(40),
   constraint PK_LRS_3D primary key (ID)
);

/*==============================================================*/
/* Table: LRS_3D_M                                              */
/*==============================================================*/
create table LRS_3D_M  (
   ID                   NUMBER                        not null,
   GEOMETRY             MDSYS.SDO_Geometry,
   DESCRIPTION          VARCHAR(40),
   constraint PK_LRS_3D_M primary key (ID)
);

