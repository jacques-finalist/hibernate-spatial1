#! /bin/sh
export DBASE=test
export DBUSER=postgres
export POSTGRES_SHARE=/usr/local/pgsql/share

dropdb -U $DBUSER $DBASE

createdb -U $DBUSER $DBASE

echo "Creating language"
createlang -U $DBUSER plpgsql $DBASE

echo "Loading postgis extension"
psql -U $DBUSER -d $DBASE -f $POSTGRES_SHARE/lwpostgis.sql
psql -U $DBUSER -d $DBASE -f $POSTGRES_SHARE/lwpostgis_upgrade.sql

echo "Loading test schema"
psql -U $DBUSER -d $DBASE -f ./create-tables.sql
