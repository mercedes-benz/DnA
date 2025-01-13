package main

import (
	"context"
	"fmt"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"log"
	"strings"
)

// get k8s client set
func getK8sClient() (*kubernetes.Clientset, error) {

	config, err := rest.InClusterConfig()
	if err != nil {
		return nil, fmt.Errorf("error seen while getting cluster config \n %v", err)
	}

	clientSet, err := kubernetes.NewForConfig(config)
	if err != nil {
		return nil, fmt.Errorf("error seen while getting kuberenetes config \n %v", err)
	}

	return clientSet, nil
}

// getCredsFromSecret get the neo4j authentication details from the k8s secret stored under the key NEO4J_AUTH
func getCredsFromSecret(secretName string) (string, error) {

	log.Println("Fetching creds from secret", secretName)
	secret, err := ClientSet.CoreV1().Secrets(Namespace).Get(context.TODO(), secretName, v1.GetOptions{})
	if err != nil {
		return "", fmt.Errorf("unable to fetch details about secret %s \n %v", secretName, err)
	}
	pass, present := secret.Data["NEO4J_AUTH"]
	if !present {
		return "", fmt.Errorf("secret does not contain key NEO4J_AUTH")
	}
	if len(strings.Split(string(pass), "/")) != 2 {
		return "", fmt.Errorf("kubernetes secret pass should be of the format <username>/<password>")
	}
	return string(pass), nil
}
