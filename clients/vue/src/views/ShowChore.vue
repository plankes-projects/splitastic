<template>
  <ShowEditChore :readOnly="true" />
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import ShowEditChore from "@/components/ShowEditChore.vue";

@Component({
  components: {
    ShowEditChore
  }
})
export default class ShowChore extends Vue {}
</script>

<style scoped lang="scss">
</style>