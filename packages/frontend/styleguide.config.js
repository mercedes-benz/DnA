/* eslint-disable */
const path = require('path');
const cors = require('cors');
const webpack = require('webpack');

module.exports = {
  components: 'src/components/**/*.tsx',
  propsParser: require('react-docgen-typescript').withCustomConfig('./tsconfig.json').parse,
  skipComponentsWithoutExample: true,
  require: [
    path.join(__dirname, './src/assets/modules/uilab/bundle/css/uilab.min.css'),
    path.join(__dirname, './src/globals/css/main.scss'),
  ],
  ignore: ['src/utils/*.tsx'],
  template: {
    favicon: './src/assets/appIcon/logo.png',
  },
  moduleAliases: {
    'storage-mfe/Bucket': false,
  },
  configureServer(app) {
    // `app` is the instance of the express server running Styleguidist
    app.use(cors());

    app.get('/api/userinfo/:id', (req, res) => {
      res.status(200).send({
        department: 'ITP/IG',
        email: 'demouser@mercedes-benz.com',
        firstName: 'Demo',
        id: 'DEMOUSER',
        lastName: 'User',
        mobileNumber: '',
      });
    });
    app.listen(7171);
  },
  sections: [
    {
      name: 'Introduction',
      content: 'README.md',
    },
    {
      name: 'UI Components',
      components: () => ['src/**/*.tsx'],
      content: 'src/decorators/README.md',
    },
  ],
  theme: {
    color: {
      baseBackground: '#000',
      sidebarBackground: '#000',
      base: '#c0c8d0',
    },
  },
  styles: {
    Para: {
      para: {
        fontSize: 13,
        marginBottom: 0,
      },
    },
    Table: {
      cellHeading: {
        paddingRight: '20px',
      },
    },
    StyleGuide: {
      sidebar: {
        border: '1px solid #697582',
      },
      logo: {
        borderBottom: '1px solid #697582',
      },
    },
    TableOfContents: {
      input: {
        border: '1px solid #697582',
      },
    },
    Playground: {
      preview: {
        border: '1px solid #697582',
      },
    },
    Editor: {
      root: {
        background: '#d9dfe4',
      },
    },
  },
  getExampleFilename(componentPath) {
    return componentPath.replace(/\.tsx?$/, '.md');
  },
  webpackConfig: {
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
                configFile: path.resolve(process.cwd(), 'tsconfig.json'),
              },
            },
          ],
        },
        {
          test: /\.(eot|woff2|woff|svg|otf)/,
          use: [
            {
              loader: 'url-loader',
              options: {
                limit: 1000,
                name: 'fonts/[name]-[contenthash].[ext]',
              },
            },
          ],
        },
        {
          test: /\.(ttf)/,
          use: [
            {
              loader: 'url-loader',
              options: {
                limit: 1000,
                name: 'fonts/[name].[ext]',
              },
            },
          ],
        },
        {
          test: /\.(png|jpg|gif)$/i,
          use: [
            {
              loader: 'url-loader',
              options: {
                limit: 1000,
                name: 'images/[name]-[contenthash].[ext]',
              },
            },
          ],
        },
        // project css
        {
          test: /\.(css|scss)$/,
          use: [
            {
              loader: 'style-loader',
            },
            {
              loader: 'css-loader',
              options: {
                sourceMap: true,
                importLoaders: 1,
                modules: {
                  auto: (resourcePath) => !resourcePath.includes('uilab'),
                  localIdentName: '[name]_[local]_[hash:base64:5]',
                },
              },
            },
            {
              loader: 'sass-loader',
              options: {
                sourceMap: true,
              },
            },
          ],
          include: path.resolve(__dirname, path.join('.', 'src')),
        },
      ],
    },
    resolve: {
      alias: {
        process: 'process/browser',
        stream: 'stream-browserify',
        zlib: 'browserify-zlib',
      },
    },
    plugins: [
      new webpack.ProvidePlugin({
        regeneratorRuntime: 'regenerator-runtime/runtime',
        Promise: ['es6-promise', 'Promise'],
        process: 'process/browser',
        Buffer: ['buffer', 'Buffer'],
      }),
      new webpack.HotModuleReplacementPlugin(),
    ],
  },
};
