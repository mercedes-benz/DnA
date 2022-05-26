const base = require('./base');
const { merge } = require('webpack-merge');
const path = require('path');
const terserPlugin = require('terser-webpack-plugin');
const optimizeCSSAssetsPlugin = require('css-minimizer-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const prodMode = process.env.build === 'prod';
const packageJson = require(path.resolve(process.cwd(), 'package.json'));
const htmlWebpackMultiBuildPlugin = require('html-webpack-multi-build-plugin');

const prodConfig = {
  name: 'ProdConfig',
  mode: 'production',
  devtool: false,
  output: {
    publicPath: packageJson.config.publicPath || '',
  },
  module: {
    rules: [
      // {
      //   test: /\.tsx?$/,
      //   enforce: 'pre',
      //   use: [
      //     {
      //       loader: 'tslint-loader',
      //       options: {
      //         emitErrors: true,
      //         tsConfigFile: prodMode ? 'tsconfig.json' : 'tsconfig.legacy.json',
      //         fix: true,
      //       },
      //     },
      //   ],
      //   exclude: [path.resolve(process.cwd(), 'stories'), path.resolve(process.cwd(), 'test')],
      // },
      {
        test: /\.tsx?$/,
        use: [
          {
            loader: 'ts-loader',
            options: {
              configFile: prodMode ? 'tsconfig.json' : 'tsconfig.legacy.json',
            },
          },
        ],
        exclude: [path.resolve(process.cwd(), 'stories'), path.resolve(process.cwd(), 'test')],
      },
    ],
  },
  plugins: [
    new CleanWebpackPlugin(),
    new htmlWebpackMultiBuildPlugin(),
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
    concatenateModules: true,
  },
  stats: process.env.verbose === 'true' ? 'normal' : 'errors-only',
};

module.exports = merge(base, prodConfig);
