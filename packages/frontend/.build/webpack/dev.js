const base = require('./base'),
  { merge } = require('webpack-merge'),
  webpack = require('webpack'),
  forkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin'),
  path = require('path'),
  packageJson = require(path.resolve(process.cwd(), 'package.json')),
  legacyMode = process.env.legacy === 'true';
ESLintPlugin = require('eslint-webpack-plugin');

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
    new ESLintPlugin({
      extensions: ['tsx'],
      fix: true,
    }),
  ],
  devServer: {
    hot: true,
    host: packageJson.config.devServer.host || '0.0.0.0',
    port: packageJson.config.devServer.port || '9090',
    historyApiFallback: true,
    https: packageJson.config.devServer.https || false,
    stats: process.env.verbose === 'true' ? 'normal' : 'errors-only',
    publicPath: packageJson.config.devServer.publicPath || '',
  },
};

module.exports = merge(base, devConfig);
