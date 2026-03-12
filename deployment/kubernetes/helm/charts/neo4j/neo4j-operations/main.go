package main

import (
	"k8s.io/client-go/kubernetes"
	"log"
	"os"
	"strings"
	"time"
)

var (
	Namespace   string
	ReleaseName string
	ClientSet   *kubernetes.Clientset
)

func main() {

	err := checkRequiredVariables()
	if err != nil {
		log.Printf("missing required env variables \n %v \n Exiting... :-( ", err)
		os.Exit(1)
	}
	Namespace = os.Getenv("NAMESPACE")
	ReleaseName = os.Getenv("RELEASE_NAME")

	// load kubernetes config
	clientSet, err := getK8sClient()
	if err != nil {
		log.Printf("error while setting k8s client \n %v \n Exiting... :-( ", err)
		os.Exit(1)
	}
	ClientSet = clientSet

	// get password from kubernetes secret either specified in values.yaml or the generic k8s secret
	pass, err := getCredsFromSecret(os.Getenv("SECRETNAME"))
	if err != nil {
		log.Printf("error while fetching secret pass \n %v", err)
		os.Exit(1)
	}

	log.Printf("Waiting for Neo4j to startup. Sleeping for 60 seconds.")
	time.Sleep(60 * time.Second)
	// connect using the above creds
	username := strings.Split(pass, "/")[0]
	password := strings.Split(pass, "/")[1]
	err = ExecuteEnablement(username, password)
	if err != nil {
		log.Printf("error while connecting to neo4j \n %v", err)
		os.Exit(1)
	}

}
