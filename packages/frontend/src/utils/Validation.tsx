export function validateMobileNumber(val: string) {
  // Only + and number should be allowed
  const str = /^\+[0-9 ]*$/; // eslint-disable-line
  const match = str.test(val) || !val ? true : false;
  if (!match) {
    return false;
  } else {
    return true;
  }
}

export function validateEmail(email: string) {
  return String(email)
    .toLowerCase()
    .match(
      /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/,
    );
}
