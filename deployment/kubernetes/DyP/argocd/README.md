# Install Argocd with Plugin

## Apply the following yaml for vault
Before applying, update the argocd-vault-plugin-credentials secret value 
`kubectl apply -f vault-plugin/cmp-plugin.yaml`
`kubectl apply -f vault-plugin/argocd-vault-plugin-credentials.yaml`

## Update values-dhc.yaml
 1. Update value for `items.channel-teams-url` in the values.yaml
 2. export ARGOSECRET=argocd-vault-plugin-credentials
 3. helm upgrade --install argocd . -f values-<<envirnoment>>.yaml -n argocd --set "configs.cm.dex.config.connectors.config.caData=$ARGOSECRET"