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
