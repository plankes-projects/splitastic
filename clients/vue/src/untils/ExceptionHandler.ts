import { RouterNames } from "./RouterNames";
import { LogoutHelper } from "./LogoutHelper";
import { StateUtils } from "./StateUtils";

export class ExceptionHandler {
  public static handle(error: any, vm: Vue) {
    if (error.response.status == 401) {
      LogoutHelper.logout(vm);
    } else if (error.message == "Network Error") {
      vm.$router.push({
        name: RouterNames.ERROR,
        params: {
          msg:
            "API server is not reachable at the moment. Please try again later or check your internet connection.",
        },
      });
    } else if (error.response.status == 500) {
      vm.$router.push({
        name: RouterNames.ERROR,
        params: {
          msg: "Internal Server Error...",
        },
      });
    } else {
      StateUtils.unsetActiveGroupId();
      vm.$router.push({ name: RouterNames.HOME });
    }
  }
}
