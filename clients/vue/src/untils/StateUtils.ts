export class StateUtils {
  private static registration: ServiceWorkerRegistration | null = null;

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

  public static setApiKey(apiKey: string) {
    localStorage.apiKey = apiKey;
  }

  public static getApiKey(): string {
    return localStorage.apiKey;
  }
  public static hasApiKey(): boolean {
    return localStorage.apiKey != undefined;
  }
  public static unsetApiKey() {
    localStorage.removeItem("apiKey");
  }
}
