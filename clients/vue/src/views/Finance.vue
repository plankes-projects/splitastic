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
      </div>

      <b-tabs class="tabsContainer" v-model="activeTab" expanded>
        <b-tab-item class="tabContent" label="Entries">
          <template slot="header">
            <b-icon icon="list"></b-icon>
            <strong>Entries</strong>
          </template>
          <FinanceCardList
            :group="group"
            :finances="finances"
            :numOfFinancesToLoad="numOfFinancesToLoad"
          />
        </b-tab-item>

        <b-tab-item class="tabContent" label="Balance">
          <template slot="header">
            <b-icon icon="chart-pie"></b-icon>
            <strong>Balance</strong>
          </template>
          <FinanceBalanceTab :group="group" :balanceData="balanceData" />
        </b-tab-item>
      </b-tabs>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { RouterNames } from "@/untils/RouterNames";
import GroupCard from "@/components/GroupCard.vue";
import FinanceCard from "@/components/FinanceCard.vue";
import FinanceCardList from "@/components/Finance/FinanceCardList.vue";
import FinanceBalanceTab from "@/components/Finance/FinanceBalanceTab.vue";
import {
  GroupApi,
  Group,
  FinanceApi,
  FinanceEntry,
  GroupBalanceData
} from "@/generated/api-axios";
import config from "@/../config";
import { StateUtils } from "@/untils/StateUtils";

@Component({
  components: {
    FinanceCard,
    GroupCard,
    FinanceCardList,
    FinanceBalanceTab
  }
})
export default class Finance extends Vue {
  private routeAddFinance = RouterNames.ADD_FINANCE;

  private atLeastOneRefreshDone = false;
  private loading = true;
  private group!: Group;
  private finances!: FinanceEntry[];
  private balanceData!: GroupBalanceData;
  private numOfFinancesToLoad = 10;
  private activeTab = 0;

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

    const balanceDataCall = groupApi.groupGroupIdBalanceGet(
      Number(this.$route.params.groupId)
    );

    this.group = (await groupCall).data;
    this.finances = (await financesCall).data;
    this.balanceData = (await balanceDataCall).data;

    this.loading = false;
    this.atLeastOneRefreshDone = true;
  }

  private async mounted() {
    StateUtils.setActiveGroupId(Number(this.$route.params.groupId));
    StateUtils.setLastActiveDefaultRouteName(this.$route.name!);
    return this.refresh();
  }
}
</script>

<style scoped>
.balanceTab {
  padding-left: 0;
  padding-right: 0;
}
.tabsContainer {
  margin-top: -3em;
}
.tabContent {
  margin-left: 1em;
  margin-right: 1em;
}

.cardsContainer {
  padding-top: 0.5em;
  padding-bottom: 0em;
}
.groupContainer {
  margin-bottom: 0.5em;
}
.addButton {
  background-color: white;
  border-radius: 50%;
  z-index: 1;
  position: relative;
}
</style>
