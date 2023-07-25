export const markdownParser = (markdownText: string) => {
  const htmlText = markdownText
    .replace(/\[(.*?)\]\((.*?)\)/gi, "<a href='$2' target='_blank' rel='noopener noreferrer'>$1</a>")
    .replace(/\*\*(.*)\*\*/gi, '<strong>$1</strong>')
    .replace(/__(.*)__/gi, '<strong>$1</strong>')
    .replace(/_(.*)_/gi, '<em>$1</em>')
    .replace(/\*(.*)\*/gi, '<em>$1</em>')
    .replace(/\n/gi, '<br />');
  return htmlText.trim();
};

export const htmlToMarkdownParser = (htmlText: string) =>{
  const markdownText = htmlText.replace(/<a.*?href="(.*?)".*?>(.*?)<\/a>/gi, '[$2]($1)');
  return markdownText.trim();
}
