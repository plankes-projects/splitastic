<template>
  <div>
    <div v-if="checkingToken">
      Checking link...
    </div>
    <div v-else>
      <p>This link is not valid anymore :(</p> 
        <router-link v-if="hasApiKey" class="button is-primary" :to="{ name: homeRoute }">HOME</router-link>
        <router-link v-else class="button is-primary" :to="{ name: loginRoute }">LOGIN</router-link>
    </div>
  </div>
</template>

<script lang="ts">
import { AutenticationApi, Configuration } from "@/generated/api-axios";
import { Component, Vue } from "vue-property-decorator";
import config from "@/../config";
import { StateUtils } from "@/untils/StateUtils";
import { RouterNames } from "@/untils/RouterNames";

@Component
export default class Login extends Vue {
  
  private checkingToken = true;
  private hasApiKey = false;
  private loginRoute = RouterNames.LOGIN;
  private homeRoute = RouterNames.HOME;

  private mounted() {
    this.hasApiKey = StateUtils.hasApiKey();
    this.checkLink();
  }

  public async checkLink(): Promise<void> {
    const autenticationApi = new AutenticationApi({basePath: config.basePath});

    try {
      const token = this.$route.query.token.toString();
      const result = await autenticationApi.loginPut(token);
      StateUtils.setApiKey(result.data.token!);
      localStorage.userId = result.data.userId;
      console.log("Link OK!");
      this.loginSuccess();
    } catch (error) {
      if (!error.response || error.response.status === 404) {
        console.log("Link not valid anymore!");
        this.checkingToken = false;
      } else {
        throw error;
      }
    }
  }
  
  private loginSuccess() {
    localStorage.removeItem("loginData");
    this.$router.push({ name: RouterNames.HOME });
  }
}
</script>

<style scoped lang="scss">

</style>
