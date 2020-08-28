<template>
  <div v-if="isMounted">
    <div class="userList">
      <div class="total">
        <strong>Total expenses: {{totalExpenses}}€</strong>
      </div>
      <div
        class="userListEntry"
        v-for="userAndBalace in userAndBalaceArray"
        :key="userAndBalace.user.id"
      >
        <div>
          <strong :class="balanceColorClass(userAndBalace.balance)">
            <template v-if="userAndBalace.balance > 0">+</template>
            <template>{{formatBalance(userAndBalace.balance)}}</template>
          </strong>
        </div>
        <img class="userImage" :src="userAndBalace.user.image.url" alt="Image" />
        <div class="name">{{userAndBalace.user.name}}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from "vue-property-decorator";
import { GroupBalanceData, Group, User } from "@/generated/api-axios";

class UserAndBalance {
  public balance = 0.0;
  public user!: User;
}

@Component
export default class FinanceBalanceTab extends Vue {
  private isMounted = false;
  @Prop()
  private group!: Group;
  @Prop()
  private balanceData!: GroupBalanceData;

  private userAndBalaceArray!: UserAndBalance[];

  private formatBalance(balance: number) {
    return balance.toFixed(2);
  }

  @Watch("balanceData")
  balanceDataChanged(newVal: GroupBalanceData) {
    this.userAndBalaceArray = [];

    for (const userBalance of newVal.userBalances!) {
      for (const user of this.group.users!) {
        if (user.id == userBalance.userId) {
          this.userAndBalaceArray.push({
            balance: userBalance.balance!,
            user: user
          });
          continue;
        }
      }
    }
  }

  private balanceColorClass(balance: number) {
    return balance < 0 ? "red" : "green";
  }

  get totalExpenses() {
    return this.group.totalExpenses!.toFixed(2);
  }

  private mounted() {
    this.balanceDataChanged(this.balanceData);
    this.isMounted = true;
  }
}
</script>

<style scoped lang="scss">
.userImage {
  border: solid gray 0.1em;
  border-radius: 50%;
  object-fit: cover;
  height: 3em;
  width: 3em;
}

.userList {
  display: inline-block;
  width: 100%;
  margin-left: -1em;
  margin-right: -1em;
  background-color: $transbarentBackground;
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

.total {
  margin-top: 1em;
  margin-bottom: 1em;
}
</style>
