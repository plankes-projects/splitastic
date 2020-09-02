import { Group, User } from "@/generated/api-axios";

export class GroupUtils {
  public static getUserFromGroup(group: Group, userId: number): User {
    for (const user of group.users!) {
      if (user.id == userId) {
        return user;
      }
    }

    throw Error("Could not find user with user id " + userId);
  }
}
