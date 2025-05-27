const HtmlWebpackPlugin = require('html-webpack-plugin');
const ModuleFederationPlugin = require('webpack/lib/container/ModuleFederationPlugin');
const webpack = require('webpack');
const packageJson = require('./package.json');
const deps = packageJson.dependencies;
const miniCssExtractPlugin = require('mini-css-extract-plugin');
const path = require('path');
const packageName = packageJson.name.replace(/@/, '-').replace(/\//, '-');
const version = packageJson.version.toLowerCase().trim();
const ESLintPlugin = require('eslint-webpack-plugin');
const optimizeCSSAssetsPlugin = require('css-minimizer-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const terserPlugin = require('terser-webpack-plugin');
const duplicatePackageCheckerPlugin = require('duplicate-package-checker-webpack-plugin');
const Dotenv = require('dotenv-webpack');
const fs = require('fs');
const copyWebpackPlugin = require('copy-webpack-plugin');
const ExternalTemplateRemotesPlugin = require('./ExternalTemplateRemotesPlugin');

const base = {
  mode: 'production',
  target: 'web',
  entry: './src/index.js',
  output: {
    path: path.resolve(__dirname, './dist'),
    filename: `${packageName}_${version}_[name].[fullhash].js`,
    chunkFilename: `${packageName}_${version}_[name].[fullhash].bundle.js`,
    clean: true,
    publicPath: 'auto',
  },
  devtool: false,
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
        test: /\.m?js/,
        resolve: {
          fullySpecified: false,
        },
      },
      {
        test: /\.js?$/,
        exclude: /node_modules/,
        // To Use babel Loader
        loader: 'babel-loader',
        options: {
          presets: ['@babel/preset-env' /* to transfer any advansed ES to ES5 */, '@babel/preset-react'], // to compile react to ES5
        },
      },
      {
        test: /\.(css|scss)$/,
        use: [
          {
            loader: miniCssExtractPlugin.loader,
          },
          {
            loader: 'css-loader',
            options: {
              modules: true,
              sourceMap: true,
              importLoaders: 1,
              // eslint-disable-next-line no-dupe-keys
              modules: {
                auto: (resourcePath) => !resourcePath.includes('uilab'),
                localIdentName: '[name]_[local]_[hash:base64:5]',
              },
            },
          },
          {
            loader: 'postcss-loader',
            options: {
              sourceMap: false,
            },
          },
          {
            loader: 'sass-loader',
            options: {
              sourceMap: false,
            },
          },
        ],
        include: [path.resolve(__dirname, path.join('.', 'src'))],
      },
      {
        test: /\.(css|scss)$/,
        use: [
          {
            loader: miniCssExtractPlugin.loader,
          },
          {
            loader: 'css-loader',
          },
        ],
        exclude: [path.resolve(__dirname, path.join('.', 'src'))],
      },
    ],
  },
  plugins: [
    new CleanWebpackPlugin(),
    new webpack.ids.HashedModuleIdsPlugin(),
    new duplicatePackageCheckerPlugin(),
    new Dotenv({
      path: './.docker.env',
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
      template: 'public/index.html',
      favicon: './src/appIcon/logo.png',
    }),
    new miniCssExtractPlugin({
      filename: `${packageName}_${version}_[name].[fullhash].css`,
      chunkFilename: `${packageName}_${version}_[name].[fullhash].bundle.css`,
    }),
    new ModuleFederationPlugin({
      name: 'storage_mfe',
      filename: 'remoteEntry.js',
      exposes: {
        './Bucket': './src/App',
      },
      remotes: {
        'dna-container': `dna_container@[(window.STORAGE_INJECTED_ENVIRONMENT && window.STORAGE_INJECTED_ENVIRONMENT.CONTAINER_APP_URL)]/remoteEntry.js?[(new Date()).getTime()]`,
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
  ],

  optimization: {
    minimizer: [
      new terserPlugin({
        extractComments: true,
        parallel: true,
        terserOptions: {
          safari10: true,
        },
      }),
      new optimizeCSSAssetsPlugin({}),
    ],
    splitChunks: {
      chunks: 'async',
    },
  },
  resolve: {
    fallback: { crypto: false },
  },
};

// copy config file part of build
if (fs.existsSync(path.join(process.cwd(), 'public'))) {
  base.plugins.push(new copyWebpackPlugin({ patterns: [{ from: 'public/config.js', toType: 'dir' }] }));
}

module.exports = base;
