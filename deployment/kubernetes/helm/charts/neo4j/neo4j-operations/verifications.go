package main

import (
	"fmt"
	"os"
)

func checkRequiredVariables() error {
	_, present := os.LookupEnv("RELEASE_NAME")
	if !present {
		return fmt.Errorf("Please provide the env variable RELEASE_NAME ")
	}
	_, present = os.LookupEnv("NAMESPACE")
	if !present {
		return fmt.Errorf("Please provide the env variable NAMESPACE")
	}
	_, present = os.LookupEnv("SECRETNAME")
	if !present {
		return fmt.Errorf("please provide the env variable SECRETNAME")
	}
	_, present = os.LookupEnv("PROTOCOL")
	if !present {
		return fmt.Errorf("please provide the env variable PROTOCOL")
	}

	return nil
}
