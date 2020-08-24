<template>
  <div class>
    <b-loading v-if="loading" :is-full-page="true" :active="loading"></b-loading>
    <template v-if="atLeastOneRefreshDone">
      <div class="section cardsContainer">
        <div class="groupContainer">
          <GroupCard :group="group" />
        </div>
        <router-link :to="{ name: routeAddFinance, params: { groupId: group.id } }">
          <b-icon class="addButton" type="is-info" icon="plus-circle" size="is-large"></b-icon>
        </router-link>
        <div v-for="finance in finances" :key="finance.id">
          <a @click="viewDetails(finance.id)">
            <FinanceCard
              class="financeCard"
              :group="group"
              :myUserId="myUserId"
              :finance="finance"
            />
          </a>
        </div>
      </div>
      <div v-if="showLoadMoreButton" @click="loadMore()" class="showMoreButton">
        <b-button v-if="loadingMore" loading>Load More</b-button>
        <b-button v-else>Load More</b-button>
      </div>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { RouterNames } from "@/untils/RouterNames";
import GroupCard from "@/components/GroupCard.vue";
import FinanceCard from "@/components/FinanceCard.vue";
import {
  GroupApi,
  Group,
  FinanceApi,
  FinanceEntry
} from "@/generated/api-axios";
import config from "@/../config";
import { StateUtils } from "@/untils/StateUtils";

@Component({
  components: {
    FinanceCard,
    GroupCard
  }
})
export default class Finance extends Vue {
  private routeAddFinance = RouterNames.ADD_FINANCE;

  private atLeastOneRefreshDone = false;
  private loading = true;
  private group!: Group;
  private finances!: FinanceEntry[];
  private myUserId = localStorage.userId;
  private showLoadMoreButton = false;
  private loadingMore = false;
  private numOfFinancesToLoad = 10;

  private balanceColorClass() {
    return this.group.balance! < 0 ? "red" : "green";
  }

  get balance() {
    return this.group.balance!.toFixed(2);
  }

  private viewDetails(financeId: number) {
    this.$router.push({
      name: RouterNames.VIEW_FINANCE,
      params: {
        groupId: this.group.id!.toString(),
        financeId: financeId.toString()
      }
    });
  }

  private async loadMore() {
    if (this.loadingMore || !this.showLoadMoreButton) {
      return;
    }
    this.loadingMore = true;
    const lastFinance = this.finances[this.finances.length - 1];
    const api = new FinanceApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });

    const newFinances = (
      await api.groupGroupIdFinanceGet(
        Number(this.$route.params.groupId),
        this.numOfFinancesToLoad,
        lastFinance.id
      )
    ).data;
    for (const finance of newFinances) {
      this.finances.push(finance);
    }
    this.showLoadMoreButton = newFinances.length == this.numOfFinancesToLoad;

    this.loadingMore = false;
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

    const financesCall = financeApi.groupGroupIdFinanceGet(
      Number(this.$route.params.groupId),
      this.numOfFinancesToLoad
    );
    const groupCall = groupApi.groupGroupIdGet(
      Number(this.$route.params.groupId)
    );

    this.group = (await groupCall).data;
    this.finances = (await financesCall).data;

    this.showLoadMoreButton = this.finances.length == this.numOfFinancesToLoad;
    this.loading = false;
    this.atLeastOneRefreshDone = true;
  }

  private onScroll() {
    const bottomOfWindow =
      document.documentElement.scrollTop + window.innerHeight ===
      document.documentElement.offsetHeight;

    if (bottomOfWindow) {
      this.loadMore();
    }
  }

  private async mounted() {
    window.addEventListener("scroll", this.onScroll);
    StateUtils.setActiveGroupId(Number(this.$route.params.groupId));
    StateUtils.setLastActiveDefaultRouteName(this.$route.name!);
    return this.refresh();
  }

  private async beforeDestroy() {
    window.removeEventListener("scroll", this.onScroll);
  }
}
</script>

<style scoped>
.yourBalanceText {
  padding-right: 0.5em;
}
.yourBalanceNumber {
  font-weight: bold;
}

.financeCard {
  padding-top: 0.5em;
}

.cardsContainer {
  padding-top: 0.5em;
  padding-bottom: 1em;
}

.showMoreButton {
  padding-bottom: 1em;
}

.panel-heading {
  background-color: #ffffff42;
}

.groupContainer {
  margin-bottom: 0.5em;
}
.addButton {
  background-color: white;
  border-radius: 50%;
}
</style>
