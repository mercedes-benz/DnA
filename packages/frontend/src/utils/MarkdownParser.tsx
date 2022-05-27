export const markdownParser = (markdownText: string) => {
  const htmlText = markdownText.replace(
    /\[(.*?)\]\((.*?)\)/gi,
    "<a href='$2' target='_blank' rel='noopener noreferrer'>$1</a>",
  );
  return htmlText.trim();
};
