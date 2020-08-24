import { RouterNames } from "./RouterNames";
import { StateUtils } from "./StateUtils";

export class LogoutHelper {
  public static logout(vm: Vue) {
    localStorage.removeItem("apiKey");
    StateUtils.unsetActiveGroupId();
    vm.$router.push({ name: RouterNames.LOGIN });
  }
}
