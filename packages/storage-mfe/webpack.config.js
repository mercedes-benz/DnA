const HtmlWebpackPlugin = require('html-webpack-plugin');
const ModuleFederationPlugin = require('webpack/lib/container/ModuleFederationPlugin');
const { MFLiveReloadPlugin } = require('@module-federation/fmr');
const deps = require('./package.json').dependencies;
const Dotenv = require('dotenv-webpack');
const path = require('path');
const ESLintPlugin = require('eslint-webpack-plugin');
const webpack = require('webpack');
const fs = require('fs');
const copyWebpackPlugin = require('copy-webpack-plugin');
const ExternalTemplateRemotesPlugin = require("./ExternalTemplateRemotesPlugin");

const CONTAINER_APP_URL = process.env.CONTAINER_APP_URL ? process.env.CONTAINER_APP_URL : 'http://localhost:9090';

const base = {
  mode: 'development',
  devtool: 'inline-source-map',
  devServer: {
    port: 8083,
    historyApiFallback: true,
    headers: {
      'Access-Control-Allow-Origin': '*',
    },   
  },
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
        include: [path.resolve(__dirname, path.join('.', 'src'))],
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
        exclude: [path.resolve(__dirname, path.join('.', 'src'))],
      },
      {
        test: /config.js/,
        use: [
          {
            loader: 'raw-loader',
          },
        ],
        include: [path.resolve(__dirname, path.join('.', 'public'))],
      },
      {
        test: /\.m?js/,
        resolve: {
          fullySpecified: false,
        },
      },
    ],
  },
  plugins: [
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
    new Dotenv({
      path: './.env',
    }),
    new MFLiveReloadPlugin({
      port: 8083,
      container: 'storage-mfe',
      standalone: false,
    }),
    new ModuleFederationPlugin({
      name: 'storage_mfe',
      filename: 'remoteEntry.js',
      exposes: {
        './Bucket': './src/App',
      },
      remotes: {
        'dna-container': `dna_container@[(window.STORAGE_INJECTED_ENVIRONMENT && window.STORAGE_INJECTED_ENVIRONMENT.CONTAINER_APP_URL || '${CONTAINER_APP_URL}')]/remoteEntry.js?[(new Date()).getTime()]`,
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
    new ExternalTemplateRemotesPlugin(),
    new HtmlWebpackPlugin({
      template: './public/index.html',
      favicon: './src/appIcon/logo.png',
    }),
  ],
};

//copy config file part of build
if (fs.existsSync(path.join(process.cwd(), 'public'))) {
  base.plugins.push(
    new copyWebpackPlugin({ patterns: [{ from: 'public/config.js', toType: 'dir' }] }),
  );
}


module.exports = base;
