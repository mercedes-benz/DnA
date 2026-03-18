package main

import (
	"fmt"
	"io/ioutil"
	"os"

	yaml "gopkg.in/yaml.v3"
)

func main() {
	input, err := ioutil.ReadFile("values-dev.yaml")
	CheckIfError(err)

	m := make(map[interface{}]interface{})

	err = yaml.Unmarshal([]byte(input), &m)
	CheckIfError(err)
	fmt.Printf("--- m:\n%v\n\n", m["image"])

	err = ioutil.WriteFile("myfile", []byte(input), 0644)
	CheckIfError(err)

}

func CheckIfError(err error) {
	if err == nil {
		return
	}

	fmt.Printf("\x1b[31;1m%s\x1b[0m\n", fmt.Sprintf("error: %s", err))
	os.Exit(1)
}
func Info(format string, args ...interface{}) {
	fmt.Printf("\x1b[34;1m%s\x1b[0m\n", fmt.Sprintf(format, args...))
}
func Warning(format string, args ...interface{}) {
	fmt.Printf("\x1b[36;1m%s\x1b[0m\n", fmt.Sprintf(format, args...))
}
