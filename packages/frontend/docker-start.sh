#!/bin/bash
echo "===Docker-start.sh==="
echo "Following ProjectsMO variables are going to be substituted:"
printf '%s ' $(sh -c "env|grep -i PROJECTSMO_ |cut -d'=' -f1")
echo "===Docker-start.sh==="
echo "Checking DNS nameserver ..."

[ -z "$PROJECTSMO_DNS_RESOLVER_ADDRESS" ] && \
echo "PROJECTSMO_DNS_RESOLVER_ADDRESS is not set. Using values from /etc/resolv.conf ..." && \
export PROJECTSMO_DNS_RESOLVER_ADDRESS=$(awk '/^nameserver/{print $2}' /etc/resolv.conf);
echo "DNS Resolver set to: $PROJECTSMO_DNS_RESOLVER_ADDRESS"
echo "Changing to directory and setting environment variables"
cd /usr/share/nginx/html
env > env.txt
export JS_FILE=$(find . -name '*.js')
export JS_MAP_FILE=$(find . -name '*.js.map')
env > env_new.txt

# echo "Processing app.js ($APP_FILE). Execute ENV replacement ..."
# cp $APP_FILE app.bak
# cp $LEGACY_APP_FILE legacy.bak
echo "Processing ($JS_FILE). Execute ENV replacement ..."
perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' -i $JS_FILE
echo "Processing ($JS_MAP_FILE). Execute ENV replacement ..."
perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' -i $JS_MAP_FILE
echo "Starting NGINX"
nginx -c /etc/nginx/nginx.conf -g 'daemon off;'
