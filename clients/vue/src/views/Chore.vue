<template>
  <div class>
    <b-loading v-if="loading" :is-full-page="true" :active="loading"></b-loading>
    <template v-if="atLeastOneRefreshDone">
      <div class="section cardsContainer">
        <div class="groupContainer">
          <GroupCard :group="group" />
        </div>
        <div v-for="choreSum in choreSums" :key="choreSum.chore.id">
          <ChoreCard class="choreCard" :group="group" :myUserId="myUserId" :choreSum="choreSum" />
        </div>

        <div class="addButtonContainer">
          <a v-if="isOwner" @click="createNewChore()">
            <b-icon class="addButton" type="is-info" icon="plus-circle" size="is-large"></b-icon>
          </a>
          <div v-else>Only owner of the group can create new chores.</div>
        </div>
      </div>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { RouterNames } from "@/untils/RouterNames";
import { StateUtils } from "@/untils/StateUtils";
import GroupCard from "@/components/GroupCard.vue";
import ChoreCard from "@/components/ChoreCard.vue";
import { GroupApi, Group, ChoreSummary, ChoreApi } from "@/generated/api-axios";
import config from "@/../config";

@Component({
  components: {
    ChoreCard,
    GroupCard
  }
})
export default class Chore extends Vue {
  private atLeastOneRefreshDone = false;
  private loading = true;
  private group!: Group;
  private choreSums!: ChoreSummary[];
  private isOwner = false;
  private myUserId = localStorage.userId;
  private showLoadMoreButton = false;
  private loadingMore = false;
  private numOfFinancesToLoad = 10;

  private async createNewChore() {
    const choreApi = new ChoreApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });

    const choreId = (
      await choreApi.chorePost(Number(this.$route.params.groupId))
    ).data;
    this.$router.push({
      name: RouterNames.EDIT_CHORE,
      params: { choreId: choreId.toString() }
    });
  }

  private sortChores() {
    this.choreSums.sort(
      (c1: ChoreSummary, c2: ChoreSummary) =>
        this.getChoreSortScore(c2) - this.getChoreSortScore(c1)
    );
  }

  private getChoreSortScore(choreSum: ChoreSummary): number {
    const myCount = this.getCount(this.myUserId, choreSum);
    const maxCount = this.getMaxOtherCount(choreSum);
    return maxCount - myCount;
  }

  private getMaxOtherCount(choreSum: ChoreSummary): number {
    let maxCount = 0;

    for (const user of this.group.users!) {
      if (user.id! != this.myUserId) {
        const count = this.getCount(user.id!, choreSum);
        if (maxCount < count) {
          maxCount = count;
        }
      }
    }
    return maxCount;
  }

  private getCount(userId: number, choreSum: ChoreSummary) {
    for (const entry of choreSum.doneArray!) {
      if (entry.userId == userId) {
        return entry.count!;
      }
    }
    return 0;
  }

  private async refresh() {
    this.loading = true;
    const groupApi = new GroupApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });

    const choreApi = new ChoreApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });

    const sumsCall = choreApi.choreSummariesGroupIdGet(
      Number(this.$route.params.groupId)
    );
    const groupCall = groupApi.groupGroupIdGet(
      Number(this.$route.params.groupId)
    );

    this.group = (await groupCall).data;
    this.isOwner = this.group.owner == this.myUserId;
    this.choreSums = (await sumsCall).data;

    this.sortChores();
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
.yourBalanceText {
  padding-right: 0.5em;
}
.yourBalanceNumber {
  font-weight: bold;
}

.choreCard {
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

.addButtonContainer {
  padding-top: 1em;
}
</style>