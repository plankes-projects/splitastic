<template>
  <div v-if="ready" class="card container">
    <div class="mySection">
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
import { Group, User } from "@/generated/api-axios";

@Component({
  components: {
    UserChooseButton
  }
})
export default class MoveView extends Vue {
  @Prop()
  private group!: Group;
  private ready = false;

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
    console.log("applyWithConfirm");
  }

  private apply() {
    console.log("Apply");
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
  padding-top: 2em;
  padding-bottom: 2em;
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
