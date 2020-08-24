<template>
  <div class="formData">
    <b-loading v-if="loading" :is-full-page="true" :active="loading"></b-loading>
    <template v-if="atLeastOneRefreshDone">
      <b-field label="Title">
        <b-input v-model="title" :readonly="readOnly"></b-input>
      </b-field>
      <b-field label="Description">
        <b-input v-model="description" maxlength="255" type="textarea" :readonly="readOnly"></b-input>
      </b-field>
      <section>
        <button class="button is-warning backButton" @click="back()">
          <span>BACK</span>
        </button>

        <button v-if="isGroupOwner && readOnly" class="button" @click="editChoreClicked()">
          <span>EDIT</span>
        </button>

        <template v-if="!readOnly">
          <b-button v-if="!savingDetails" @click="saveDetails">SAVE</b-button>
          <b-icon v-else icon="sync" type="is-success" class="fa-spin"></b-icon>
          <button class="button is-danger deleteButton" @click="deleteChoreWithConfirm()">
            <span>DELETE</span>
          </button>
        </template>
      </section>
    </template>
  </div>
</template>


<script lang="ts">
import { Component, Vue, Prop } from "vue-property-decorator";
import { ChoreApi } from "@/generated/api-axios";
import config from "@/../config";
import { RouterNames } from "@/untils/RouterNames";
import { StateUtils } from "@/untils/StateUtils";

@Component
export default class ShowEditChore extends Vue {
  @Prop()
  private readOnly!: boolean;
  private isGroupOwner = false;
  private atLeastOneRefreshDone = false;
  private loading = true;
  private savingDetails = false;

  private title = "asd";
  private description = "desc";

  private editChoreClicked() {
    this.$router.push({
      name: RouterNames.EDIT_CHORE,
      params: { choreId: this.$route.params.choreId }
    });
  }

  private async deleteChoreWithConfirm() {
    this.$buefy.dialog.confirm({
      title: "Delete Chore",
      message:
        "Are you sure you want to <b>delete</b> this chore? This action cannot be undone.",
      confirmText: "Delete Chore",
      type: "is-danger",
      hasIcon: true,
      onConfirm: () => this.deleteChore()
    });
  }

  private async deleteChore() {
    const api = new ChoreApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });

    await api.choreChoreIdDelete(Number(this.$route.params.choreId));
    this.$router.push({
      name: RouterNames.CHORE,
      params: { groupId: StateUtils.getActiveGroupId()!.toString() }
    });
  }

  private async back() {
    this.$router.back();
  }

  private async saveDetails() {
    this.savingDetails = true;
    const api = new ChoreApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });

    await api.chorePut({
      title: this.title,
      description: this.description,
      id: Number(this.$route.params.choreId)
    });
    this.$buefy.toast.open({
      duration: 1000,
      message: `Successfully updated`,
      type: "is-success"
    });
    this.savingDetails = false;
  }

  private async refresh() {
    this.loading = true;
    const api = new ChoreApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });

    const chore = (
      await api.choreChoreIdGet(Number(this.$route.params.choreId))
    ).data;

    this.title = chore.title!;
    this.description = chore.description!;
    this.isGroupOwner = chore.isOwner!;

    this.loading = false;
    this.atLeastOneRefreshDone = true;
  }

  private async mounted() {
    return this.refresh();
  }
}
</script>


<style scoped lang="scss">
.formData {
  background-color: $transbarentBackground;
  padding: $formDataPadding;
  margin: 1em;
}

.deleteButton {
  margin-left: 1em;
}
.backButton {
  margin-right: 1em;
}

.editButton {
}
</style>