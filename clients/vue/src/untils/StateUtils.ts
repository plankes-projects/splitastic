import { RouterNames } from "./RouterNames";

export class StateUtils {
  public static setActiveGroupId(groupId: number) {
    localStorage.lastFinanceGroupId = groupId;
  }

  public static getActiveGroupId(): number {
    return localStorage.lastFinanceGroupId;
  }

  public static hasActiveGroupId(): boolean {
    return localStorage.lastFinanceGroupId != undefined;
  }

  public static unsetActiveGroupId() {
    localStorage.removeItem("lastFinanceGroupId");
  }

  public static setLastActiveDefaultRouteName(routeName: string) {
    localStorage.lastActiveDefaultRouteName = routeName;
  }
  public static getLastActiveDefaultRouteName(): string {
    return localStorage.lastActiveDefaultRouteName;
  }
  public static hasLastActiveDefaultRouteName(): boolean {
    return localStorage.lastActiveDefaultRouteName != undefined;
  }
  public static unsetLastActiveDefaultRouteName() {
    localStorage.removeItem("lastActiveDefaultRouteName");
  }

  public static setDeviceId(deviceId: string) {
    localStorage.deviceId = deviceId;
  }
  public static getDeviceId(): string {
    return localStorage.deviceId;
  }
}
