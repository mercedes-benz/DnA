# Clickhouse

This helmchart is being installed as subchart/dependecy for signoz helmchart with default values.

### Usage recommendation

In case you are not using a well-know reserved private IP address range that are whitelisted by default for your deployment environment (like for minikube environment), eg:
  - 10.0.0.0/8
  - 100.64.0.0/10
  - 172.16.0.0/12
  - 192.0.0.0/24
  - 198.18.0.0/15
  - 192.168.0.0/16

You must whitelist IP address range used for your enviroment (eg. kubernetes nodes IPs) manually in Signoz values chart.

```yaml
clickhouse:
  allowedNetworkIps:
    - "192.173.0.0/16"
```

Otherwise you'll be getting error message like:

```
Failed to initialize ClickHouse: error connecting to primary db: code: 516,
message: admin: Authentication failed: password is incorrect, or there is no user with such name
```
