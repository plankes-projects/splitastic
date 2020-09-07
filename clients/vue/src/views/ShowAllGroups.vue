<template>
  <div class="section">
    <b-loading
      v-if="loading"
      :is-full-page="true"
      :active="loading"
    ></b-loading>
    <template v-if="atLeastOneRefreshDone">
      <div v-if="invites.length != 0" class="panel">
        <div class="margin0 panel-heading level is-mobile">
          <p class="level-left level-item">Open Invites</p>
        </div>

        <table class="inviteTable">
          <tr
            class="margin0 panel-block level is-mobile"
            v-for="invite in this.invites"
            :key="invite.id"
          >
            <td>{{ invite.group.name }}</td>
            <td class="level-right level-item">
              <a @click="acceptInviteWithConfirm(invite.group.id)">
                <b-icon icon="check" size="is-small" type="is-success"></b-icon>
              </a>
              <a
                class="deleteInvite"
                @click="deleteInviteWithInvite(invite.id)"
              >
                <b-icon icon="trash" size="is-small" type="is-danger"></b-icon>
              </a>
            </td>
          </tr>
        </table>
      </div>

      <template v-if="userHasGroups">
        <div class="panel">
          <p class="panel-heading">Your groups:</p>
          <div
            class="groupCardContainer"
            v-for="group in groups"
            :key="group.id"
          >
            <GroupCard :group="group" :myUserId="user.id" />
          </div>
        </div>
      </template>
      <template v-else
        >You are in no groups yet. Use the menu to create a new group.</template
      >
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import config from "@/../config";
import { Configuration } from "@/generated/api-axios/configuration";
import {
  UserApi,
  User,
  GroupApi,
  Group,
  Invite,
} from "@/generated/api-axios/api";
import GroupCard from "@/components/GroupCard.vue";
import { StateUtils } from "@/untils/StateUtils";

@Component({
  components: {
    GroupCard,
  },
})
export default class ShowAllGroups extends Vue {
  private user!: User;
  private groups!: Group[];
  private invites!: Invite[];
  private userHasGroups = false;
  private loading = true;
  private atLeastOneRefreshDone = false;

  private async deleteInviteWithInvite(inviteId: number) {
    this.$buefy.dialog.confirm({
      title: "Delete Invite",
      message: "Are you sure you want to <b>delete</b> this invite?",
      confirmText: "Delete Invite",
      type: "is-danger",
      hasIcon: true,
      onConfirm: () => this.deleteInvite(inviteId),
    });
  }

  private async deleteInvite(inviteId: number) {
    const api = new GroupApi({
      basePath: config.basePath,
      apiKey: StateUtils.getApiKey(),
    });

    try {
      this.loading = true;
      await api.groupInviteInviteIdDelete(inviteId);
      this.refresh();
    } catch (e) {
      this.loading = false;
      this.$toast.error(`Delete failed`);
    }
  }
  private async acceptInviteWithConfirm(groupId: number) {
    this.$buefy.dialog.confirm({
      title: "Accepting Invite",
      message: "Are you sure you want to <b>accept</b> this invite?",
      confirmText: "Accept Invite",
      type: "is-success",
      hasIcon: true,
      onConfirm: () => this.acceptInvite(groupId),
    });
  }

  private async acceptInvite(groupId: number) {
    const userApi = new UserApi({
      basePath: config.basePath,
      apiKey: StateUtils.getApiKey(),
    });

    try {
      this.loading = true;
      await userApi.groupGroupIdJoinPut(groupId);
      this.refresh();
    } catch (e) {
      this.loading = false;
      this.$toast.error(`Join failed`);
    }
  }

  private async refresh() {
    this.loading = true;
    const apiConfig: Configuration = {
      basePath: config.basePath,
      apiKey: StateUtils.getApiKey(),
    };
    const userApi = new UserApi(apiConfig);
    const groupApi = new GroupApi(apiConfig);
    const userPromise = userApi.userGet();
    const groupsPromise = groupApi.groupGet(50);
    const invitesPromise = userApi.userInviteGet();

    this.user = (await userPromise).data;
    this.groups = (await groupsPromise).data;
    this.invites = (await invitesPromise).data;

    this.userHasGroups = this.groups.length != 0;
    this.loading = false;
    this.atLeastOneRefreshDone = true;
  }

  private async mounted() {
    return this.refresh();
  }
}
</script>

<style scoped>
.margin0 {
  margin: 0;
}

.deleteInvite {
  padding-left: 0.5em;
}

.panel:not(:last-child) {
  margin-bottom: 1rem;
}

.section {
  padding: 1em;
}

.inviteTable {
  width: 100%;
}
</style>
