<template>
  <div class="section"></div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { RouterNames } from "@/untils/RouterNames";
import { StateUtils } from "@/untils/StateUtils";

@Component
export default class Home extends Vue {
  private mounted() {
    if (
      StateUtils.hasActiveGroupId() &&
      StateUtils.hasLastActiveDefaultRouteName()
    ) {
      const routeName = StateUtils.getLastActiveDefaultRouteName();
      //in case we have an app update and change route names, this will be set again on the view anyways.
      StateUtils.unsetLastActiveDefaultRouteName();
      this.$router.push({
        name: routeName,
        params: {
          groupId: StateUtils.getActiveGroupId().toString()
        }
      });
    } else {
      this.$router.push({ name: RouterNames.SHOW_ALL_GROUPS });
    }
  }
}
</script>
