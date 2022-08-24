export const encodeParams = (params) => {
  let result = '';
  Object.keys(params).forEach((key) => {
    result += `${key}=${encodeURIComponent(params[key])}&`;
  });
  return result.substring(0, result.length - 1);
};
