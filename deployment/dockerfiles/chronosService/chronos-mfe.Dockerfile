FROM node:16.14.2 as base

# Building the frontend
WORKDIR /usr/src/packages/chronos-mfe

COPY . .
RUN yarn

RUN yarn build:docker

#use non-root nginx
FROM bitnami/nginx:latest

WORKDIR /usr/share/nginx/html
USER 0
RUN rm -rf /usr/share/nginx/html/*
COPY --from=base /usr/src/packages/chronos-mfe/dist .
COPY nginx.conf /opt/bitnami/nginx/conf/nginx.conf
RUN chmod -R g+rwX /usr/share/nginx/html
EXPOSE 3000
USER 1001