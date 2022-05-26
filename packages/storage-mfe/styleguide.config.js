/* eslint-disable */
const path = require('path');
const cors = require('cors');
const webpack = require('webpack');

module.exports = {
  skipComponentsWithoutExample: true,
  require: [
    path.join(__dirname, './src/common/modules/uilab/bundle/css/uilab.min.css'),
    path.join(__dirname, './src/common/globals/css/main.scss'),
  ],
  moduleAliases: {
    'dna-container/Pagination': path.join(__dirname, '../frontend/src/components/mbc/pagination/Pagination.tsx'),
    'dna-container/ConfirmModal': path.join(
      __dirname,
      '../frontend/src/components/formElements/modal/confirmModal/ConfirmModal.tsx',
    ),
    'dna-container/InfoModal': path.join(
      __dirname,
      '../frontend/src/components/formElements/modal/infoModal/InfoModal.tsx',
    ),
  },
  styleguideComponents: {
    Wrapper: path.join(__dirname, 'src/styleguide/Wrapper'),
  },
  template: {
    favicon: './src/appIcon/logo.png',
  },
  sections: [
    {
      name: 'Introduction',
      content: 'README.md',
    },
    {
      name: 'UI Components',
      components: () => ['src/components/**/*.js'],
    },
  ],
  configureServer(app) {
    // `app` is the instance of the express server running Styleguidist
    app.use(cors());

    app.get(process.env.STORAGE_API_BASEURL + '/buckets', (req, res) => {
      res.status(200).send({
        data: [
          {
            bucketName: 'mock-bucket',
            collaborators: [
              {
                accesskey: 'DEMOUSER',
                hostName: null,
                permission: { read: true, write: false },
                secretKey: null,
                uri: null,
              },
            ],
            creationDate: '2022-04-25T05:09:45.334Z',
            permission: { read: true, write: true },
          },
        ],
      });
    });
    app.listen(7175);
  },
  getExampleFilename(componentPath) {
    return componentPath.replace(/\.js?$/, '.md');
  },
  webpackConfig: {
    devtool: 'inline-source-map',
    module: {
      rules: [
        {
          /* The following line to ask babel 
               to compile any file with extension
               .js */
          test: /\.js?$/,

          /* exclude node_modules directory from babel. 
              Babel will not compile any files in this directory*/
          exclude: /node_modules/,

          // To Use babel Loader
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env' /* to transfer any advansed ES to ES5 */, '@babel/preset-react'], // to compile react to ES5
            plugins: [['@babel/plugin-transform-runtime']],
          },
        },
        {
          test: /\.tsx?$/,
          use: ['babel-loader', 'ts-loader'],
        },
        {
          test: /\.(css|scss)$/,
          use: [
            {
              loader: 'style-loader',
            },
            {
              loader: 'css-loader',
              options: {
                sourceMap: true,
                importLoaders: 1,
                modules: {
                  auto: (resourcePath) => !resourcePath.includes('uilab'),
                  localIdentName: '[name]_[local]_[hash:base64:5]',
                },
              },
            },
            {
              loader: 'postcss-loader',
              options: {
                sourceMap: true,
              },
            },
            {
              loader: 'sass-loader',
              options: {
                sourceMap: true,
              },
            },
          ],
          include: [
            path.resolve(__dirname, path.join('.', 'src')),
            path.resolve(__dirname, path.join('..', 'frontend/src')),
          ],
        },
        {
          test: /\.(css|scss)$/,
          use: ['style-loader', 'css-loader'],
          exclude: [
            path.resolve(__dirname, path.join('.', 'src')),
            path.resolve(__dirname, path.join('..', 'frontend/src')),
          ],
        },
      ],
    },
    resolve: {
      alias: {
        process: 'process/browser',
        stream: 'stream-browserify',
        zlib: 'browserify-zlib',
      },
    },
    plugins: [
      new webpack.ProvidePlugin({
        regeneratorRuntime: 'regenerator-runtime/runtime',
        Promise: ['es6-promise', 'Promise'],
        process: 'process/browser',
        Buffer: ['buffer', 'Buffer'],
      }),
      new webpack.HotModuleReplacementPlugin(),
    ],
  },
};
