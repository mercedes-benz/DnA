const path = require('path'),
  packageJson = require(path.resolve(process.cwd(), 'package.json')),
  packageName = packageJson.name.replace(/@/, '').replace(/\//, '-'),
  version = packageJson.version.toLowerCase().trim(),
  devMode = process.env.build === 'dev';

const legacyConfig = {
  name: 'LegacyConfig',
  entry: {
    app: ['webcrypto-shim', '@babel/polyfill', './src/index.tsx'],
  },
  output: {},
  module: {
    rules: [
      {
        test: /\.js$/,
        use: [
          {
            loader: 'babel-loader',
            options: {
              presets: [
                [
                  '@babel/preset-env',
                  {
                    modules: false,
                    useBuiltIns: 'entry',
                    // corejs: '3.11.0',
                    targets: {
                      browsers: ['> 1%', 'last 2 versions', 'Firefox ESR', 'ie >= 11'],
                    },
                  },
                ],
              ],
            },
          },
        ],
      },
    ],
  },
};

// in devmode chunkhash throws error
if (!devMode) {
  legacyConfig.output.filename = `${packageName}_${version}_[name]_legacy.[fullhash].js`;
  legacyConfig.output.chunkFilename = `${packageName}_${version}_[name].[fullhash].bundle.js`;
}

module.exports = legacyConfig;
