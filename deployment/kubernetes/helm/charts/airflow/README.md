## Install
``` bash
helm upgrade airflow . --namespace airflow --install -f values.yaml
```
- Add harbor-image-pull secret


- ALTER ROLE admin SET search_path = public 


- For git-sync use: k8s.gcr.io/git-sync:v3.1.5


- Connections for airflow (added manually through UI):

    1. test_input_path
        - Conn Type: File(path)
        - Extra: { “path” : “/usr/local/airflow/dags/git-sync/dags” }

    2. postgres
        - Conn Type: Postgres
        - Host: xxxxxxxxx
        - Schema: db
        - Port: 64000
        - add Login(User) and password

    3. s3_connection_aws
        - Conn Type: Amazon Web Services
        - extra: {
              “host”: “https://your URL:443”,
              “aws_access_key_id”: aws_access_key_id,
              “aws_secret_access_key”: aws_secret_access_key
            }

- Variables for airflow (added manually through UI):
    secret_ACCESS_KEY:aws_access_key_id
    secret_SECRET_KEY:aws_secret_access_key
