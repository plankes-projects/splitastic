<template>
  <div>
    <a @click="userClicked()">
      <CompactUser class="userListEntry" :user="user"></CompactUser>
    </a>

    <b-modal :active.sync="isModalActive">
      <UserChooseView :group="group" @selected="userSelected"></UserChooseView>
    </b-modal>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from "vue-property-decorator";
import { User, Group } from "@/generated/api-axios";
import UserChooseView from "@/components/EditGroup/MoveEntries/UserChooseView.vue";
import CompactUser from "@/components/EditGroup/MoveEntries/CompactUser.vue";
import { GroupUtils } from "@/untils/GroupUtils";

@Component({
  components: {
    UserChooseView,
    CompactUser
  }
})
export default class UserChooseButton extends Vue {
  @Prop()
  private userId!: number;
  @Prop()
  private group!: Group;

  private isModalActive = false;

  get user(): User {
    return GroupUtils.getUserFromGroup(this.group, this.userId);
  }

  private userSelected(userId: number) {
    this.isModalActive = false;
    this.$emit("update:userId", userId);
  }
  private userClicked() {
    this.isModalActive = true;
  }
}
</script>

<style scoped lang="scss">
</style>
