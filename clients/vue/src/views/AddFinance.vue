<template>
  <div class>
    <b-loading v-if="loading" :is-full-page="true" :active="loading"></b-loading>
    <template v-if="atLeastOneRefreshDone">
      <b-button
        class="backButton"
        type="is-warning"
        icon-left="arrow-left"
        @click="backClicked()"
      >Back</b-button>

      <div class="formData">
        <section>
          <div class>
            <b-field label="Title">
              <b-autocomplete
                v-model="title"
                :data="filteredTitleArray"
                :open-on-focus="true"
                :placeholder="titlePlaceholder"
                dropdown-position="bottom"
              ></b-autocomplete>
            </b-field>

            <b-field label="Amount Spent (€)">
              <b-input v-model="amount" placeholder="Amount" type="number" step="0.01" min="0.01"></b-input>
            </b-field>

            <b-button size="is-large" @click="add">Add</b-button>

            <p class="forLabel">
              <b>For</b>
            </p>
            <div v-for="user in group.users" :key="user.id">
              <b-checkbox-button
                v-model="userIdsForSpent"
                :native-value="user.id"
                type="is-success"
                class="block userButton"
              >
                <div>{{ user.name }}</div>
              </b-checkbox-button>
              <figure class="image imageContainer">
                <img class="myImage" :src="user.image.url" alt="Image" />
              </figure>
            </div>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { RouterNames } from "@/untils/RouterNames";
import {
  GroupApi,
  Group,
  FinanceApi,
  FinanceEntry,
  FinanceEntryEntry
} from "@/generated/api-axios";
import config from "@/../config";

@Component
export default class AddFinance extends Vue {
  private atLeastOneRefreshDone = false;
  private loading = true;
  private userIdsForSpent: number[] = [];

  private group!: Group;
  private title = "";
  private amount!: number;
  private titleSuggestions: string[] = [];
  private titlePlaceholder = "e.g. grocery";

  get filteredTitleArray() {
    return this.titleSuggestions.filter(option => {
      return (
        option
          .toString()
          .toLowerCase()
          .indexOf(this.title.toLowerCase()) >= 0
      );
    });
  }

  private backClicked() {
    this.$router.push({ name: RouterNames.FINANCE });
  }

  private async add() {
    if (this.userIdsForSpent.length == 0) {
      this.$buefy.toast.open({
        duration: 1000,
        message: `Need at least one user`,
        type: "is-danger"
      });
      return;
    }

    const spentPerPerson = this.amount / this.userIdsForSpent.length;

    const finance: FinanceEntry = {};
    finance.spentFrom = localStorage.userId;
    finance.title = this.title;
    finance.spent = [];

    for (const userIdForSpent of this.userIdsForSpent) {
      const entry1: FinanceEntryEntry = {};
      entry1.spentFor = userIdForSpent;
      entry1.amount = spentPerPerson;
      finance.spent.push(entry1);
    }

    const api = new FinanceApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });

    this.loading = true;

    await api.groupGroupIdFinancePost(
      Number(this.$route.params.groupId),
      finance
    );
    this.$router.push({ name: RouterNames.FINANCE });
  }

  private async refresh() {
    this.loading = true;
    const groupApi = new GroupApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });
    const financeApi = new FinanceApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });

    const groupPromise = groupApi.groupGroupIdGet(
      Number(this.$route.params.groupId)
    );
    const suggestionsPromise = financeApi.groupGroupIdFinanceTitleSuggestionsGet(
      Number(this.$route.params.groupId)
    );
    this.group = (await groupPromise).data;
    this.titleSuggestions = (await suggestionsPromise).data;
    if (this.titleSuggestions.length > 0) {
      this.titlePlaceholder = "e.g. " + this.titleSuggestions[0];
    }

    const users = this.group.users!;
    for (const user of users) {
      this.userIdsForSpent.push(user.id!);
    }

    this.loading = false;
    this.atLeastOneRefreshDone = true;
  }

  private async mounted() {
    return this.refresh();
  }
}
</script>

<style scoped lang="scss">
.yourBalanceText {
  font-size: 0.95em;
}
.yourBalanceNumber {
  font-weight: bold;
}

.imageContainer {
  width: 3.5em;
  height: 3.5em;
  position: absolute;
  margin-top: -2.6em;
}

.myImage {
  border-radius: 50%;
  object-fit: cover;
  width: 100%;
  height: 100%;
}

.userButton {
  margin: 0;
  height: 4em;
  padding-top: 2em;
  padding-left: 3em;
}

.forLabel {
  margin-top: 1em;
  margin-bottom: -1em;
}

.section {
  padding-top: 1em;
}

.backButton {
  position: absolute;
  left: 0.25em;
  margin-top: -0.75em;
}

.formData {
  background-color: $transbarentBackground;
  padding: $formDataPadding;
  margin: 1em;
}
</style>
