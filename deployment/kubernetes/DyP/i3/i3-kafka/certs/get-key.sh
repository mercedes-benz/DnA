#!/usr/bin/env bash
# Replace: <<your-password>> with your password length of 20 char

rm truststore.jks keystore.p12 truststorebase64 keystorebase64

kubectl get secret dna-user  -n kafka  -o jsonpath='{.data.user\.key}' | base64 -d > user.key
kubectl get secret dna-user -n kafka -o jsonpath='{.data.user\.crt}' | base64 -d > user.crt
kubectl get secret my-cluster-cluster-ca-cert -n kafka -o jsonpath='{.data.ca\.crt}' | base64 -d > ca.crt

echo "yes" | keytool -import -trustcacerts -file ca.crt -keystore truststore.jks -storepass <<your-password>>
RANDFILE=/tmp/.rnd openssl pkcs12 -export -in user.crt -inkey user.key -name my-user -password pass:<<your-password>> -out keystore.p12

# covert to base64
cat truststore.jks | base64 > truststorebase64
cat keystore.p12 | base64 > keystorebase64
rm user.crt user.key ca.crt