
    CREATE database airflow; 
    CREATE user airflow WITH PASSWORD 'airflow';
    GRANT all privileges ON database airflow TO airflow;
    GRANT USAGE ON SCHEMA public to airflow;
    
    CREATE database dashboard;
    CREATE user dashboard WITH PASSWORD 'dashboard';
    GRANT all privileges ON database dashboard TO dashboard;
    GRANT USAGE ON SCHEMA public to dashboard;

    CREATE user admin WITH PASSWORD 'admin';
    GRANT all privileges ON db admin TO admin;
    GRANT USAGE ON SCHEMA public to admin;