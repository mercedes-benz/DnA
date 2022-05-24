const base = require('./base');
const { merge } = require('webpack-merge');
const path = require('path');
const terserPlugin = require('terser-webpack-plugin');
const optimizeCSSAssetsPlugin = require('css-minimizer-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const prodMode = process.env.build === 'prod';
const packageJson = require(path.resolve(process.cwd(), 'package.json'));
const htmlWebpackMultiBuildPlugin = require('html-webpack-multi-build-plugin');
const ESLintPlugin = require('eslint-webpack-plugin');
const webpack = require('webpack');

const { ModuleFederationPlugin } = webpack.container;
const MFE_URL = process.env.ENV_FILE ? '${PROJECTSMO_STORAGE_MFE_APP_URL}' : 'http://localhost:8083';

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
    new ESLintPlugin({
      extensions: ['tsx'],
      fix: true,
    }),
    new ModuleFederationPlugin({
      name: 'dna_container',
      filename: 'remoteEntry.js',
      exposes: {
        './Progress': './src/components/progress/Progress.tsx',
        './InfoModal': './src/components/formElements/modal/infoModal/InfoModal.tsx',
        './Modal': './src/components/formElements/modal/Modal.tsx',
        './ConfirmModal': './src/components/formElements/modal/confirmModal/ConfirmModal.tsx',
        './Pagination': './src/components/mbc/pagination/Pagination.tsx',
        './Header': './src/components/header/Header.tsx',
        './MainNavigation': './src/components/mainNavigation/MainNavigation.tsx',
        './Footer': './src/components/mbc/footer/Footer.tsx',
        './NotFound': './src/router/NotFoundPage.tsx',
        './UnAuthorised': './src/router/UnAuthorised.tsx',
        './AddUser': './src/components/mbc/addUser/AddUser.tsx',
        './SelectBox': './src/components/formElements/SelectBox/SelectBox.ts',
      },
      remotes: {
        'storage-mfe': `storage_mfe@${MFE_URL}/remoteEntry.js`,
      },
      shared: {
        ...packageJson.dependencies,
        react: { singleton: true, eager: true, requiredVersion: packageJson.dependencies.react },
        'react-dom': {
          singleton: true,
          eager: true,
          requiredVersion: packageJson.dependencies['react-dom'],
        },
        'react-router-dom': {
          singleton: true,
          eager: true,
          requiredVersion: packageJson.dependencies['react-router-dom'],
        },
      },
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
    concatenateModules: true,
  },
  stats: process.env.verbose === 'true' ? 'normal' : 'errors-only',
};

module.exports = merge(base, prodConfig);
