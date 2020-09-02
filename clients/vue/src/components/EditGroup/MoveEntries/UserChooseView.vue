<template>
  <div class="card container">
    <div class="userList">
      <a @click="userClicked(user.id)" v-for="user in group.users" :key="user.id">
        <CompactUser class="userListEntry" :user="user"></CompactUser>
      </a>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from "vue-property-decorator";
import { User, Group } from "@/generated/api-axios";
import CompactUser from "@/components/EditGroup/MoveEntries/CompactUser.vue";

@Component({
  components: {
    CompactUser
  }
})
export default class UserChooseView extends Vue {
  @Prop()
  private group!: Group;

  private userClicked(userId: number) {
    this.$emit("selected", userId);
  }
}
</script>

<style scoped lang="scss">
.container {
  padding: 1em;
}

.userList {
  display: inline-block;
  background-color: $transbarentBackground;
  width: 100%;
}
</style>
