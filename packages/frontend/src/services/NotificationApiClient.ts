import { Envs } from '../globals/Envs';
import { HTTP_METHOD } from '../globals/constants';
import { ApiClient } from './ApiClient';

const baseUrl = Envs.NOTIFICATIONS_API_BASEURL ? Envs.NOTIFICATIONS_API_BASEURL : `http://${window.location.hostname}:7272/api`;
const getUrl = (endpoint: string) => {
  return `${baseUrl}/${endpoint}`;
};

export class NotificationApiClient {
  public static get(endpoint: string) {
    return ApiClient.fetch(getUrl(endpoint), HTTP_METHOD.GET);
  }
  public static post(endpoint: string, body?: any) {
    return ApiClient.fetch(getUrl(endpoint), HTTP_METHOD.POST, body);
  }
  public static put(endpoint: string, body?: any) {
    return ApiClient.fetch(getUrl(endpoint), HTTP_METHOD.PUT, body);
  }
  public static putWithFormData(endpoint: string, formData: FormData) {
    return ApiClient.fetchWithFormData(getUrl(endpoint), HTTP_METHOD.PUT, formData);
  }
  public static patch(endpoint: string, body?: any) {
    return ApiClient.fetch(getUrl(endpoint), HTTP_METHOD.PATCH, body);
  }
  public static delete(endpoint: string, body?: any) {
    return ApiClient.fetch(getUrl(endpoint), HTTP_METHOD.DELETE, body);
  }

  public static getNotifications(userId: string, limit?: number, offset?: number, readType?: string) {
    let reqParams = `userId=${userId}&`;
    if(readType && readType !== ''){
      reqParams += `readType=${readType}&`;
    }
    if(limit > 0){
      reqParams += `limit=${limit}&`;
    }
    if(offset > -1){
      reqParams += `offset=${offset}&`;
    }
    return this.get(`notifications?`+reqParams);
  }

  public static markAsReadNotifications(notificationIds: string[], userId: string) {
    return this.put(`notifications`, { messageIds: notificationIds, userId });
  }

  public static deleteNotifications(notificationIds: string[], userId: string) {
    return this.delete(`notifications`, { messageIds: notificationIds, userId });
  }

  public static createNotification(message: string, publishingUser: string, serviceUsersType = 'All') {
    return this.post(`notifications`, {
      data: {
        eventType: 'Announcement',
        message,
        publishingUser,
        serviceUsersType,
        subscribedUsers:['']
      }
    })
  }
}
