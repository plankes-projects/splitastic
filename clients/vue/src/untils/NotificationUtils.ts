import firebase from "firebase/app";
import "firebase/messaging";
import config from "@/../config";
import { UserApi } from "@/generated/api-axios";
import { StateUtils } from "./StateUtils";

export class NotificationUtils {
  public static init(vue: Vue) {
    firebase.initializeApp(config.firebase.config);
    const messaging = firebase.messaging();
    messaging.usePublicVapidKey(config.firebase.publicVapidKey);
    messaging.onMessage((payload) => {
      console.log("push foreground received: ", payload);
      this.showMessage(payload.notification);

      const notification = new Notification(
        payload.notification.title,
        payload.notification
      );
      notification.onclick = function(event: any) {
        console.log("push foreground clicked: ", event);
        notification.close();
        const data = JSON.parse(payload.data.data);
        vue.$router.go(data.url);
      };
    });

    this.refreshFirebaseToken();
    messaging.onTokenRefresh(() => {
      return this.refreshFirebaseToken();
    });

    return navigator.serviceWorker.ready.then((registration) => {
      return registration.addEventListener("notificationclick", function(
        event: any
      ) {
        console.log("foreground notification clicked", event);
        event.stopImmediatePropagation();
        event.notification.close();
        vue.$router.go(event.notification.data.FCM_MSG.data.url);
      });
    });
  }

  public static showMessage(notification: any) {
    if (Notification.permission == "granted") {
      return navigator.serviceWorker.ready.then((registration) => {
        return registration.showNotification(notification.title, notification);
      });
    }
  }

  public static async refreshFirebaseToken() {
    try {
      const messaging = firebase.messaging();
      const token = await messaging.getToken();
      console.log("Token: " + token);
      this.sendTokenToServer(token); //we dont want to have await here
    } catch (err) {
      console.log("Unable to get permission to notify.", err);
    }
  }

  private static async sendTokenToServer(token: string) {
    const api = new UserApi({
      basePath: config.basePath,
      apiKey: StateUtils.getApiKey(),
    });
    await api.userFirebaseTokenPut(token);
  }
}
