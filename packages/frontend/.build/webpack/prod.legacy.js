const prod = require('./prod'),
  { merge } = require('webpack-merge'),
  legacyConfig = require('./legacyConfig');

module.exports = merge(prod, legacyConfig);
