#! /bin/sh
export DBASE=test
export DBUSER=postgres
export POSTGRES_SHARE=/usr/share/postgresql/8.4/contrib

dropdb -U $DBUSER $DBASE

createdb -U $DBUSER $DBASE

echo "Creating language"
createlang -U $DBUSER plpgsql $DBASE

echo "Loading postgis extension"
psql -U $DBUSER -d $DBASE -f $POSTGRES_SHARE/postgis.sql
psql -U $DBUSER -d $DBASE -f $POSTGRES_SHARE/spatial_ref_sys.sql

echo "Loading test schema"
psql -U $DBUSER -d $DBASE -f ./create-table-geomtest.sql
