module.exports = {
  env: {
    commonjs: true,
    browser: true,
    node: true,
    es6: true,
  },
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 2020,
    sourceType: 'module',
    ecmaFeatures: {
      jsx: true,
    },
  },
  settings: {
    react: {
      version: 'detect',
    },
  },
  extends: [
    'eslint:recommended',
    'plugin:react/recommended',
    'plugin:@typescript-eslint/recommended',
    'prettier',
    // "eslint-config-prettier/react",
    // "prettier/@typescript-eslint"
  ],
  rules: {
    'prettier/prettier': [
      0,
      {
        singleQuote: true,
        tabWidth: 2,
        trailingComma: 'all',
        parser: 'typescript',
        printWidth: 120,
      },
    ],
    // uncomment below rules in respective order and try to fix the corresponding errors/warning.
    'react/no-deprecated': 0,
    'no-case-declarations': 0,
    'react/no-unescaped-entities': 0,
    'react/no-direct-mutation-state': 0,
    'react/display-name': 0,
    'no-prototype-builtins': 0,
    'no-self-assign': 0,
    '@typescript-eslint/ban-types': 0,
    '@typescript-eslint/no-empty-function': 0,
    '@typescript-eslint/no-unused-vars': 0,
    '@typescript-eslint/ban-ts-comment': 0,
    '@typescript-eslint/no-explicit-any': 0,
    '@typescript-eslint/explicit-module-boundary-types': 0,
  },
};
