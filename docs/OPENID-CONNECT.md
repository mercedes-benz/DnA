## Overview

This documentation provides details about DNA Authentication process using OIDC provider *OKTA*.

## Connect with OKTA

## Pre-requisites

Please create OKTA developer account first and then follow the steps to create application [Create OKTA Application](https://developer.okta.com/docs/guides/implement-grant-type/authcodepkce/main/#overview).

## Steps to integrate OKTA with DNA

### Using Docker compose up-

Update "docker-compose-local-basic.yml" under folder `cd <clonned>/deployment/`, add the following under **result-backend** properties-

```
environment:

- "OIDC_PROVIDER=OKTA"
- "OIDC_ISSUER=[Base URL]/oauth2/default"
- "OIDC_TOKEN_INTROSPECTION_URL=[Base URL]/oauth2/v1/introspect "
- "OIDC_TOKEN_REVOCATION_URL=[Base URL]/oauth2/v1/revoke"
- "OIDC_USER_INFO_URL=[Base URL]/oauth2/v1/userinfo"
- "OIDC_DISABLED=false"
- "OIDC_CLIENT_ID=(Client Id)"
- "OIDC_CLIENT_SECRET=(Client secret)"
```

add the following under **result-frontend** properties-

```
environment:

- "PROJECTSMO_FRONTEND_OIDC_PROVIDER=OKTA"
- "PROJECTSMO_FRONTEND_OAUTH2_LOGOUT_URL=[Base URL]/oauth2/v1/logout"
- "PROJECTSMO_FRONTEND_OAUTH2_REVOKE_URL=[Base URL]/oauth2/v1/revoke"
- "PROJECTSMO_FRONTEND_OAUTH2_TOKEN_URL=[Base URL]/oauth2/v1/token"
- "PROJECTSMO_FRONTEND_OAUTH2_AUTH_URL=[Base URL]/oauth2/v1/authorize"
- "PROJECTSMO_FRONTEND_CLIENT_IDS=(Client Id)"
- "PROJECTSMO_FRONTEND_REDIRECT_URLS=http://localhost:9090"
```

### Using any code editor

#### Backend properties update

Locate the "**application.yaml**" inside backend folder `cd clonnedpackages/backend/dnambc-lib/src/main/resources/`, update the following properties-

```
  oidc.provider: ${OIDC_PROVIDER:OKTA}
  oidc.token.issuer: ${OIDC_ISSUER:[Base URL]/oauth2/default} 
  oidc.token.issuer.introspection.url: ${OIDC_TOKEN_INTROSPECTION_URL:[Base URL]/oauth2/v1/introspect } 
  oidc.token.issuer.revocation.url: ${OIDC_TOKEN_REVOCATION_URL:[Base URL]/oauth2/v1/revoke}
  oidc.token.userinfo.url: ${OIDC_USER_INFO_URL:[Base URL]/oauth2/v1/userinfo}
  oidc.disabled: ${OIDC_DISABLED:false}
  oidc.clientId: ${OIDC_CLIENT_ID:Client Id}
```


### UI properties update

Locate the "**.env**" in` root dir`, update the following properties-

```
 CLIENT_IDS=(Client Id)
 REDIRECT_URLS=http://localhost:9090
 OAUTH2_AUTH_URL=[Base URL]/oauth2/v1/authorize
 OAUTH2_TOKEN_URL=[Base URL]/oauth2/v1/token
 OAUTH2_REVOKE_URL=[Base URL]/oauth2/v1/revoke
 OAUTH2_LOGOUT_URL=[Base URL]/oauth2/v1/logout
 OIDC_PROVIDER=OKTA
```

##### Links

* [About DNA deployment.](/README.md)
* [Local run using Docker Compose](./SETUP-DOCKER-COMPOSE.md)
