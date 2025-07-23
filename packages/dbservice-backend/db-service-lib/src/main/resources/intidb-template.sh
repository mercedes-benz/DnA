##!/bin/bash
#local connections:
sed -i -- '92 i local               all               all            trust' $PGDATA/pg_hba.conf
sed -i -- '93 i local               postgres               postgres            trust' $PGDATA/pg_hba.conf

sed -i -- '$ a host                 all               all            0.0.0.0/0  md5' $PGDATA/pg_hba.conf
sed -i -- '$ a host                 all               all            ::0/0 md5' $PGDATA/pg_hba.conf

psql -c "ALTER USER <superusername> WITH SUPERUSER" -U postgres;
psql -c "ALTER USER <superusername> SET search_path = public" -U postgres;
psql -c "GRANT all privileges on database <dbname> to <superusername>" -U postgres;
psql -c "GRANT dai_db_connect to <superusername>" -U postgres;
psql -c "GRANT dai_public_compat to <superusername>" -U postgres;

#creation of ready only user
psql -c "CREATE USER <readonlyusername> WITH PASSWORD '<readonlypassword>'" -U postgres;
psql -c "GRANT CONNECT ON DATABASE <dbname> TO <readonlyusername>" -U postgres;
psql -c "GRANT USAGE ON SCHEMA public TO <readonlyusername>" -U postgres;
psql -c "GRANT SELECT ON ALL TABLES IN SCHEMA public TO <readonlyusername>" -U postgres;
psql -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO <readonlyusername>" -U postgres;
 
psql -c "GRANT dai_db_connect to <readonlyusername>" -U postgres;
psql -c "GRANT dai_public_compat to <readonlyusername>" -U postgres;

#creation of ready write user
psql -c "CREATE USER <readwrtiteusername> WITH PASSWORD '<readwrtitepassword>'" -U postgres;
psql -c "ALTER USER <readwrtiteusername> SET search_path = public" -U postgres;
psql -c "GRANT all privileges on database <dbname> to <readwrtiteusername>" -U postgres;
psql -c "GRANT dai_db_connect to <readwrtiteusername>" -U postgres;
psql -c "GRANT dai_public_compat to <readwrtiteusername>" -U postgres;

psql <<INITSQL

INITSQL
