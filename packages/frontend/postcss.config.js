const postcssPresetEnv = require('postcss-preset-env');
const autoprefixer = require('autoprefixer');
const postcssPluginSorting = require('postcss-sorting');
const postcssNested = require('postcss-nested');
const postcssImport = require('postcss-import');
const postcssExtend = require('postcss-extend');

const cssCustomProperties = require('./.build/postcss/cssCustomProperties');
const customMediaQueries = require('./.build/postcss/customMediaQueries');

module.exports = (cfg) => {
  return {
    plugins: [
      postcssImport(),
      autoprefixer(),
      postcssNested(),
      postcssExtend(),
      (process.env.build === 'dev' || process.env.build === 'prod' || process.env.build === 'multi') &&
        postcssPresetEnv({
          browsers: '> 1%, last 2 versions, Firefox ESR, ie >= 11',
          stage: 3,
          features: {
            'custom-properties': {
              appendVariables: true,
              strict: false,
              warnings: true,
              noValueNotifications: 'error',
              variables: cssCustomProperties,
            },
            'custom-media-queries': {
              extensions: customMediaQueries,
            },
          },
        }),
      postcssPluginSorting({
        order: ['custom-properties', 'dollar-variables', 'declarations', 'at-rules', 'rules'],
        'properties-order': 'alphabetical',
        'unspecified-properties-position': 'bottom',
      }),
    ],
  };
};
