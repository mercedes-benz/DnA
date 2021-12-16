export function formatCurrency(n: number): string {
  if (n < 0) {
    return '';
  }

  const currencyFormatWithoutSymbol = new Intl.NumberFormat(navigator.language, {
    style: 'decimal',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
  return currencyFormatWithoutSymbol.format(n);
}

export function formatCurrencyForChart(n: number): string {
  const currencyFormat = new Intl.NumberFormat(navigator.language, {
    style: 'decimal',
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  });

  let euroNumber = n / 1_000_000;
  let symbol = 'm/EUR';
  if (euroNumber < 1) {
    euroNumber = n / 1_000_000;
    symbol = 'k/EUR';
  }
  return currencyFormat.format(euroNumber) + ' ' + symbol;
}

export function isCurrencyInputValid(input?: string): number | undefined {
  if (!input) {
    return undefined;
  } else {
    const formater = new Intl.NumberFormat();
    const thousandSeparator = formater.format(1111).replace(/1/g, '');
    let formattedInput = input.replace(new RegExp('\\' + thousandSeparator, 'g'), '');
    const decimalSeparator = formater.format(1.1).replace(/1/g, '');
    formattedInput = formattedInput.replace(new RegExp('\\' + decimalSeparator, 'g'), '.');

    const n = parseFloat(formattedInput);
    if (isNaN(n)) {
      return undefined;
    } else {
      return n;
    }
  }
}
