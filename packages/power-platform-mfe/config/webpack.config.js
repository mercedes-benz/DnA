const { MFLiveReloadPlugin } = require('@module-federation/fmr');
const base = require('./base.config');
const { merge } = require('webpack-merge');

const devConfig = {
  mode: 'development',
  devtool: 'inline-source-map',
  devServer: {
    port: 8093,
    historyApiFallback: true,
    headers: {
      'Access-Control-Allow-Origin': '*',
    },
  },
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
    new MFLiveReloadPlugin({
      port: 8093,
      container: 'power-platform-mfe',
      standalone: false,
    }),
  ],
  cache: false
};

module.exports = merge(base, devConfig);