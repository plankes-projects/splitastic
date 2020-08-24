<template>
  <div class="section">
    <div>
      <b-icon icon="sad-cry" size="is-large"></b-icon>
    </div>
    <p>{{ msg }}</p>

    <b-button @click="back">BACK</b-button>
  </div>
</template>


<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { RouterNames } from "@/untils/RouterNames";

@Component
export default class Error extends Vue {
  private loading = true;
  private msg = "";

  private back() {
    this.$router.back();
  }
  private async mounted() {
    this.msg = this.$route.params.msg;
    if (this.msg == undefined) {
      this.$router.push({ name: RouterNames.HOME });
    }
  }
}
</script>