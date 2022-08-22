const { MFLiveReloadPlugin } = require('@module-federation/fmr');
const base = require('./base.config');
const { merge } = require('webpack-merge');

const devConfig = {
  mode: 'development',
  devtool: 'inline-source-map',
  devServer: {
    port: 8085,
    historyApiFallback: true,
    headers: {
      'Access-Control-Allow-Origin': '*',
    },
  },
  plugins: [
    new MFLiveReloadPlugin({
      port: 8085,
      container: 'chronos-mfe',
      standalone: false,
    }),
  ],
};

module.exports = merge(base, devConfig);
