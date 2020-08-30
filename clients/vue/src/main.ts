import Vue from "vue";
import App from "./App.vue";
import "./registerServiceWorker";
import router from "./router";
import store from "./store";

import Buefy from "buefy";
import "buefy/dist/buefy.css";

import { library } from "@fortawesome/fontawesome-svg-core";
import { fas } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { ExceptionHandler } from "./untils/ExceptionHandler";

//##########################################
//setup vue
library.add(fas);
Vue.component("font-awesome-icon", FontAwesomeIcon);
Vue.use(Buefy, { defaultIconPack: "fas" });

Vue.config.errorHandler = function(err, vm, info) {
  ExceptionHandler.handle(err, vm);
};

Vue.config.productionTip = false;
new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount("#app");

//##########################################
//setup push notification
import firebase from "firebase/app";
import "firebase/messaging";
import { StateUtils } from "./untils/StateUtils";
import config from "@/../config";

firebase.initializeApp(config.firebase.config);

const messaging = firebase.messaging();

messaging.usePublicVapidKey(config.firebase.publicVapidKey);

messaging.onMessage((notification) => {
  if (Notification.permission == "granted") {
    return navigator.serviceWorker.ready.then((registration) => {
      return registration.showNotification(
        notification.notification.title,
        notification.notification
      );
    });
  }
});

// Request Permission of Notifications
messaging
  .getToken()
  .then((token) => {
    StateUtils.setFireBaseToken(token!);
    console.log("Token: " + token);
  })
  .catch((err) => {
    console.log("Unable to get permission to notify.", err);
    StateUtils.unsetFireBaseToken();
  });
