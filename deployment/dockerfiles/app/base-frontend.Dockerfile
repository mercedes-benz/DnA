FROM dna-base:all
WORKDIR /usr/src/packages/frontend

COPY package.json ./
RUN yarn

COPY .build ./.build
COPY public ./public
COPY src ./src
COPY test ./test
COPY .editorconfig ./
COPY .prettierrc ./
COPY jest.config.js ./
COPY postcss.config.js ./
COPY babel.config.js ./
COPY .eslintrc.js ./
COPY tsconfig.* ./
COPY tslint* ./
COPY typings.d.ts ./
COPY declaration.d.ts ./
COPY webpack.config.js ./
COPY .env ./
COPY .docker.env ./

RUN yarn build:docker
