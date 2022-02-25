    CREATE user admin password 'admin';
    CREATE SCHEMA dai_admin;
    ALTER SCHEMA dai_admin OWNER TO admin;
    GRANT dai_admin to admin;
    alter role admin with password 'admin';
  --  \connect db admin;
    \i /docker-entrypoint-initdb.d/schema-creation.sql -U admin -d db;
    \i /docker-entrypoint-initdb.d/schema-data.sql -U admin -d db;

    CREATE user airflow;
    CREATE database airflow;
    ALTER user airflow WITH PASSWORD 'airflow';
    GRANT all privileges ON database airflow TO airflow;
    GRANT USAGE ON SCHEMA public to airflow;  

    CREATE user dashboard;
    CREATE database dashboard;
    ALTER user dashboard WITH PASSWORD 'dashboard';
    GRANT all privileges ON database dashboard TO dashboard;
    GRANT USAGE ON SCHEMA public to dashboard;
    \c dashboard dashboard; 
    \i /docker-entrypoint-initdb.d/dashboard-schema-creation.sql -U dashboard -d dashboard;
    \i /docker-entrypoint-initdb.d/dashboard-data-dump.sql -U dashboard -d dashboard;
   
    pg_ctl stop
    sleep 5
    pg_ctl start
    sleep 5