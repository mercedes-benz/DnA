#use non-root nginx
FROM bitnami/nginx:latest

USER root
WORKDIR /usr/share/nginx/html

USER 0

COPY --from=dna-base:frontend /usr/src/packages/frontend/dist/app .
COPY nginx.conf /etc/nginx/nginx.conf

COPY docker-start.sh .
COPY envsubst /usr/local/bin
RUN chmod +x /usr/local/bin/envsubst
RUN chmod +x docker-start.sh
RUN chmod -R g+rwX /usr/share/nginx/html
CMD bash docker-start.sh
USER 1001

EXPOSE 8080
