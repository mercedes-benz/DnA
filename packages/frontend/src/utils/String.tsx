import * as React from 'react';

export function splitStringOnNewline(input: string): string[] {
  return input.split(/\r?\n/g);
}

export function linesToParagraph(lines: string) {
  return splitStringOnNewline(lines).map((line, idx) => <p key={idx}>{line}</p>);
}
