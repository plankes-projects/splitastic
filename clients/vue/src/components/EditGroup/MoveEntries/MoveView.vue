<template>
  <b-loading v-if="loading" :is-full-page="true" :active="loading"></b-loading>
  <div v-else-if="ready" class="card container">
    <div class="mySection">
      <div class="header">
        Used this to replace a user in the group.
        <br />(eg.: virtual by real user)
        <br />
        <b>Steps:</b>
        <br />Invite new user into the group
        <br />Move data from old to new
        <br />Remove old user from the group
      </div>

      <div class="header">
        <b>Select the users</b>
      </div>
      <div>
        <UserChooseButton class="inline-block" :group="group" :userId.sync="userIdFrom"></UserChooseButton>
        <b-icon class="arrow" icon="arrow-right" size="is-large"></b-icon>
        <UserChooseButton class="inline-block" :group="group" :userId.sync="userIdTo"></UserChooseButton>
      </div>
    </div>
    <div class="mySection">
      <div class="header">
        <b>Select which data</b>
      </div>
      <div>
        <div class="inline-block">
          <b-checkbox v-model="finance">Finance</b-checkbox>
        </div>
        <div class="inline-block">
          <b-checkbox v-model="chore">Chore</b-checkbox>
        </div>
      </div>
    </div>

    <div>
      <b-button size="is-medium" icon-left="check" @click="applyWithConfirm">Apply</b-button>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from "vue-property-decorator";
import UserChooseButton from "@/components/EditGroup/MoveEntries/UserChooseButton.vue";
import { Group, GroupApi } from "@/generated/api-axios";
import config from "@/../config";
import { StateUtils } from "@/untils/StateUtils";

@Component({
  components: {
    UserChooseButton
  }
})
export default class MoveView extends Vue {
  @Prop()
  private group!: Group;
  private ready = false;
  private loading = false;

  private userIdFrom = 0;
  private userIdTo = 0;

  private finance = false;
  private chore = false;

  private applyWithConfirm() {
    if (this.userIdFrom == this.userIdTo) {
      this.$buefy.toast.open({
        duration: 1000,
        message: `Need different users`,
        type: "is-danger"
      });
      return;
    }
    if (!this.finance && !this.chore) {
      this.$buefy.toast.open({
        duration: 1000,
        message: `Need at least one data select`,
        type: "is-danger"
      });
      return;
    }

    const which = this.buildWhichString();
    this.$buefy.dialog.confirm({
      title: "Move " + which,
      message:
        "Are you sure you want to do this? This action cannot be undone.",
      confirmText: "Move",
      //type: "is-warning",
      hasIcon: true,
      icon: "exclamation-triangle",
      onConfirm: () => this.apply()
    });
  }

  private buildWhichString(): string {
    if (this.finance && this.chore) {
      return "finance and chores";
    }

    if (this.finance) {
      return "finance";
    }

    return "chores";
  }

  private async apply() {
    this.loading = true;

    const groupApi = new GroupApi({
      basePath: config.basePath,
      apiKey: StateUtils.getApiKey()
    });
    try {
      await groupApi.groupGroupIdMoveUserDataPut(this.group.id!, {
        fromUserId: this.userIdFrom,
        toUserId: this.userIdTo,
        chores: this.chore,
        finance: this.finance
      });
      this.loading = false;
      this.$buefy.toast.open({
        duration: 1000,
        message: `Data successfully moved`,
        type: "is-success"
      });
      this.$emit("move-finished");
    } catch (e) {
      this.loading = false;
      this.$buefy.toast.open({
        duration: 1000,
        message: `Something failed`,
        type: "is-danger"
      });
    }
  }

  private mounted() {
    this.userIdFrom = this.group.users![0].id!;
    this.userIdTo = this.group.users![0].id!;
    this.ready = true;
  }
}
</script>

<style scoped lang="scss">
.inline-block {
  display: inline-block;
}

.arrow {
  display: inline-block;
  top: -1.5em;
  position: relative;
}

.container {
  padding-top: 1em;
  padding-bottom: 1em;
  padding-left: 0;
  padding-right: 0;
}
.mySection {
  padding-bottom: 2em;
}
.header {
  padding-bottom: 1em;
}
</style>
