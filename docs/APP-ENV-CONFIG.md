#### **Environment Variables**

##### Frontend Environment Variables
###### Mounted as config.js file in frontend nginx root folder from [values.yml](https://github.com/mercedes-benz/DnA/blob/dev/deployment/kubernetes/helm/charts/frontend/values.yaml)

| Name                                                          | Default Value                                                                                                | Options       | Description                                                                                                                                                                                                     |
| --------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- | --------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| DNA_APPNAME_HEADER<br />`string`                   | `"DnA App"`                                                                                                  | NA            | APP name that comes on application header.                                                                                                                                                                      |
| DNA_APPNAME_HOME<br />`string`                     | `"Data and Analytics "`                                                                                      | NA            | APP name that comes on home page.                                                                                                                                                                               |
| DNA_CONTACTUS_HTML<br />`string`                   | `<div><p>There could be many places where you may need our help, and we are happy to support you. <p></div>` | NA            | Content that comes on contact us model.                                                                                                                                                                         |
| DNA_BRAND_LOGO_URL<br />`url`                      | `/images/branding/logo-brand.png`                                                                            | NA            | Brand logo image that comes on the application header left corner.                                                                                                                                              |
| DNA_APP_LOGO_URL<br />`url`                        | `/images/branding/logo-app.png`                                                                              |               | App logo image that comes on the application header right side.                                                                                                                                                 |
| DNA_COMPANY_NAME<br/>`string`                      | `"Company_Name"`                                                                                             | NA            | Company name that comes on content of the application.                                                                                                                                                          |
| DEPLOY_VERSION<br/>`number`                        | `"0.91"`                                                                                                     | NA            | Version of deployment.                                                                                                                                                                                          |
| **ENABLE_INTERNAL_USER_INFO<br/>`boolean`          | `false`                                                                                                      | true/false    | Only applicable if`OIDC_PROVIDER`is `INTERNAL`.                                                                                                                                                                 |
| **ENABLE_DATA_COMPLIANCE<br/>`boolean`             | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| ENABLE_JUPYTER_WORKSPACE<br/>`boolean`           | `false`                                                                                                      | true/false    | This feature provides user a Jupyter Notebook workspace environment.                                                                                                                          |
| JUPYTER_NOTEBOOK_URL<br/>`url`           | `http://<YOUR_JUPYTER_NOTEBOOK_HOST_URL>`                                                                                                      | NA    | This is the host url for Jupyter Notebook interface (ex: `http://<YOUR_JUPYTER_NOTEBOOK_HOST_URL>` .                                                                                                                           |
| JUPYTER_NOTEBOOK_OIDC_POPUP_URL<br/>`url`           | `http://<YOUR_JUPYTER_NOTEBOOK_LOGIN_REDIRECT_URL>`                                                                                                      | NA    | This is the redirect url for OAuth2 login for Jupyter Notebook interface (ex: `http://<YOUR_JUPYTER_NOTEBOOK_LOGIN_REDIRECT_URL>` .                                                                                                                           |
|JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME<br/>`number`           | `5000`                                                                                                      | NA    | This is the redirect url for OAuth2 login popup wait time on screen shown for Jupyter Notebook interface (note: `You need to disable popup blocker on the DnA App at the time you see the enable popup on notbook landing page. This time depends on how much time the redirect takes on the OAuth2 redirection based on your setup`).                                                                                                                           |
| **ENABLE_DATAIKU_WORKSPACE<br/>`boolean`           | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| ENABLE_MALWARE_SCAN_SERVICE<br/>`boolean`        | `false`                                                                                                      | true/false    | This feature provides the user to subscribe to malware sacn service using appid and api key to validate the attachemnts or files from any other system is free from malware.|
| MALWARE_SCAN_SWAGGER_UI_URL<br/>`url`                    | `http://<YOUR_MALWARESCAN_HOST_URL>/swagger-ui.html`                                                                                                       | NA    | This is the base url for showing swagger documentation of Malware Scan API (ex: `http://<YOUR_MALWARESCAN_HOST_URL>/swagger-ui.html`).                                                                                                                            |
| **ENABLE_DATA_PIPELINE_SERVICE<br/>`boolean`       | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **DATA_PIPELINES_APP_BASEURL<br/>`url`       | `http://<YOUR_DATA_PIPELINE_HOST_URL>`                                                                                                      | NA    | This is the base url for showing Airflow Data Pipeline landing page (ex: `http://<YOUR_DATA_PIPELINE_HOST_URL>` .                                                                                                                             |
| **ENABLE_MY_MODEL_REGISTRY_SERVICE<br/>`boolean`       | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **MODEL_REGISTRY_API_BASEURL<br/>`url`       | `http://<YOUR_MODAL_ML_PIPELINE_HOST_URL>/api`                                                                                                      | NA   | This is the base url for accessing kubeflow modal apis from other third party applications (ex: `http://<YOUR_MODAL_ML_PIPELINE_HOST_URL>/api` (Note: This features are coming soon. Changing/updating may result in unexpected behavior).                                                                                                                                       |
| **ENABLE_ML_PIPELINE_SERVICE<br/>`boolean`         | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **ML_PIPELINE_URL<br/>`url`       | `http://<YOUR_ML_PIPELINE_HOST_URL>`                                                                                                      | NA    | This is the base url for showing Kubeflow ML Pipeline landing page (ex: `http://<YOUR_ML_PIPELINE_HOST_URL>` (Note: This features are coming soon. Changing/updating may result in unexpected behavior).                                               |
| **ENABLE_MALWARE_SCAN_ONEAPI_INFO<br/>`boolean`    | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| ENABLE_NOTIFICATION<br/>`boolean`                | `false`                                                                                                      | true/false    | This feature enables notifications in DnA App on the actions takes place in ohter area to user. Please make sure deploy and configure nass micro service and update api base url before enabling this feature. |
| ENABLE_STORAGE_SERVICE<br/>`boolean`             | `false`                                                                                                      | true/false    | This feature provides Storage Serivce on DnA App. Make sure Storage mirco service deployed and api url configured before enabling this feature. |
| ENABLE_REPORTS<br/>`boolean`                     | `false`                                                                                                      | true/false    | This feature provides Report creation transparency on KPI. Make sure Dashboard mirco service deployed before enabling this feature. |
| OIDC_DISABLED<br />`boolean`              | true                                                                                                         | true/false    | To enable the OAuth Authentication make the value`false`                                                                                                                                                        |
| API_BASEURL<br />`url`                    | `http://<YOUR_BACKEND_HOST_URL>/api`                                                                                 | NA            | This is the base url for calling backend apis from frontend(ex: If frontend and backend runs on same host like`<YOUR_BACKEND_HOST_URL>` then backend api will defaultrun on the url path `http://<YOUR_BACKEND_HOST_URL>/api` . |
| DATA_PIPELINES_API_BASEURL<br />`url`                    | `http://<YOUR_DATA_PIPELINE_URL>/api`                                                                                 | NA            | This is the base url for calling Airflow Data Pipeline apis from frontend(ex: `http://<YOUR_DATA_PIPELINE_URL>/airflow/api` . |
| MALWARESCAN_API_BASEURL<br />`url`                    | `http://<YOUR_MALWARESCAN_API_HOST_URL>/api`                                                                                 | NA            | This is the base url for calling Malware scan apis from frontend(ex: `http://<YOUR_MALWARESCAN_API_HOST_URL>/api` . |
| DASHBOARD_API_BASEURL<br />`url`                    | `http://<YOUR_DASHBOARD_API_HOST_URL>/api`                                                                                 | NA            | This is the base url for calling Dashboard apis from frontend(ex: `http://<YOUR_DASHBOARD_API_HOST_URL>/api` . |
| NOTIFICATIONS_API_BASEURL<br />`url`                    | `http://<YOUR_NOTIFICATIONS_API_HOST_URL>/api`                                                                                 | NA            | This is the base url for calling Notifications apis from frontend(ex: `http://<YOUR_NOTIFICATIONS_API_HOST_URL>/api` . |
| OAUTH2_TOKEN_URL<br />`url`               | `<<https://dev-xxxxx.okta.com/oauth2/v1/token>>`                                                             | NA            | Get access toke api. for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/).                                                                                                           |
| OAUTH2_AUTH_URL<br />`url`                | `<<https://dev-xxxxx.okta.com/oauth2/v1/authorize>>`                                                         | NA            | OIDC auth api for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/)                                                                                                                   |
| OAUTH2_REVOKE_URL<br />`url`              | `<<https://dev-xxxxx.okta.com/oauth2/v1/revoke>>`                                                            | NA            | Revoke token api for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/).                                                                                                               |
| OAUTH2_LOGOUT_URL<br />`url`               | `<<https://dev-XXXX.okta.com/oauth2/v1/logout>>`                                                             | NA            | LOGOUT api. for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/).                                                                                                                    |
| CLIENT_IDS<br />`secret`                  | `<<Client Id>>`                                                                                              | NA            | Client id of OIDC provider.                                                                                                                                                                                     |
| REDIRECT_URLS<br />`url`                  | `<http://localhost:9090>`                                                                                  | NA            | URL to be redirected after successful login.                                                                                                                                                                    |
| OIDC_PROVIDER<br />`string`               | `OKTA`                                                                                                       | OKTA/INTERNAL | Name of ODIC provider                                                                                                                                                                                           |
| JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME<br/>`number` | `5000`                                                                                                     | NA            | OIDC popup wait time.                                                                                                                                                                                           |
| STORAGE_MFE_APP_URL<br />`url`                     | `<http://localhost:8083>>`                                                                                  | NA            | Storage Micro Frontend Application URL, needed for Enabling Storage Service.                                                                                                                                    |
| STORAGE_API_BASEURL<br />`url`                     | `<http://STORAGE_API_HOST_URL/api>`                                                                                  | NA            | This url used by Storage Micro Frontend to to STORAGE micro service backend (ex: `<http://STORAGE_API_HOST_URL/api>`).                                                                                                                                    |
| CONTAINER_APP_URL<br />`url`                     | `<http://FRONTEND_HOST_URL>`                                                                                  | NA            | This url used by Storage Micro Frontend to get rendered in a parent Frontend Application URL, needed for Enabling Storage Service to work under Main Frontend App.                                                                                                                                    |
| ENABLE_DATA_CLASSIFICATION_SECRET<br />`boolean`              | `false`                                                                                                         | true/false    | To enable the Secret data clasiification option while creating bucket make the value`true`    
|
| TOU_HTML<br />`string`              | `<div>I agree to <a href="https://YOUR_STORAGE_TERMS_OF_USE_LINK" target="_blank" rel="noopener noreferrer">terms of use</a></div>`                                                                                                         | NA   | To show Terms of use content for Sotage Service this external link providing details about storage usage like keeping personal details and secret files based on data compliance of your organization.    
|
| **ENABLE_TRINO_PUBLISH<br />`boolean`              | `false`                                                                                                         | true/false    | To enable the publishing of .parquet file to Trino feature make the value`true` 
|
| **TRINO_API_BASEURL<br />`url`              | `<http://TRINO_API_HOST_URL>/api`                                                                                                         | NA    | Base api host url of Trino micro service to enable communication from storage micro frontend.
|
| INTERNAL_USER_TEAMS_INFO<br/>`string`            | `(Recommended to use Short ID. To find Short ID use <a href="YOUR_TEAMS_INFO_URL" target="_blank" rel="noreferrer noopener">Teams</a>)` | NA            | Content that comes on user search label
| ENABLE_DATA_PRODUCT<br/>`boolean`                     | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| DATA_PRODUCT_MFE_APP_URL<br />`url`                     | `<<http://localhost:8084>>`                                                                                  | NA            | Data Product Micro Frontend Application URL, needed for enabling Data Product
|
##### Backend Environment Variables


| Name                                      | Deafult Value                                            | Options             | Description                                                                                           |
| :------------------------------------------ | ---------------------------------------------------------- | --------------------- | ------------------------------------------------------------------------------------------------------- |
| API_DB_URL<br />`url`                     | `<<jdbc:postgresql://localhost:5432/db>>`                | NA                  | Database host address                                                                                 |
| API_DB_USER<br />`string`                 | `<<User id>>`                                            |                     | Database user name                                                                                    |
| API_DB_PASS<br />`string`                 | `<<password>>`                                           |                     | Database password                                                                                     |
| **ATTACHMENT_MALWARE_SCAN<br />`boolean`        | `false`                                                  | true/false          | To enable file attachment scan before upload.                                                         |
| **AVSCAN_URI<br />`url`                         | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **AVSCAN_APP_ID<br />`secret`                      | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **AVSCAN_API_KEY<br />`secret`                     | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| BYPASS_JWT_AUTHENTICATION<br />`string`         | `/api/login;/api/verifyLogin;/api/subscription/validate` |                     | URL to bypass token verification.                                                                     |
| CORS_ORIGIN_URL<br />`url pattern`        | `http://*`                                               | NA                  | CORS origin url restriction pattern.                                                                  |
| **DATAIKU<br />`boolean`                        | `false`                                                  | true/false          | To enable Dataiku feature.                                                                            |
| **DATAIKU_PROD_URI<br />`url`                   | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **DATAIKU_PROD_API_KEY<br />`secret`            | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **DATAIKU_PROD_ADMIN_GROUP<br />`url`           | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **DATAIKU_TRAINING_URI<br />`url`               | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **DATAIKU_TRAINING_API_KEY<br />`secret`           | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **DATAIKU_TRAINING_ADMIN_GROUP<br />`string`       | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| *DRD_INTERNAL_USER_ENABLED<br />`boolean`        | `false`                                                    | `true/false`                  | To enable internal user                                                                               |
| *DASHBOARD_URI<br />`url`                        | NA                                                         | NA                  | Url to connect with dashboard service                                                                 |
| *FLYWAY_ENABLED<br />`boolean`                   | `true`                                                  | `true/false`          | To enable flyway                                                                                      |
| FLYWAY_BASELINE_ON_MIGRATE<br />`boolean`       | `true`                                                  | `true/false`          | To enable flyway baseline migration                                                                   |
| FLYWAY_BASELINEVERSION<br />`int`               | `0`                                                        | `0/1/2...`            | Flyway baseline version                                                                               |
| FLYWAY_SCHEMA<br />`string`                     | `public`                                                   |                     | Flyway schema                                                                                         |
| INACTIVE_SOLUTION_DURATION_YRS<br />`int` | `2`                                                      | NA                  | Solution will be deleted if inactive for`value` configured.                                           |
| **ITSMM<br />`boolean`                          | `false`                                                  | true/false          | To enable itsmm notebook feature.                                                                     |
| **INTERNAL_USER_REQUEST_URL<br />`url`          | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **INTERNAL_USER_CERT_FILE<br />`string`         | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **INTERNAL_USER_CERT_PASS<br />`secret`         | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **JUPYTER_NOTEBOOK<br />`boolean`               | `false`                                                  | true/false          | To enable jupyter notebook feature.                                                                   |
| JWT_TOKEN_EXPIRY_TIME_IN_MIN<br />`int`         | `90`                                                     | NA                  | Set jwt token exipry                                                                                  |
| **JUPYTER_NOTEBOOK_BASEURI<br />`url`           | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **JUPYTER_NOTEBOOK_TOKEN<br />`secret`          | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| *JWT_SECRET_KEY<br />`secret`                    | NA                                                       | NA                  | Default jwt secret key. key                                                                                |
| *LOGGING_ENVIRONMENT<br />`string`               | `DEV`                                                         | `DEV/PROD`            | Environment name                                                                                      |
| *LOGGING_PATH<br />`string`                      | `/logs`                                                         | NA                  | Path for log file                                                                                     |
| **MATOMO_SITE_ID<br />`string`                  | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **MATOMO_HOST_URL<br />`url`                    | NA                                                         | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| *NAAS_BROKER<br />`url`                          | `<<localhost:9092>>`                                           |                     | Notification service url                                                                              |
| OIDC_DISABLED<br />`boolean`              | `true`                                                   | true/false          | To enable the OAuth Authentication make the value`false`                                              |
| OIDC_ISSUER<br />`url`                          | `<<https://dev-xxxxx.okta.com/oauth2/default>>`          | NA                  | OIDC issuer url.                                                                                      |
| OIDC_CLIENT_SECRET<br />`secret`          | `<<Client Secret>>`                                      | NA                  | Client secret of OIDC provider.                                                                       |
| OIDC_CLIENT_ID<br />`secret`              | `<<Client Id>>`                                          | NA                  | Client id of OIDC provider.                                                                           |
| OIDC_USER_INFO_URL<br />`url`             | `<<https://dev-xxxxx.okta.com/oauth2/v1/userinfo>>`      | NA                  | User Info api for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/).        |
| OIDC_TOKEN_INTROSPECTION_URL<br />`url`   | `<<https://dev-xxxxx.okta.com/oauth2/v1/introspect>>`    | NA                  | Token introspect api for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/). |
| OIDC_TOKEN_REVOCATION_URL<br />`url`      | `<<https://dev-xxxxx.okta.com/oauth2/v1/revoke>>`        | NA                  | Revoke token api for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/).     |
| OIDC_PROVIDER<br />`string`               | `OKTA`                                                   | <br />OKTA/INTERNAL | Name of ODIC provider.                                                                                |
| REDIRECT_URL<br />`url`                   | `<<http://localhost:9090>>`                              | NA                  | URL to be redirected after successful login.                                                          |
| *S3_EP_URL<br />`url`                     | `https://s3-xxxx.com:443`                                | NA                  | S3 bucket url.                                                                                        |
| *S3_ACCESS_KEY<br />`secret`              | `<<access_key>>`                                         | NA                  | S3 access key.                                                                                        |
| *S3_BUCKET_NAME<br />`string`             | `Bucket_name`                                            | NA                  | S3 bucket name.                                                                                       |
| *S3_SECRET_KEY<br />`secret`              | `<<secret_key>>`                                         |                     | S3 secret key.                                                                                        |
| *S3_MAX_PARALLEL_UPLOADTHREADS<br />`int` | `20`                                                     | NA                  | To restrct no of parallel thread to upload file ot S3                                                 |
| *S3_MIN_FILESIZE<br />`int`               | `1024`                                                   | NA                  |                                                                                                       |
| *S3_MAX_FILESIZE<br />`int`               | `5242880`                                                | NA                  |                                                                                                       |
| SWAGGER_HEADER_AUTH<br />`string`               | NA                                                         | NA                  | Auth token for swagger ui                                                                             |
| USER_ROLE<br />`string`                         | `Admin`                                                  | User, Admin         | Available role is [`User`,`Admin`]                                                                    |
| *VAULT_HOST<br />`url`                           | `localhost`                                                |                     | Host name of Hashicorp vault                                                                          |
| *VAULT_PORT<br />`string`                        | `8200`                                                     |                     | Port number of Hashicorp vault                                                                        |
| *VAULT_SCHEME<br />`string`                      | `http`                                                     |                     | Protocol to connect with Hashicorp vault                                                              |
| *VAULT_AUTHENTICATION<br />`string`              | `TOKEN`                                                    |                     | Authentication method to connect with Hashicorp vault                                                 |
| *VAULT_TOKEN<br />`secret`                       | `***REMOVED***`                     |                     | Admin token to connect with Hashicorp. vault                                                           |
| *VAULT_MOUNTPATH<br />`string`                   | `secret`                                                   |                     | Mount path to store secret in Hashicorp. vault                                                         |
| *VAULT_PATH<br />`string`                        | `dna/avscan`                                               |                     | Path in which secrets to be stored in Hashicorp. vault                                                 |

##### Airflow Backend Environment Variables


| Name                                   | Deafult Value                             | Options            | Description                                                   |
| :--------------------------------------- | ------------------------------------------- | -------------------- | --------------------------------------------------------------- |
| *API_DB_URL<br />`url`                  | `<<jdbc:postgresql://localhost:5432/db>>` | NA                 | Database host address.                                         |
| *API_DB_USER<br />`string`              | `<<User id>>`                             |                    | Database user name.                                            |
| *API_DB_PASS<br />`string`              | `<<password>>`                            |                    | Database password.                                             |
| *AIRFLOW_GIT_URI<br />`url`                   | `https:git/dna/XXX`                       | NA                 | GIT url to push DAG for airflow.                              |
| *AIRFLOW_GIT_MOUNTPATH<br />`string`          | `*\GITTest\airflow-user-dags`             | NA                 | Path to clone Airflow DAG repository.                         |
| *AIRFLOW_GIT_TOKEN<br />`string`              | NA                                        | NA                 | Token to connect with Airflow DAG repository.                 |
| *AIRFLOW_GIT_BRANCH<br />`string`             | `master`                                  | `master/development/any branch` | Airflow DAG repository branch name in which DAG to be pushed. |
| *AIRFLOW_DAG_MENU_CREATE_WAIT_TIME<br />`int` | `4`                                       | NA                 | Wait time in second for airflow dag menu creation.            |
| *CORS_ORIGIN_URL<br />`url pattern`     | `http://*`                                | NA                 | CORS origin url restriction patterm.                          |
| *DNA_AUTH_ENABLE<br />`boolean`               | `false`                                   | `true/false`         | To enable authorization from DnA backend.                     |
| DNA_URI<br />`url`                           | `http://localhost:7171`                   | NA                 | DnA backend url to validate jwt token.                        |
| *DAG_PATH<br />`string`                       | `dags`                                    | NA                 | Path inside airflow DAG repository where DAG to be pushed.    |
| *DAG_FILE_EXTENSION<br />`string`             | `py`                                      | NA                 | DAG file extension example: for python py.                    |
| *FLYWAY_ENABLED<br />`boolean`                | `true`                                   | `true/false`         | To enable flyway.                                              |
| FLYWAY_BASELINE_ON_MIGRATE<br />`boolean`    | `true`                                   | `true/false`         | To enable flyway baseline migration.                           |
| FLYWAY_BASELINEVERSION<br />`int`            | 0                                         | `0/1/2...`           | Flyway baseline version.                                       |
| FLYWAY_SCHEMA<br />`string`                  | `public`                                    |                    | Flyway schema name.                                                 |
| *JWT_SECRET_KEY<br />`secret`                 | NA                                        | NA                 | Default jwt secret key.                                        |
| *LOGGING_ENVIRONMENT<br />`string`            | `DEV`                                          | `DEV/PROD/any environment name`           | Environment name.                                              |
| *LOGGING_PATH<br />`string`                   | `/logs`                                          | NA                 | Path for log file.                                             |
| SWAGGER_HEADER_AUTH<br />`secret`            |                                           | NA                 | Auth token for swagger ui.                                     |

##### Dashboard Backend Environment Variables


| Name                                | Deafult Value                             | Options    | Description                               |
| :------------------------------------ | ------------------------------------------- | ------------ | ------------------------------------------- |
| *API_DB_URL<br />`url`               | `<<jdbc:postgresql://localhost:5432/db>>` | NA         | Database host address.                     |
| *API_DB_USER<br />`string`           | `<<User id>>`                             | NA           | Database user name.                        |
| *API_DB_PASS<br />`string`           | `<<password>>`                            | NA           | Database password.                         |
| *CORS_ORIGIN_URL<br />`url pattern`  | `http://*`                                | NA         | CORS origin url restriction patterm.      |
| *DNA_AUTH_ENABLE<br />`boolean`            | `false`                                   | `true/false` | To enable authorization from DnA backend. |
| DNA_URI<br />`url`                        | `http://localhost:7171`                   | NA         | DnA backend url to validate jwt token.    |
| *FLYWAY_ENABLED<br />`boolean`             | `true`                                   | `true/false` | To enable flyway.                          |
| FLYWAY_BASELINE_ON_MIGRATE<br />`boolean` | `true`                                   | `true/false` | To enable flyway baseline migration.       |
| FLYWAY_BASELINEVERSION<br />`int`         | `0`                                         | `0/1/2...`   | Flyway baseline version.                   |
| FLYWAY_SCHEMA<br />`string`               | `public`                                    | `Schema_name`           | Flyway schema.                             |
| JWT_SECRET_KEY<br />`secret`              | NA                                        | NA         | Default jwt secret key.                    |
| *LOGGING_ENVIRONMENT<br />`string`         | `DEV`                                          | `DEV/PROD`   | Environment name.                         |
| *LOGGING_PATH<br />`string`                | `/logs`                                          | NA         | Path for log file.                        |
| SWAGGER_HEADER_AUTH<br />`secret`         | NA                                          | NA         | Auth token for swagger ui.                 |

##### Notification Backend Environment Variables


| Name                                       | Deafult Value                             | Options    | Description                                                    |
| :------------------------------------------- | ------------------------------------------- | ------------ | ---------------------------------------------------------------- |
| *API_DB_URL<br />`url`                      | `<<jdbc:postgresql://localhost:5432/db>>` | NA         | Database host address.                                          |
| *API_DB_USER<br />`string`                  | `<<User id>>`                             | NA           | Database user name.                                             |
| *API_DB_PASS<br />`string`                  | `<<password>>`                            | NA           | Database password.                                              |
| *CORS_ORIGIN_URL<br />`url pattern`         | `http://*`                                | NA         | CORS origin url restriction patterm.                           |
| *DNA_MAIL_SERVER_HOST<br />`string`         | `<<localhost>>`                           | NA           | Mail server host name.                                         |
| *DNA_MAIL_SERVER_PORT<br />`string`         | `<<port>>`                                | NA           | Mail server port number.                                       |
| *DNA_AUTH_ENABLE<br />`boolean`                   | `false`                                   | `true/false` | To enable authorization from DnA backend.                      |
| *DNA_URI<br />`url`                               | `http://localhost:7171`                   | NA         | DnA backend url to validate jwt token.                         |
| *DNA_USER_NOTIFICATION_PREF_GET_URI<br />`string` | /api/notification-preferences             | NA         | Url path to get user notification preference from DnA backend. |
| *DNA_NOTIFICATION_SENDER_EMAIL<br />`email`      | `XXXXX@dna-XXXXX`                         | NA         | DnA notification sender email.                                 |
| JWT_SECRET_KEY<br />`secret`                     | NA                                        | NA         | Default jwt secret key.                                         |
| *LOGGING_ENVIRONMENT<br />`string`                | `DEV`                                          | `DEV/PROD`   | Environment name.                                              |
| *LOGGING_PATH<br />`string`                       | `/logs`                                          | NA         | Path for log file.                                             |
| *MAX_POLL_RECORDS<br />`int`                | `5000`                                    | NA         | maximum no of records needed to pulled in on poll.             |
| *NAAS_BROKER<br />`url`                     | `<<localhost:9092>>`                      | NA           | Kafka broker url.                                              |
| *NAAS_CENTRAL_TOPIC<br />`string`           | `dnaCentralEventTopic`                    | NA           | Kafka central topic where event to be published.               |
| *NAAS_CENTRALREAD_TOPIC<br />`string`       | `dnaCentralReadTopic`                     | NA           | Kafka central topic where read messages will be pushed.        |
| *NAAS_CENTRALDELTE_TOPIC<br />`string`      | `dnaCentralDeleteTopic`                   | NA           | Kafka central topic where deleted messages will be pushed.     |
| *POLL_TIME<br />`int`                       | `5000`                                    | NA         | waiting time in milliseconds for each poll.                    |
| SWAGGER_HEADER_AUTH`string`                | NA                                          | NA         | Auth token to access swagger ui.                               |

##### Malware scanner Backend Environment Variables


| Name                               | Deafult Value                                         | Options           | Description                                                 |
| :----------------------------------- | ------------------------------------------------------- | ------------------- | ------------------------------------------------------------- |
| *AUTH_API_HOST<br />`url`                 | `<<http://localhost:7171/api/subscription/validate>>` | NA                | url to validate subscription for the service.               |
| AUTH_API_TOKEN<br />`secret`             | NA                                                    | NA                | NA                                                          |
| *API_REQUEST_LIMIT<br />`int`             | `1`                                                   | `1,2,3...`        | Limit to allow number of request in given time span.        |
| *CORS_ORIGIN_URL<br />`url pattern` | `http://*`                                            | NA                | CORS origin url restriction patterm.                        |
| *CLAMAV_BACKEND_URL<br />`string`   | `localhost`                                           | NA                | CLAMAV url for file scan.                                   |
| *CLAMAV_BACKEND_PORT<br />`int`     | `3310`                                                | NA                | CLAMAV port.                                                |
| *LOGGING_ENVIRONMENT<br />`string`        | `DEV`                                                      | DEV/PROD          | Environment name.                                           |
| *LOGGING_PATH<br />`string`               | `/logs`                                                      | NA                | Path for log file.                                          |
| *MAX_FILE_SIZE<br />`int`           | `10MB`                                                | `upto 3000MB`     | Maximum allowed file size.                                  |
| *MAX_REQUEST_SIZE<br />`int`        | `11MB`                                                | `upto 3000MB`     | Maximum allowed request size.                               |
| ONEAPI_BASICAUTH_TOKEN<br />`secret`     | NA                                                    | NA                | Basic authorization token to allow connection from one API. |
| *RESTRICTED_URL_PATTERN<br />`string`     | `/avscan/api/v1/scan.*`                               | NA                | Restricted url pattern.                                     |
| *TIME_UNIT<br />`string`                  | `seconds`                                             | `seconds/minutes` | Time unit to restrict api request limit.                    |
| *WITH_IN<br />`int`                       | `20`                                                  | `any number`      | Time for which number of api request allowed.               |

##### Storage Backend Environment Variables


| Name                               | Deafult Value                        | Options       | Description                                           |
| :----------------------------------- | -------------------------------------- | --------------- | ------------------------------------------------------- |
| ATTACHMENT_MALWARE_SCAN<br />`boolean`   | `false`                              | `true/false`    | To enable malware scan for attachments.               |
| *CORS_ORIGIN_URL<br />`url pattern` | `http://*`                           | NA            | CORS origin url restriction patterm.                  |
| *DNA_AUTH_ENABLE<br />`boolean`           | `false`                              | `true/false`    | To enable authorization from DnA backend.             |
| DNA_URI<br />`url`                       | `http://localhost:7171`              | NA            | DnA backend url to validate jwt token.                |
| *JWT_SECRET_KEY<br />`secret`             | NA                                   | NA            | Default jwt secret key.                                |
| *LOGGING_ENVIRONMENT<br />`string`        |  `DEV`                                    | `DEV/PROD/Any environment name`      | Environment name.                                      |
| *LOGGING_PATH<br />`string`               |  `/logs`                                    | NA            | Path for log file.                                     |
| *MAX_FILE_SIZE<br />`int`           | `10MB`                               | `upto 3000MB` | Maximum allowed file size.                            |
| *MAX_REQUEST_SIZE<br />`int`        | `11MB`                               | `upto 3000MB` | Maximum allowed request size.                         |
| MALWARE_SCANNER_APP_ID<br />`secret`     | NA                                   | NA            | Application ID of malware scan service subscription.  |
| MALWARE_SCANNER_API_KEY<br />`string`    | NA                                   | NA            | API key of malware scan service subscription.         |
| MALWARE_SCANNER_URI<br />`url`           | `<<http://localhost:7171>>`          | NA            | Malware scanner service url.                          |
| *MINIO_ENDPOINT<br />`url`                | `<<http://localhost:9000>>`          | NA            | Minio endpoint url.                                   |
| *MINIO_ADMIN_ACCESS_KEY<br />`secret`     | NA                                   | NA            | Admin access key of Minio.                            |
| *MINIO_ADMIN_SECRET_KEY<br />`secret`     | NA                                   | NA            | Admin secret key of Minio.                            |
| *MINIO_POLICY_VERSION<br />`string`       | NA                                   | NA            | Minio policy version.                                 |
| SWAGGER_HEADER_AUTH<br />`string`        |  NA                                    | NA            | Auth token for swagger ui.                             |
| *VAULT_HOST<br />`url`                    | localhost                            | NA              | Host name of Hashicorp vault.                          |
| *VAULT_PORT<br />`string`                 | 8200                                 | NA              | Port number of Hashicorp vault.                        |
| *VAULT_SCHEME<br />`string`               | http                                 | NA              | Protocol to connect with Hashicorp vault.              |
| *VAULT_AUTHENTICATION<br />`string`       | TOKEN                                | NA              | Authentication method to connect with Hashicorp vault. |
| *VAULT_TOKEN<br />`secret`                | ***REMOVED*** | NA              | Admin token to connect with Hashicorp vault.           |
| *VAULT_MOUNTPATH<br />`string`            | secret                               | NA              | Mount path to store secret in Hashicorp vault.         |
| *VAULT_PATH<br />`string`                 | dna/storage                          | NA              | Path in which secrets to be stored in Hashicorp vault. |

<br />

**Note**

1. Marked `*`are mandatory.
2. Marked `**` These features are coming soon. Changing/updating may result in unexpected behavior.

**Note for Portfolio Locations Widget**

In order to make locations widget understand and display your locations correctly, the locations you have confiugured in Database at table location_nsql also need the locations corordinates information specified in [countries.json](https://github.com/mercedes-benz/DnA/blob/main/packages/frontend/src/globals/maps/countries.json) (DnA/packages/frontend/src/globals/maps/countries.json).

Example JSON - For location1 and location2
```json
[
    {
        "name": "location1",
        "latlng": [
            12.5, 
            -69.96666666
        ]
    },
    {
        "name": "location2",
        "latlng": [
            33, 
            65
        ]
    }
]
```
