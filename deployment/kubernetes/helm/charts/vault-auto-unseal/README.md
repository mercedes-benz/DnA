# vault-unseal-cronjob

A chart for unseal Vault cron job

**Homepage:** <https://github.com/mercedes-benz/DnA/tree/dev/utils/vaultUnseal>

## Maintainers

| Name | Email | Url |
| ---- | ------ | --- |
| sathish |  |  |

## Source Code

* <https://github.com/mercedes-benz/DnA/tree/dev/utils/vaultUnseal>

## Environment Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| VAULT_TOKEN | string | `""` | Root token of the vault |
| VAULT_ADDR | string | `""` | Http Url of the vault |
| KUBERNETES_SERVICE_HOST | string | `""` | Optional : Host address of k8s cluster. When its runs on k8s, it will take the value |
| KUBERNETES_SERVICE_PORT | string | `""` |  Optional : Post number of k8s cluster. When its runs on k8s, it will take the value |

----------------------------------------------

