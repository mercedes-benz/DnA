FROM node:14
WORKDIR /usr/src/

COPY package.json ./
COPY .yarnrc ./

COPY packages/frontend/package.json ./packages/frontend/

RUN yarn

