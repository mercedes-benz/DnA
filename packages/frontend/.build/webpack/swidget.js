const base = require('./base'),
  { merge } = require('webpack-merge'),
  path = require('path'),
  uglifyJsPlugin = require('uglifyjs-webpack-plugin'),
  packageJson = require(path.resolve(process.cwd(), 'package.json')),
  cleanWebpackPlugin = require('clean-webpack-plugin'),
  nameLibrary = packageJson.name.replace(/@/, '-').replace(/\//, '-'),
  legacyMode = process.env.legacy === 'true',
  SimpleProgressWebpackPlugin = require('simple-progress-webpack-plugin');

const swigdetConfig = {
  mode: 'production',
  output: {
    library: nameLibrary,
    libraryTarget: 'var',
    path: path.resolve(__dirname, '../../dist/swidget'),
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: [
          {
            loader: 'ts-loader',
            options: {
              configFile: legacyMode ? 'tsconfig.prod.legacy.json' : 'tsconfig.prod.json',
            },
          },
        ],
      },
    ],
  },
  plugins: [
    new cleanWebpackPlugin(['dist/swidget'], { root: __dirname + '/../..' }),
    new SimpleProgressWebpackPlugin({
      format: process.env.verbose === 'true' ? 'expanded' : 'compact',
    }),
  ],
  externals: {
    react: 'ExposedReact',
    'react-dom': 'ExposedReactDOM',
    inversify: 'ExposedInversify',
    urijs: 'ExposedUrijs',
    xml2js: 'ExposedXml2js',
    'route-parser': 'ExposedRouteParser',
  },
  optimization: {
    minimizer: [
      new uglifyJsPlugin({
        cache: true,
        parallel: true,
        sourceMap: true,
      }),
    ],
  },
};

module.exports = merge(base, swigdetConfig);
