DROP TABLE recipe_nsql;

CREATE TABLE
    recipe_nsql (
        id text NOT NULL PRIMARY KEY,
        data jsonb NOT NULL
    );

insert into
    recipe_nsql (id, data)
values
(
        '1',
        '{"recipeName": "Quarkus", "recipeType": "Quarkus", "repodetails": "User", "diskSpace": "Demo", "minCpu": "TE/ST", "maxCpu": "TE/ST", "minRam": "TE/ST", "maxRam": "TE/ST", "operatingSystem": "TE/ST", "software":{"recipeName": "Quarkus", "recipeType": "Quarkus"}, "createdOn": "1111", "createdBy":{"email": "demouser@web.de",  "lastName": "User", "firstName": "Demo", "department": "TE/ST", "mobileNumber": "2345673456", "gitUserName":"demo" }}'
    );
