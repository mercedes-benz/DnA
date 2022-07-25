const base = require('./base'),
  { merge } = require('webpack-merge'),
  webpack = require('webpack'),
  forkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin'),
  path = require('path'),
  packageJson = require(path.resolve(process.cwd(), 'package.json')),
  legacyMode = process.env.legacy === 'true';

const devConfig = {
  mode: 'development',
  devtool: 'inline-source-map',
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: [
          {
            loader: 'babel-loader',
            options: {
              babelrc: true,
              plugins: ['react-hot-loader/babel'],
            },
          },
          {
            loader: 'ts-loader',
            options: {
              transpileOnly: true,
              configFile: path.resolve(process.cwd(), legacyMode ? 'tsconfig.legacy.json' : 'tsconfig.json'),
            },
          },
        ],
      },
    ],
  },
  plugins: [
    new forkTsCheckerWebpackPlugin({
      typescript: {
        configFile: path.resolve(process.cwd(), legacyMode ? 'tsconfig.legacy.json' : 'tsconfig.json'),
        tslint: false,
      },
    }),
    new webpack.HotModuleReplacementPlugin(),
  ],
  devServer: {
    port: packageJson.config.devServer.port || '9090',
    historyApiFallback: true,
    https: packageJson.config.devServer.https || false,
  },
};

module.exports = merge(base, devConfig);
