<template>
  <div id="app">
    <b-navbar id="nav" v-if="hasApiKey" :shadow="true">
      <template slot="brand">
        <b-navbar-item id="logoText" tag="router-link" :to="{ name: routeHome }">
          <img src="/img/logo-menu.png" alt="Split your work and expenses!" />
        </b-navbar-item>
      </template>

      <template slot="start">
        <b-navbar-item tag="router-link" :to="{ name: routeShowAllGroups }">All Groups</b-navbar-item>
        <b-navbar-item tag="router-link" :to="{ name: routeCreateGroup }">Create Group</b-navbar-item>
        <b-navbar-item tag="router-link" :to="{ name: routeProfile }">My Profile</b-navbar-item>
        <b-navbar-item tag="router-link" :to="{ name: routeAbout }">About</b-navbar-item>
      </template>

      <template slot="end">
        <b-navbar-item tag="div">
          <div class="buttons">
            <router-link class="button is-primary" :to="{ name: routeLogout }">Logout</router-link>
          </div>
        </b-navbar-item>
      </template>
    </b-navbar>

    <router-view />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from "vue-property-decorator";
import { RouterNames } from "@/untils/RouterNames";
import { StateUtils } from "./untils/StateUtils";

@Component
export default class App extends Vue {
  
  private isActive = false;
  private hasApiKey = true;
  private routeHome = RouterNames.HOME;
  private routeShowAllGroups = RouterNames.SHOW_ALL_GROUPS;
  private routeAbout = RouterNames.ABOUT;
  private routeLogout = RouterNames.LOGOUT;
  private routeProfile = RouterNames.PROFILE;
  private routeCreateGroup = RouterNames.CREATE_GROUP;

  @Watch("$route")
  private routeChanged() {
    this.hasApiKey = StateUtils.hasApiKey();
    if (this.$router.currentRoute.name == RouterNames.ERROR) {
      return;
    }
    if (this.$router.currentRoute.name == RouterNames.ABOUT) {
      return;
    }
    if (this.$router.currentRoute.name == RouterNames.LOGOUT) {
      return;
    }
    if (this.$router.currentRoute.name == RouterNames.LOGIN_WITH_TOKEN) {
      return;
    }

    if (
      !this.hasApiKey &&
      this.$router.currentRoute.name != RouterNames.LOGIN
    ) {
      this.$router.push({ name: RouterNames.LOGIN });
    }
  }

  private mounted() {
    document.title = "Splitastic";
  }
}
</script>

<style lang="scss">
@import "https://use.fontawesome.com/releases/v5.6.3/css/all.css";

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

html {
  background-color: #f3f3f3;
}

#nav {
  a {
    font-weight: bold;

    &.router-link-exact-active {
      color: #42b983;
    }
  }
}

.panel-block {
  background-color: $transbarentBackground;
}

.box {
  background-color: $transbarentBackground !important;
}

html {
  height: 100vh;
  background-image: linear-gradient(to bottom right, #4dba87, #25d7d8);
  background-repeat: no-repeat;
  background-attachment: fixed;
}

.red {
  color: red;
}
.green {
  color: green;
}
</style>
