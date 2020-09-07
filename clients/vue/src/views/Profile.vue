<template>
  <div class>
    <b-loading
      v-if="loading"
      :is-full-page="true"
      :active="loading"
    ></b-loading>
    <template v-if="atLeastOneRefreshDone">
      <div class="profileData">
        <b-upload v-model="newProfileImage" accept="image/*">
          <img class="profileImage" :src="imageUrl" alt="Image" />
        </b-upload>
        <br />
        <p>Tap image to change</p>
        <span v-if="newProfileImage" class="tag is-primary">
          <button
            class="removeNewFile delete is-small"
            type="button"
            @click="removeNewFile()"
          ></button>
          Change to: {{ newProfileImage.name }}
        </span>
        <b-field label="Email">
          <b-input
            type="email"
            icon="envelope"
            v-model="user.email"
            readonly
          ></b-input>
        </b-field>
        <b-field label="Name">
          <b-input v-model="user.name"></b-input>
        </b-field>
        <section id="saveButton">
          <b-button v-if="!savingUserDetails" @click="saveUserDetails"
            >SAVE</b-button
          >
          <b-icon v-else icon="sync" type="is-success" class="fa-spin"></b-icon>
        </section>
      </div>
      <b-button @click="logoutOnAll()" class="logoutButton" type="is-danger"
        >Logout on ALL devices</b-button
      >
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { UserApi, User } from "@/generated/api-axios";
import config from "@/../config";
import { LogoutHelper } from "@/untils/LogoutHelper";
import { FileUtils } from "@/untils/FileUtils";
import { StateUtils } from "@/untils/StateUtils";

@Component
export default class Profile extends Vue {
  private loading = true;
  private user?: User;
  private savingUserDetails = false;
  private newProfileImage: File | null = null;
  private imageUrl = "";
  private atLeastOneRefreshDone = false;

  private removeNewFile() {
    this.newProfileImage = null;
  }

  private async logoutOnAll() {
    const api = new UserApi({
      basePath: config.basePath,
      apiKey: StateUtils.getApiKey(),
    });

    await api.userResetApiKeyPut();
    LogoutHelper.logout(this);
  }

  private async saveUserDetails() {
    this.savingUserDetails = true;
    const api = new UserApi({
      basePath: config.basePath,
      apiKey: StateUtils.getApiKey(),
    });

    if (this.newProfileImage) {
      this.user!.image = {
        url: await FileUtils.loadToDataURL(this.newProfileImage),
      };
    } else {
      this.user!.image = undefined;
    }

    await api.userPut(this.user!);

    this.$toast.success(`Successfully updated`);

    this.refresh();

    this.savingUserDetails = false;
  }

  private async refresh() {
    this.loading = true;
    this.newProfileImage = null;
    const api = new UserApi({
      basePath: config.basePath,
      apiKey: StateUtils.getApiKey(),
    });
    this.user = (await api.userGet()).data;
    this.imageUrl = this.user!.image!.url!;
    this.loading = false;
    this.atLeastOneRefreshDone = true;
  }

  private async mounted() {
    return this.refresh();
  }
}
</script>

<style scoped lang="scss">
.profileImage {
  border-radius: 50%;
  object-fit: cover;
  height: 6em;
  width: 6em;
}

.logoutButton {
  margin: 2em;
}
.removeNewFile {
  margin: 0 !important;
}

.profileData {
  background-color: $transbarentBackground;
  padding: $formDataPadding;
  margin: 2em;
}
</style>
