<template>
  <div class="section">
    <b-loading v-if="loading" :is-full-page="true" :active="loading"></b-loading>
    <div class="profileData">
      <div v-if="imageUrl">
        <b-upload v-model="newProfileImage" accept="image/*">
          <img class="profileImage" :src="imageUrl" alt="Image" />
        </b-upload>
        <br />
        <p>Tap image to change</p>
        <span v-if="newProfileImage" class="tag is-primary">
          <button class="removeNewFile delete is-small" type="button" @click="removeNewFile()"></button>
          Change to: {{newProfileImage.name}}
        </span>
      </div>
      <b-field label="Name">
        <b-input placeholder="Name" v-model="userName"></b-input>
      </b-field>
      <section id="saveButton">
        <b-button @click="saveClicked">SAVE</b-button>
      </section>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from "vue-property-decorator";
import config from "@/../config";
import { GroupApi, User, UserApi } from "@/generated/api-axios";
import { FileUtils } from "@/untils/FileUtils";
import { StateUtils } from "@/untils/StateUtils";

@Component
export default class AddOrEditImaginaryFriend extends Vue {
  @Prop()
  private groupId!: number;
  @Prop()
  private user!: User | null;

  private loading = false;

  private userName = "";
  private imageUrl: string | null = null;
  private newProfileImage: File | null = null;

  private removeNewFile() {
    this.newProfileImage = null;
  }
  private async saveClicked() {
    if (this.userName.length == 0) {
      this.$buefy.toast.open({
        duration: 1000,
        message: `Username shall not be empty`,
        type: "is-danger"
      });
      return;
    }

    try {
      this.loading = true;

      if (this.user) {
        await this.updateUser();
      } else {
        await this.createUser();
      }

      this.$emit("user-created");
    } catch (e) {
      this.loading = false;
      this.$buefy.toast.open({
        duration: 1000,
        message: `Somewthing went wrong =(`,
        type: "is-danger"
      });
    }
    this.loading = false;
  }

  private async createUser() {
    const api = new GroupApi({
      basePath: config.basePath,
      apiKey: StateUtils.getApiKey()
    });
    await api.groupGroupIdAddVirtualUserPost(Number(this.groupId), {
      name: this.userName
    });
  }

  private async updateUser() {
    const api = new UserApi({
      basePath: config.basePath,
      apiKey: StateUtils.getApiKey()
    });

    const userToUpdate = Object.assign({}, this.user!);
    userToUpdate.name = this.userName;

    if (this.newProfileImage) {
      userToUpdate.image = {
        url: await FileUtils.loadToDataURL(this.newProfileImage)
      };
    } else {
      userToUpdate.image = undefined;
    }
    await api.userPut(userToUpdate);
  }

  private async mounted() {
    if (this.user) {
      this.imageUrl = this.user.image!.url!;
      this.userName = this.user.name!;
    }
  }
}
</script>

<style scoped lang="scss">
.profileData {
  background-color: $transbarentBackground;
  padding: $formDataPadding;
  margin: 2em;
}
</style>
