<template>
  <div>
    <div class="cardsContainer">
      <div v-for="finance in finances" :key="finance.id">
        <a @click="viewDetails(finance.id)">
          <FinanceCard class="financeCard" :group="group" :myUserId="myUserId" :finance="finance" />
        </a>
      </div>
    </div>
    <div v-if="showLoadMoreButton" @click="loadMore()" class="showMoreButton">
      <b-button v-if="loadingMore" loading>Load More</b-button>
      <b-button v-else>Load More</b-button>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from "vue-property-decorator";
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
export default class FinanceCardList extends Vue {
  @Prop()
  private group!: Group;

  @Prop()
  private finances!: FinanceEntry[];

  @Prop()
  private numOfFinancesToLoad!: number;

  private myUserId = localStorage.userId;
  private showLoadMoreButton = false;
  private loadingMore = false;

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

  private onScroll() {
    const bottomOfWindow =
      document.documentElement.scrollTop + window.innerHeight ===
      document.documentElement.offsetHeight;

    if (bottomOfWindow) {
      this.loadMore();
    }
  }

  private async mounted() {
    this.showLoadMoreButton = this.finances.length == this.numOfFinancesToLoad;
  }
}
</script>

<style scoped>
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

.groupContainer {
  margin-bottom: 0.5em;
}
.addButton {
  background-color: white;
  border-radius: 50%;
}
</style>
