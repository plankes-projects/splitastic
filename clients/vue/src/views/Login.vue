<template>
  <div class="page-wrapper">
    <div class="imageContainer">
      <img src="/img/logo-menu.png" alt="Split your work and expenses!" />
    </div>
    <div class="content-wrapper section">
      <div class="card cardClass">
        <section v-if="insertEmailView">
          <b-field class="email" label="Email">
            <b-input
              @keyup.native.enter="sendLogInEmail"
              type="email"
              icon="envelope"
              placeholder="Email"
              v-model="email"
            ></b-input>
          </b-field>
          <b-button v-if="!sendingMail" @click="sendLogInEmail">Login</b-button>
          <b-icon v-else icon="sync" type="is-success" class="fa-spin"></b-icon>
        </section>
        <section v-else>
          <div>
            Click the link we send to
            <b>{{ email }}</b>
          </div>
          <div>
            <b-icon icon="sync" size="is-large" type="is-success" class="fa-spin"></b-icon>
          </div>
          <div>
            Verify code:
            <b>{{ loginData.verify }}</b>
          </div>
          <div>This email may take up to 10 minutes to arrive.</div>
          <b-button @click="userOtherEmailCLicked">Use other email</b-button>
        </section>
      </div>
    </div>
    <footer class="footer">
      <div class="content has-text-centered">
        <p>
          Welcome to
          <strong>Splitastic</strong>!
          <br />An app to split your chores and expenses.
          <br />
          <br />
          <router-link :to="{ name: aboutRoute }">About</router-link>
        </p>
      </div>
    </footer>
  </div>
</template>

<script lang="ts">
//&#169; Splitastic {{ new Date().getFullYear() }} -
import { Component, Vue } from "vue-property-decorator";
import config from "@/../config";
import { Configuration } from "@/generated/api-axios/configuration";
import {
  AutenticationApi,
  GeneralApi,
  LoginData
} from "@/generated/api-axios/api";
import { RouterNames } from "@/untils/RouterNames";

@Component
export default class Login extends Vue {
  private aboutRoute = RouterNames.ABOUT;
  private email = "";
  private autenticationApi!: AutenticationApi;
  private generalApi!: GeneralApi;
  private loginData!: LoginData;
  private insertEmailView = true;
  private sendingMail = false;

  public async sendLogInEmail(): Promise<void> {
    this.sendingMail = true;
    let option = {};
    if (localStorage.getItem("admin.token")) {
      option = {
        headers: { "X-ADMIN-TOKEN": localStorage.getItem("admin.token") }
      };
    }
    this.loginData = (
      await this.autenticationApi.loginPost(this.email, option)
    ).data;
    this.insertEmailView = false;

    localStorage.loginData = JSON.stringify(this.loginData);
    this.checkLinkClicked();
    this.sendingMail = false;
  }

  public async userOtherEmailCLicked(): Promise<void> {
    localStorage.removeItem("loginData");
    this.insertEmailView = true;
  }

  public async checkLinkClicked(): Promise<void> {
    console.log("Check link clicked! " + this.loginData.verify);
    if (this.insertEmailView) {
      return;
    }
    try {
      const result = await this.autenticationApi.loginPut(
        this.loginData.token ?? ""
      );
      localStorage.apiKey = result.data.token;
      localStorage.userId = result.data.userId;
      this.insertEmailView = true;
      this.loginSuccess();
    } catch (error) {
      if (error.response.status === 404) {
        if (this.insertEmailView == false) {
          setTimeout(() => {
            this.checkLinkClicked();
          }, 5000);
        }
      } else {
        throw error;
      }
    }
  }

  private loginSuccess() {
    localStorage.removeItem("loginData");
    this.$router.push({ name: RouterNames.HOME });
  }

  private mounted() {
    if (localStorage.apiKey) {
      this.loginSuccess();
      return;
    }

    const apiConfig: Configuration = {
      basePath: config.basePath
    };

    this.autenticationApi = new AutenticationApi(apiConfig);
    this.generalApi = new GeneralApi(apiConfig);

    if (localStorage.loginData) {
      this.loginData = JSON.parse(localStorage.loginData);
      this.email = this.loginData.email!;
      this.insertEmailView = false;
      this.checkLinkClicked();
    }
  }
}
</script>

<style scoped lang="scss">
.email {
  max-width: 20em;
  margin-left: auto;
  margin-right: auto;
}
.page-wrapper {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
}

.content-wrapper {
  flex: 1;
  padding-top: 0;
}

.footer {
  padding: 1em;
}
.imageContainer {
  padding: 3em;
}

.cardClass {
  padding: 1em;
  max-width: 25em;
  margin: 0 auto;
  background-color: #fafafa;
}
</style>
