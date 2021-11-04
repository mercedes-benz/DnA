# Installing the DnA platform 

## Prerequisites

### Docker 
Install the Docker desktop application from the [official website](https://www.docker.com/products/docker-desktop). 

__For Windows devices:__ 

Check the option to **Install required Windows components for WSL2**

### Node.js
Install the Node.js application from the [official website](https://nodejs.org/en/download/).

### pgAdmin
Install the pgAdmin application from the [official website](https://www.pgadmin.org/download).


## Setting up the DB

Steps to start and run the DB:
1. Install postgresql 
2. Run the following command to load a DB:
`psql -h localhost -p 5432 -U admin -d dbname < db_prod_bkp_24092020.sql`
3. yarn start:db+swagger

Steps to mount data in pgAdmin:
1.	Launch pgAdmin
2.	Create a new server by navigating to: `Server -> Create -> Server`
3. Execute the follow INC in pgadmin:

`insert into userinfo_nsql values('DEMOUSER','{"email": "demouser@web.de", "roles": [{"id": "3", "name": "Admin"}], "lastName": "User", "firstName": "Demo", "department": "TE/ST", "mobileNumber": "", "favoriteUsecases":[]}');`

## Starting the application

You can start the application by running:

```bash
Yarn install
yarn start:web+backend
```

## Extra: Working with Eclipse

You can configure lombook by following these steps:


1.	Create a new gradle project in eclipse.
2.	Copy the src and build.gradle file from `\dna\packages\backend` path to the new project.
3.	Build the project.
4.	Search for the lombook-1.xx.jar inside project and external dependencies.
5.	Run lombook-1.xx.jar as `java –jar lombook-1.xx.jar`
6.	Specify the Eclipse installation directory manually or lombook will automatically search for installed IDE.
7.	Restart eclipse.
8.	This will add below properties in eclipse.ini file
`-javaagent:C:\Users\User\Downloads\eclipse-jee-2020-06-R-win32-x86_64\eclipse\lombok.jar`
9.	Now go to project properties and enable annotation processing. This will automatically rebuild the project.
