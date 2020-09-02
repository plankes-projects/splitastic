<template>
  <div>
    <img class="userImage" :src="user.image.url" alt="Image" />
    <div class="name">{{user.name}}</div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from "vue-property-decorator";
import { User } from "@/generated/api-axios";

@Component
export default class CompactUser extends Vue {
  @Prop()
  private user!: User;
}
</script>

<style scoped lang="scss">
.userImage {
  border: solid gray 0.1em;
  border-radius: 50%;
  object-fit: cover;
  height: 3em;
  width: 3em;
}

.userListEntry {
  display: inline-block;
  width: 6em;
}
.name {
  display: inline-block;
  width: 90%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: black;
}
</style>
