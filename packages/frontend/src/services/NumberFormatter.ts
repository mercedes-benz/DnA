export function parseFormattedString(lang: string, input: string): number {
  const formater = new Intl.NumberFormat(lang);
  const thousandSeparator = formater.format(1111).replace(/1/g, '');
  const unformattedString = input.replace(new RegExp('\\' + thousandSeparator, 'g'), '');
  const decimalSeparator = formater.format(1.1).replace(/1/g, '');
  const numberString = unformattedString.replace(new RegExp('\\' + decimalSeparator, 'g'), '.');
  return Number(numberString);
}

export function formatNumber(lang: string, input: number) {
  return new Intl.NumberFormat(lang).format(input);
}
