<template>
  <ShowEditGroup :readOnly="false" />
</template>


<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import ShowEditGroup from "@/components/ShowEditGroup.vue";

@Component({
  components: {
    ShowEditGroup
  }
})
export default class EditGroup extends Vue {}
</script>


<style scoped>
</style>