import { RouterNames } from "./RouterNames";
import { StateUtils } from "./StateUtils";

export class LogoutHelper {
  public static logout(vm: Vue) {
    StateUtils.unsetApiKey();
    StateUtils.unsetActiveGroupId();
    vm.$router.push({ name: RouterNames.LOGIN });
  }
}
