const HtmlWebpackPlugin = require('html-webpack-plugin');
const ModuleFederationPlugin = require('webpack/lib/container/ModuleFederationPlugin');
const deps = require('../package.json').dependencies;
const Dotenv = require('dotenv-webpack');
const path = require('path');
const ESLintPlugin = require('eslint-webpack-plugin');
const webpack = require('webpack');
const ExternalTemplateRemotesPlugin = require('external-remotes-plugin');
const copyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
  module: {
    rules: [
      {
        test: /\.(eot|woff2|woff|svg|otf)/,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 1000,
              name: 'fonts/[name]-[contenthash].[ext]',
            },
          },
        ],
      },
      {
        test: /\.(ttf)/,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 1000,
              name: 'fonts/[name].[ext]',
            },
          },
        ],
      },
      {
        test: /\.(png|jpg|gif)$/i,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 1000,
              name: 'images/[name]-[contenthash].[ext]',
            },
          },
        ],
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
        include: [path.resolve(__dirname, path.join('..', 'src'))],
      },
      {
        test: /\.(css|scss)$/,
        use: [
          {
            loader: 'style-loader',
          },
          {
            loader: 'css-loader',
          },
        ],
        exclude: [path.resolve(__dirname, path.join('..', 'src'))],
      },
      {
        test: /config.js/,
        use: [
          {
            loader: 'raw-loader',
          },
        ],
        include: [path.resolve(__dirname, path.join('..', 'public'))],
      },
    ],
  },
  plugins: [
    new Dotenv({
      path: process.env.NODE_ENV === 'development' ? '../.env' : '../.docker.env',
    }),
    new ESLintPlugin({
      extensions: ['js'],
      fix: true,
    }),
    //async await support in es6
    new webpack.ProvidePlugin({
      regeneratorRuntime: 'regenerator-runtime/runtime',
      Promise: ['es6-promise', 'Promise'],
      process: 'process/browser',
      Buffer: ['buffer', 'Buffer'],
    }),
    new HtmlWebpackPlugin({
      template: './public/index.html',
      favicon: './src/appIcon/logo.png',
    }),
    new ExternalTemplateRemotesPlugin(),
    new ModuleFederationPlugin({
      name: 'data_product_mfe',
      filename: 'remoteEntry.js',
      exposes: {
        './DataProduct': './src/App',
        './ProviderForm': './src/components/ProviderForm',
        './ConsumerForm': './src/components/ConsumerForm',
      },
      remotes: {
        'dna-container': `dna_container@[(window.DATA_PRODUCT_INJECTED_ENVIRONMENT && window.DATA_PRODUCT_INJECTED_ENVIRONMENT.CONTAINER_APP_URL)]/remoteEntry.js?[(new Date()).getTime()]`,
      },
      shared: {
        ...deps,
        react: { singleton: true, eager: true, requiredVersion: deps.react },
        'react-dom': {
          singleton: true,
          eager: true,
          requiredVersion: deps['react-dom'],
        },
        'react-router-dom': {
          singleton: true,
          eager: true,
          requiredVersion: deps['react-router-dom'],
        },
      },
    }),
    new copyWebpackPlugin({ patterns: [{ from: 'public/config.js', toType: 'dir' }] }),
  ],
};
