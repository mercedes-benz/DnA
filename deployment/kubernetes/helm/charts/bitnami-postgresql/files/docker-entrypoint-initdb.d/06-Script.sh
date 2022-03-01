    echo "********Executing custom scripts********"
    psql -f /docker-entrypoint-initdb.d/01-updatescript.sql
    psql -f /docker-entrypoint-initdb.d/02-schema-creation.sql -U admin -d db;
    psql -f /docker-entrypoint-initdb.d/02-schema-data.sql -U admin -d db;
    psql -f /docker-entrypoint-initdb.d/03-dashboard-schema-creation.sql -U dashboard -d dashboard;
    psql -f /docker-entrypoint-initdb.d/04-dashboard-data-dump.sql -U dashboard -d dashboard;

    #psql -f /docker-entrypoint-initdb.d/05-airflow-db.sql -U airflow -d airflow;