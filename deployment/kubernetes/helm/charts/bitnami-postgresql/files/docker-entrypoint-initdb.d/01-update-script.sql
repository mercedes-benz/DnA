
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

    CREATE user hive;
    CREATE database hive;
    ALTER user hive WITH PASSWORD 'hive123';
    GRANT all privileges ON database hive TO hive;
    GRANT USAGE ON SCHEMA public to hive;

    CREATE user storage;
    CREATE database storage;
    ALTER user storage WITH PASSWORD 'storage123';
    GRANT all privileges ON database storage TO storage;
    GRANT USAGE ON SCHEMA public to storage;

    CREATE user malware;
    CREATE database malware;
    ALTER user malware WITH PASSWORD 'malware123';
    GRANT all privileges ON database malware TO malware;
    GRANT USAGE ON SCHEMA public to malware;
