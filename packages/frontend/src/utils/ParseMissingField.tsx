export function parseMessage(n: string): string {
    const newArr = n.split('.').join(',').split(',');
    const newMessage = newArr[1] + ' in ' + newArr[0] + ' tab ' + newArr[2];
    return newMessage;
}