const webpack = require('webpack');
const packageJson = require('../package.json');
const miniCssExtractPlugin = require('mini-css-extract-plugin');
const path = require('path');
const packageName = packageJson.name.replace(/@/, '-').replace(/\//, '-');
const version = packageJson.version.toLowerCase().trim();
const ESLintPlugin = require('eslint-webpack-plugin');
const optimizeCSSAssetsPlugin = require('css-minimizer-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const terserPlugin = require('terser-webpack-plugin');
const duplicatePackageCheckerPlugin = require('duplicate-package-checker-webpack-plugin');
const base = require('./base.config');
const { merge } = require('webpack-merge');

const prodConfig = {
  mode: 'production',
  target: 'web',
  entry: './src/index.js',
  output: {
    path: path.resolve(__dirname, '../dist'),
    filename: `${packageName}_${version}_[name].[fullhash].js`,
    chunkFilename: `${packageName}_${version}_[name].[fullhash].bundle.js`,
    clean: true,
    publicPath: 'auto',
  },
  devtool: false,
  module: {
    rules: [
      {
        test: /\.js?$/,
        exclude: /node_modules/,
        loader: 'babel-loader',
        options: {
          presets: ['@babel/preset-env' /* to transfer any advansed ES to ES5 */, '@babel/preset-react'], // to compile react to ES5
          plugins: [['@babel/plugin-transform-runtime']],
        },
      },
    ],
  },
  plugins: [
    new CleanWebpackPlugin(),
    new webpack.ids.HashedModuleIdsPlugin(),
    new duplicatePackageCheckerPlugin(),
    new ESLintPlugin({
      extensions: ['js'],
      fix: true,
    }),
    new miniCssExtractPlugin({
      filename: `${packageName}_${version}_[name].[fullhash].css`,
      chunkFilename: `${packageName}_${version}_[name].[fullhash].bundle.css`,
    }),
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
};

module.exports = merge(base, prodConfig);
