<template>
  <div class="section">
    <div>Are you sure?</div>
    <b-button @click="createGroup">Create Group</b-button>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import config from "@/../config";
import { RouterNames } from "@/untils/RouterNames";
import { GroupApi, Configuration } from "@/generated/api-axios";
import { StateUtils } from "@/untils/StateUtils";

@Component
export default class CreateGroup extends Vue {
  private groupApi!: GroupApi;
  public async createGroup() {
    const apiConfig: Configuration = {
      basePath: config.basePath,
      apiKey: StateUtils.getApiKey()
    };
    const groupApi = new GroupApi(apiConfig);
    const groupId = (await groupApi.groupPost()).data;

    this.$router.push({
      name: RouterNames.EDIT_GROUP,
      params: { groupId: groupId.toString() }
    });
  }
}
</script>
