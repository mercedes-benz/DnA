FROM node:16.14.2 as base

# Building the frontend
WORKDIR /usr/src/packages/frontend

COPY . .
RUN yarn

RUN yarn build:docker

#use non-root nginx
FROM bitnami/nginx:latest

WORKDIR /usr/share/nginx/html
RUN rm -rf /usr/share/nginx/html/*
COPY --from=base /usr/src/packages/frontend/dist/app .
COPY nginx.conf /opt/bitnami/nginx/conf/nginx.conf
EXPOSE 3000
USER 1001