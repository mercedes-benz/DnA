package main

import (
	"context"
	"fmt"
	"github.com/neo4j/neo4j-go-driver/v5/neo4j"
	"log"
	"os"
	"strings"
	"time"
)

func ExecuteEnablement(username, pass string) error {
	// serviceName = core-4.default.svc.cluster.local:7687
	serviceName := fmt.Sprintf("%s.%s.svc.cluster.local:7687", ReleaseName, Namespace)
	ctx := context.Background()
	driver, err := getNeo4jDriver(ctx, username, pass)
	if err != nil {
		return err
	}
	defer driver.Close(ctx)
	serverId, serverState, err := getNeo4jServerIdAndState(ctx, driver, serviceName)
	if err != nil {
		return err
	}
	if strings.ToLower(serverState) == "enabled" {
		log.Println("Server is already enabled !!")
		return nil
	}

	err = enableNeo4jServer(ctx, driver, serverId)
	if err != nil {
		return err
	}

	enabled, err := isNeo4jServerEnabled(ctx, driver, serviceName)
	if err != nil {
		return err
	}
	if !enabled {
		return fmt.Errorf("Server is NOT ENABLED !!")
	}

	log.Println("Server is ENABLED !!")
	return nil
}

func getNeo4jDriver(ctx context.Context, username, pass string) (neo4j.DriverWithContext, error) {
	log.Println("Establishing connection with Neo4j")
	retries := 5
	serviceName := fmt.Sprintf("%s.%s.svc.cluster.local:7687", ReleaseName, Namespace)
	// URI examples: "neo4j://localhost", "neo4j+s://xxx.databases.neo4j.io"
	dbUri := fmt.Sprintf("%s://%s", os.Getenv("PROTOCOL"), serviceName)
	dbUser := username
	dbPassword := pass
	var driver neo4j.DriverWithContext
	var err error
	for i := 1; i <= retries; i++ {
		driver, err = neo4j.NewDriverWithContext(
			dbUri,
			neo4j.BasicAuth(dbUser, dbPassword, ""))

		err = driver.VerifyConnectivity(ctx)
		if err != nil && i == retries {
			return nil, err
		}
		if err != nil {
			log.Printf("found error while trying to connect to Neo4j (db uri := %s)\n %s", dbUri, err.Error())
			log.Printf("sleeping for 30 seconds. Retry (%d/%d)\n", i, retries)
			time.Sleep(30 * time.Second)
			continue
		}
		break
	}
	log.Println("Connectivity established !!")
	return driver, nil
}

// enableNeo4jServer fires the cypher query ENABLE SERVER <server-id>
func enableNeo4jServer(ctx context.Context, driver neo4j.DriverWithContext, serverId string) error {

	log.Println("Enabling server")
	// Enable SERVER 'serverId'
	query := fmt.Sprintf("ENABLE SERVER \"%s\"", serverId)
	_, err := neo4j.ExecuteQuery(ctx, driver,
		query,
		nil, neo4j.EagerResultTransformer,
		neo4j.ExecuteQueryWithDatabase("system"))
	if err != nil {
		return err
	}

	return nil
}

// getNeo4jServerIdAndState returns the id and state for the provided serviceName from the list of records
func getNeo4jServerIdAndState(ctx context.Context, driver neo4j.DriverWithContext, serviceName string) (string, string, error) {
	log.Println("Fetching Neo4j server id and state")
	var serverState, serverId string
	query := fmt.Sprintf("SHOW SERVERS WHERE address='%s'", serviceName)
	result, err := neo4j.ExecuteQuery(ctx, driver,
		query,
		nil, neo4j.EagerResultTransformer,
		neo4j.ExecuteQueryWithDatabase("system"))
	if err != nil {
		return serverId, serverState, err
	}

	if len(result.Records) != 1 {
		return serverId, serverState, fmt.Errorf("more than one or no records found for address %s \n records %v", serviceName, result.Records)
	}

	serverIdAny, present := result.Records[0].Get("name")
	if !present {
		return serverId, serverState, fmt.Errorf("'name' key not present in record")
	}
	serverId = serverIdAny.(string)

	serverStateAny, present := result.Records[0].Get("state")
	if !present {
		return serverId, serverState, fmt.Errorf("'state' key not present in record")
	}
	serverState = serverStateAny.(string)

	if serverId == "" || serverState == "" {
		return "", "", fmt.Errorf("cannot find serverId and serverState for %s", serviceName)
	}
	return serverId, serverState, nil
}

// isNeo4jServerEnabled checks whether the server
func isNeo4jServerEnabled(ctx context.Context, driver neo4j.DriverWithContext, serviceName string) (bool, error) {

	_, serverState, err := getNeo4jServerIdAndState(ctx, driver, serviceName)
	if err != nil {
		return false, err
	}
	if strings.ToLower(serverState) != "enabled" {
		return false, nil
	}
	return true, nil
}
