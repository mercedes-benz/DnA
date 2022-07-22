const { MFLiveReloadPlugin } = require('@module-federation/fmr');
const base = require('./base.config');
const { merge } = require('webpack-merge');

const devConfig = {
  mode: 'development',
  devtool: 'inline-source-map',
  devServer: {
    port: 8084,
    historyApiFallback: true,
    headers: {
      'Access-Control-Allow-Origin': '*',
    },
  },
  plugins: [
    new MFLiveReloadPlugin({
      port: 8084,
      container: 'data-product-mfe',
      standalone: false,
    }),
  ],
};

module.exports = merge(base, devConfig);
