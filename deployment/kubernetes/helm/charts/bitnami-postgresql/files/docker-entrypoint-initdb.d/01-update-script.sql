
    CREATE database airflow; 
    CREATE user airflow WITH PASSWORD 'airflow';
    GRANT all privileges ON database airflow TO airflow;
    GRANT USAGE ON SCHEMA public to airflow;
    
    CREATE database dashboard;
    CREATE user dashboard WITH PASSWORD 'dashboard';
    GRANT all privileges ON database dashboard TO dashboard;
    GRANT USAGE ON SCHEMA public to dashboard;