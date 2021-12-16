module.exports = {
  globals: {
    'ts-jest': {
      tsConfigFile: './packages/app/tsconfig.json',
    },
    __CONFIG__: 'local',
  },
  verbose: true,
  testURL: 'http://localhost/',
  clearMocks: true,
  resetMocks: true,
  collectCoverage: true,
  collectCoverageFrom: ['**/app/src/components/**'],
  coveragePathIgnorePatterns: ['.d.ts'],
  testRegex: '(/test/.*)\\.(test|spec)\\.(ts|tsx|js)$',
  testPathIgnorePatterns: ['/node_modules/', '/dist/', '/coverage/', '/webpack/'],
  transformIgnorePatterns: ['/node_modules/', '/dist/', '/coverage/', '/webpack/'],
  watchPathIgnorePatterns: ['/node_modules/', '/dist/', '/coverage/', '/webpack/'],
  setupFiles: ['./packages/app/.build/jest/enzymeSetup.js'],
  snapshotSerializers: ['enzyme-to-json/serializer'],
  setupTestFrameworkScriptFile: 'mock-local-storage',
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jest'],
  moduleNameMapper: {
    '\\.(css|scss)$': 'identity-obj-proxy',
  },
  transform: {
    '\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$':
      '<rootDir>/packages/app/.build/jest/fileTransformer.js',
    '^.+\\.(ts|tsx)$': 'ts-jest',
  },
};
