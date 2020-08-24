<template>
  <ShowEditChore :readOnly="false" />
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import ShowEditChore from "@/components/ShowEditChore.vue";

@Component({
  components: {
    ShowEditChore
  }
})
export default class EditChore extends Vue {}
</script>


<style scoped lang="scss">
</style>