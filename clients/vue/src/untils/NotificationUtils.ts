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
      try {
        const notification = new Notification(
          payload.notification.title,
          payload.notification
        );
        notification.onclick = function(event: any) {
          console.log("push foreground clicked: ", event);
          notification.close();
          vue.$router.go(payload.data.url);
        };
      } catch (err) {
        //todo: how to add on click listener?
        this.showMessage(payload.notification); //android fallback
      }
    });

    window.addEventListener(
      "notificationclick",
      function(event) {
        event.stopPropagation();
        console.log("notificationclick intercepted!");
      },
      true
    );

    this.refreshFirebaseToken();
    messaging.onTokenRefresh(() => {
      return this.refreshFirebaseToken();
    });
  }

  public static showMessage(notification: any) {
    if (Notification.permission == "granted") {
      navigator.serviceWorker.ready.then((registration) => {
        registration.showNotification(notification.title, notification);
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
