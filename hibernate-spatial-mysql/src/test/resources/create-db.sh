#! /bin/sh

#  A very simple database creation script.

# the host computer
export DBHOST=test.geovise.com
# the account information used by Hibernate 
export DBASE=testhbs
export DBUSER=hibernate

# ==To be removed==
export TMP=/tmp/mysql-hibernate-test-create.sql
export CREATE_TABLE_SCRIPT=./create-tables.sql

# The account that has the privileges to create the database
export DBADMIN=admin
export DBADMINPWD=nimad

echo "CREATE DATABASE $DBASE;" > $TMP
echo "GRANT ALL PRIVILEGES ON $DBASE.* TO '$DBUSER'@'%' IDENTIFIED BY '$DBUSER';" >> $TMP
echo "\q" >> $TMP

mysql --user=$DBADMIN --password=$DBADMINPWD --host=$DBHOST mysql < $TMP
mysql --user=$DBADMIN --password=$DBADMINPWD --host=$DBHOST $DBASE < $CREATE_TABLE_SCRIPT
rm $TMP

