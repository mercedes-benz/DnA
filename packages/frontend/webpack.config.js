module.exports = function(env) {
  process.env = env;
  return require(`./.build/webpack/${env.build}.js`);
};
