    
    CREATE user airflow;
    CREATE database airflow;
    ALTER user airflow WITH PASSWORD 'airflow';
    GRANT all privileges ON database airflow TO airflow;
    GRANT USAGE ON SCHEMA public to airflow;
    
    CREATE user dashboard;
    CREATE database dashboard;
    GRANT all privileges ON dashboard TO dashboard;
    GRANT USAGE ON SCHEMA public to dashboard;
    ALTER user dashboard WITH PASSWORD 'dashboard';