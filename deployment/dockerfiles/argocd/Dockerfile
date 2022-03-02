# This image is needed only if we need the vault integration otherwise go with the 
FROM argoproj/argocd:v2.0.2 as builder

ENV http_proxy=""
ENV https_proxy=""

# Switch to root for the ability to perform install
USER root

# Install tools needed for your repo-server to retrieve & decrypt secrets, render manifests
# (e.g. curl, awscli, gpg, sops)
RUN apt-get update && \
    apt-get install -y \
    curl \
    awscli \
    gpg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Install the AVP plugin (as root so we can copy to /usr/local/bin)
RUN curl -L -o argocd-vault-plugin https://github.com/IBM/argocd-vault-plugin/releases/download/v1.1.1/argocd-vault-plugin_1.1.1_linux_amd64
RUN chmod +x argocd-vault-plugin
RUN mv argocd-vault-plugin /usr/local/bin

# Switch back to non-root user
USER argocd

FROM builder

ENV http_proxy=""
ENV https_proxy=""
