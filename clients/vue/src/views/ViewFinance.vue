<template>
  <div class>
    <b-loading v-if="loading" :is-full-page="true" :active="loading"></b-loading>
    <template v-else>
      <b-button
        class="backButton"
        type="is-warning"
        icon-left="arrow-left"
        @click="backClicked()"
      >Back</b-button>
      <section>
        <div class="formData">
          <b-field label="Title">
            <b-input v-model="finance.title" readonly></b-input>
          </b-field>
          <b-field label="Amount Spent (€)">
            <b-input v-model="amount" placeholder="Number" type="number" readonly></b-input>
          </b-field>

          <div v-if="createdByUser">
            <p class="forLabel">
              <b-icon class="proxyInsertIcon" icon="hands-helping" size="is-small"></b-icon>
              <b>Inserted by:</b>
            </p>
            <b-checkbox-button type="is-success" class="block userButton" disabled="disabled">
              <div class="name">{{ createdByUser.name }}</div>
            </b-checkbox-button>
            <figure class="image imageContainer">
              <img class="myImage" :src="createdByUser.image.url" alt="Image" />
            </figure>
          </div>
          <p class="forLabel">
            <b>Paid by:</b>
          </p>
          <b-checkbox-button type="is-success" class="block userButton" disabled="disabled">
            <div class="name">{{ fromUser.name }}</div>
          </b-checkbox-button>
          <figure class="image imageContainer">
            <img class="myImage" :src="fromUser.image.url" alt="Image" />
          </figure>

          <p class="forLabel">
            <b>Bought for:</b>
          </p>
          <div v-for="user in group.users" :key="user.id">
            <b-checkbox-button
              v-model="userIdsForSpent"
              :native-value="user.id"
              type="is-success"
              class="block userButton"
              disabled="disabled"
            >
              <div class="name">{{user.name}}</div>
            </b-checkbox-button>
            <figure class="image imageContainer">
              <img class="myImage" :src="user.image.url" alt="Image" />
            </figure>
          </div>

          <b-button
            v-if="isOwner()"
            @click="deleteClicked()"
            class="deleteButton"
            type="is-danger"
            icon-left="trash"
          >Delete</b-button>
        </div>
      </section>
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
  User
} from "@/generated/api-axios";
import config from "@/../config";

@Component
export default class ViewFinance extends Vue {
  private atLeastOneRefreshDone = false;
  private loading = true;
  private userIdsForSpent!: number[];

  private group!: Group;
  private finance!: FinanceEntry;
  private amount = 0;
  private financeId!: number;
  private fromUser!: User;
  private createdByUser: User | null = null;

  private isOwner() {
    return localStorage.userId == this.finance.spentFrom;
  }

  private backClicked() {
    this.$router.push({ name: RouterNames.FINANCE });
  }

  private deleteClicked() {
    this.$buefy.dialog.confirm({
      title: "Deleting Entry",
      message:
        "Are you sure you want to <b>delete</b> this entry? This action cannot be undone.",
      confirmText: "Delete Entry",
      type: "is-danger",
      hasIcon: true,
      onConfirm: () => this.deleteEntry()
    });
  }
  private async deleteEntry() {
    const api = new FinanceApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });
    try {
      this.loading = true;
      await api.financeFinanceIdDelete(this.financeId);
      this.$router.push({ name: RouterNames.FINANCE });
    } catch (e) {
      this.$buefy.toast.open({
        duration: 1000,
        message: `Delete failed =(`,
        type: "is-danger"
      });
    }
    this.loading = false;
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

    const groupCall = groupApi.groupGroupIdGet(
      Number(this.$route.params.groupId)
    );
    const financeCall = financeApi.financeFinanceIdGet(this.financeId);

    this.group = (await groupCall).data;
    this.finance = (await financeCall).data;

    this.userIdsForSpent = [];
    for (const spent of this.finance.spent!) {
      this.userIdsForSpent.push(spent.spentFor!);
      this.amount += spent.amount!;
    }

    const users = this.group.users!;
    for (const user of users) {
      if (user.id == this.finance.spentFrom) {
        this.fromUser = user;
      }
      if (
        this.finance.spentFrom != this.finance.createdBy &&
        user.id == this.finance.createdBy
      ) {
        this.createdByUser = user;
      }
    }

    this.amount = Number(this.amount.toFixed(2));
    this.loading = false;
  }

  private async mounted() {
    this.financeId = Number(this.$route.params.financeId);
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

.deleteButton {
  margin: 0.5em;
  margin-top: 3em;
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

.name {
  width: 90%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.proxyInsertIcon {
  padding-right: 1em;
}
</style>