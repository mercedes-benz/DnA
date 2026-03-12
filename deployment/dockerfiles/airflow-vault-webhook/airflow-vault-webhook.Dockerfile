FROM registry-emea.app.corpintra.net/dockerhubcache/python:3.11-slim

USER 0
ARG HTTP_PROXY
ARG NO_PROXY

# Set environment variables from build args
ENV HTTP_PROXY=${HTTP_PROXY}
ENV http_proxy=${HTTP_PROXY}
ENV HTTPS_PROXY=${HTTP_PROXY}
ENV https_proxy=${HTTP_PROXY}
ENV NO_PROXY=${NO_PROXY}
ENV no_proxy=${NO_PROXY}

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better caching
COPY requirements-vault-injector.txt .

# Install dependencies
RUN pip install --no-cache-dir -r requirements-vault-injector.txt

# Copy webhook code
COPY vault-injector-webhook.py .

# Create non-root user with numeric UID
RUN adduser --disabled-password --gecos '' --uid 10001 webhook-user
ADD chain.crt /usr/local/share/ca-certificates/ca-bundle.crt
RUN chmod 644 /usr/local/share/ca-certificates/ca-bundle.crt && update-ca-certificates

USER 10001

# Expose HTTPS port
EXPOSE 8443

# Run the webhook with SSL/TLS support
# The Python script contains SSL configuration in __main__ block
CMD ["uvicorn", "vault-injector-webhook:app", \
     "--host", "0.0.0.0", \
     "--port", "8443", \
     "--ssl-certfile", "/etc/webhook/certs/tls.crt", \
     "--ssl-keyfile", "/etc/webhook/certs/tls.key"]