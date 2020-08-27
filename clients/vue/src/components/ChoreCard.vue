<template>
  <div v-if="loaded" class="panel">
    <div class="choreHead panel-heading">
      <table>
        <tr>
          <td class="headTitle">
            <b-icon
              v-if="isBehind"
              class="headerItem"
              icon="exclamation-triangle"
              size="is-small"
              type="is-danger"
            ></b-icon>
            <span class="headerItem">{{ choreSum.chore.title }}</span>
          </td>
          <td class="headButtonContainer">
            <a @click="decreaseWithConfirm()">
              <b-icon class="headButton" icon="minus-square" size="is-medium" type="is-danger"></b-icon>
            </a>
            <a @click="increaseWithConfirm()">
              <b-icon class="headButton" icon="plus-square" size="is-medium" type="is-info"></b-icon>
            </a>
          </td>
        </tr>
      </table>
    </div>

    <div v-if="choreSum.chore.description" class="panel-block">
      <a class="infoButton" @click="showChoreClicked()">
        <b-icon icon="info-circle" size="is-small"></b-icon>
      </a>
      {{choreSum.chore.description}}
    </div>
    <div class="userList">
      <div class="userListEntry" v-for="userAndCount in userAndCounts" :key="userAndCount.user.id">
        <div>
          <strong>
            <template v-if="userAndCount.count > 0">+</template>
            <template>{{userAndCount.count}}</template>
          </strong>
        </div>
        <img class="userImage" :src="userAndCount.user.image.url" alt="Image" />
        <div class="name">{{userAndCount.user.name}}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";
import { Group, ChoreSummary, ChoreApi, User } from "@/generated/api-axios";
import { RouterNames } from "@/untils/RouterNames";
import config from "@/../config";

class UserAndCount {
  public count = 0;
  public user!: User;
}

@Component
export default class ChoreCard extends Vue {
  @Prop() private choreSum!: ChoreSummary;
  @Prop() private myUserId!: number;
  @Prop() private group!: Group;

  private loaded = false;
  private userAndCounts: UserAndCount[] = [];
  private canEdit = false;
  private changeCount = false;
  private myCount = 0;

  private highestOtherCount = 0;

  private isBehind = false;

  private updateheaderColorClass() {
    this.isBehind = this.userAndCounts[0].count <= this.highestOtherCount - 3;
  }

  private async increaseWithConfirm() {
    if (!this.changeCount) {
      this.$buefy.dialog.confirm({
        title: "Add Chore",
        message:
          "Are you sure you want to <b>add</b> an entry to <b>" +
          this.choreSum!.chore!.title +
          "</b>?",
        confirmText: "Add",
        type: "is-success",
        hasIcon: true,
        onConfirm: () => this.increase()
      });
    }
  }

  private async increase() {
    this.changeCount = true;
    const api = new ChoreApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });

    await api.choreChoreIdPost(Number(this.choreSum!.chore!.id));
    this.changeCount = false;
    this.$buefy.toast.open({
      duration: 1000,
      message: `Successfully increased`,
      type: "is-success"
    });
    this.myCount++;
    this.userAndCounts[0].count++;
    this.updateheaderColorClass();
  }

  private async decreaseWithConfirm() {
    if (this.myCount > 0 && !this.changeCount) {
      this.$buefy.dialog.confirm({
        title: "Remove Chore",
        message:
          "Are you sure you want to <b>remove</b> an entry from <b>" +
          this.choreSum!.chore!.title +
          "</b>?",
        confirmText: "Remove",
        type: "is-danger",
        hasIcon: true,
        onConfirm: () => this.decrease()
      });
    }
  }

  private async decrease() {
    this.changeCount = true;
    const api = new ChoreApi({
      basePath: config.basePath,
      apiKey: localStorage.apiKey
    });

    await api.choreChoreIdEntryDelete(Number(this.choreSum!.chore!.id));
    this.changeCount = false;
    this.$buefy.toast.open({
      duration: 1000,
      message: `Successfully decreased`,
      type: "is-success"
    });
    this.myCount--;
    this.userAndCounts[0].count--;
    this.updateheaderColorClass();
  }

  private showChoreClicked() {
    this.$router.push({
      name: RouterNames.SHOW_CHORE,
      params: { choreId: this.choreSum.chore!.id!.toString() }
    });
  }
  private async refresh() {
    this.userAndCounts = [];
    this.canEdit = this.group.owner == this.myUserId;
    const minCount = this.getMinCount();
    const myUserAndCount = new UserAndCount();
    for (const user of this.group.users!) {
      if (user.id == this.myUserId) {
        this.myCount = this.getCount(user.id);
        myUserAndCount.count = this.myCount - minCount;
        myUserAndCount.user = user;
      } else {
        const userAndCount = new UserAndCount();
        userAndCount.count = this.getCount(user.id!) - minCount;
        userAndCount.user = user;
        this.userAndCounts.push(userAndCount);
        if (this.highestOtherCount < userAndCount.count) {
          this.highestOtherCount = userAndCount.count;
        }
      }
    }

    this.userAndCounts.unshift(myUserAndCount);
    this.updateheaderColorClass();
    this.loaded = true;
  }

  private getMinCount(): number {
    let minCount = Number.MAX_VALUE;

    for (const user of this.group.users!) {
      const count = this.getCount(user.id!);
      if ((minCount ?? Number.MAX_VALUE) > count) {
        minCount = count;
      }
    }
    return minCount;
  }

  private getCount(userId: number) {
    for (const entry of this.choreSum.doneArray!) {
      if (entry.userId == userId) {
        return entry.count!;
      }
    }
    return 0;
  }

  private async mounted() {
    return this.refresh();
  }
}
</script>

<style scoped lang="scss">
table {
  width: 100%;
}

.choreHead {
  padding: 0;
}

.headButtonContainer {
  float: right;
}

.headButton {
  margin-right: 0.25em;
  margin-top: 0.25em;
  margin-bottom: 0.25em;
}

.headTitle {
  vertical-align: middle;
}

.headerItem {
  padding-left: 0.5em;
}

.infoButton {
  padding-right: 0.5em;
}

.userImage {
  border: solid gray 0.1em;
  border-radius: 50%;
  object-fit: cover;
  height: 3em;
  width: 3em;
}

.userList {
  display: inline-block;
  background-color: $transbarentBackground;
  width: 100%;
}

.userListEntry {
  display: inline-block;
  width: 6em;
}

.name {
  display: inline-block;
  width: 90%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>