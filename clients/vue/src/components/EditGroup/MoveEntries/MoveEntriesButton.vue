<template>
  <div>
    <b-button @click="isModalActive = true" type="is-primary">Move all user entries</b-button>
    <b-modal :active.sync="isModalActive">
      <MoveView @move-finished="isModalActive = false" :group="group"></MoveView>
    </b-modal>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from "vue-property-decorator";
import { Group } from "@/generated/api-axios";
import MoveView from "@/components/EditGroup/MoveEntries/MoveView.vue";

@Component({
  components: {
    MoveView
  }
})
export default class MoveEntriesButton extends Vue {
  @Prop()
  private group!: Group;

  private isModalActive = false;
}
</script>

<style scoped lang="scss">
</style>
