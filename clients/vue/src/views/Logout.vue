<template>
  <div class="section">
    <div>Are you sure?</div>
    <b-button @click="logout">Logout</b-button>
  </div>
</template>


<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { LogoutHelper } from "@/untils/LogoutHelper";

@Component
export default class Logout extends Vue {
  public logout() {
    LogoutHelper.logout(this);
  }
}
</script>