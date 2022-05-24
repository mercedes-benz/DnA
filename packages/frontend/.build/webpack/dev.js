const base = require('./base'),
  { merge } = require('webpack-merge'),
  webpack = require('webpack'),
  forkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin'),
  path = require('path'),
  packageJson = require(path.resolve(process.cwd(), 'package.json')),
  legacyMode = process.env.legacy === 'true';
ESLintPlugin = require('eslint-webpack-plugin');

const { ModuleFederationPlugin } = webpack.container;
const MFE_URL = process.env.ENV_FILE ? '${PROJECTSMO_STORAGE_MFE_APP_URL}' : 'http://localhost:8083';

const devConfig = {
  mode: 'development',
  devtool: 'inline-source-map',
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: [
          {
            loader: 'babel-loader',
            options: {
              babelrc: true,
              plugins: ['react-hot-loader/babel'],
            },
          },
          {
            loader: 'ts-loader',
            options: {
              transpileOnly: true,
              configFile: path.resolve(process.cwd(), legacyMode ? 'tsconfig.legacy.json' : 'tsconfig.json'),
            },
          },
        ],
      },
    ],
  },
  plugins: [
    new forkTsCheckerWebpackPlugin({
      typescript: {
        configFile: path.resolve(process.cwd(), legacyMode ? 'tsconfig.legacy.json' : 'tsconfig.json'),
        tslint: false,
      },
    }),
    new webpack.HotModuleReplacementPlugin(),
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
        // object key is used to import
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
  devServer: {
    port: packageJson.config.devServer.port || '9090',
    historyApiFallback: true,
    https: packageJson.config.devServer.https || false,
  },
};

module.exports = merge(base, devConfig);
