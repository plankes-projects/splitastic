<template>
  <ShowEditGroup :readOnly="true" />
</template>


<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { Group, GroupApi, Invite, UserApi } from "@/generated/api-axios";
import config from "@/../config";
import { RouterNames } from "@/untils/RouterNames";
import { FileUtils } from "@/untils/FileUtils";
import ShowEditGroup from "@/components/ShowEditGroup.vue";

@Component({
  components: {
    ShowEditGroup
  }
})
export default class ShowGroup extends Vue {}
</script>


<style scoped>
</style>