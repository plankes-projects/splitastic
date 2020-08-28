<template>
  <div class="panel">
    <div class="financeHead panel-heading">
      <div class="level is-mobile">
        <div class="level-left">{{ getUserName() }}</div>
        <div class="level-right">{{getCreated()}}</div>
      </div>
    </div>
    <article class="panel-block media myArticel">
      <div class="media-left">
        <figure class="image myImage">
          <img class="userImage" :src="getUserImage()" alt="Image" />
        </figure>
      </div>
      <b-icon
        v-if="hasProxyInsertIcon"
        class="proxyInsertIcon"
        icon="hands-helping"
        size="is-small"
      ></b-icon>
      <div class="media-content">
        <div class="myContent content">
          <p>
            <strong>{{ this.financeTitle }}</strong>
          </p>
        </div>
        <div class="level is-mobile">
          <div class="level-left totalSum">Total Sum: {{ totalSum() }}€</div>

          <div
            class="level-right payment"
            :class="paymentColorClass()"
          >{{ balanceSignString }}{{payment()}}€</div>
        </div>
      </div>
    </article>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";
import { Group, FinanceEntry } from "@/generated/api-axios";
import { RouterNames } from "@/untils/RouterNames";

@Component
export default class FinanceCard extends Vue {
  @Prop() private finance!: FinanceEntry;
  @Prop() private myUserId!: number;
  @Prop() private group!: Group;

  private financeTitle = "No Title";
  private hasProxyInsertIcon = false;

  get balanceSignString(): string {
    return this.finance.spentFrom == this.myUserId ? "" : "-";
  }

  get isOwner(): boolean {
    return this.finance.spentFrom == this.myUserId;
  }

  private paymentColorClass(): string {
    return this.finance.spentFrom == this.myUserId ? "green" : "red";
  }

  private payment(): string {
    const myEntry = this.finance.spentFrom == this.myUserId;
    const spentArray = this.finance.spent!;
    let result = 0;
    for (const spent of spentArray) {
      if (myEntry && spent.spentFor != this.myUserId) {
        result += spent.amount!;
      }
      if (!myEntry && spent.spentFor == this.myUserId) {
        result += spent.amount!;
      }
    }
    return result.toFixed(2);
  }

  private totalSum(): string {
    const spentArray = this.finance.spent!;
    let result = 0;
    for (const spent of spentArray) {
      result += spent.amount!;
    }
    return result.toFixed(2);
  }

  private getCreated(): string {
    const d = new Date(this.finance.created!);
    const ye = new Intl.DateTimeFormat("en", { year: "numeric" }).format(d);
    const mo = new Intl.DateTimeFormat("en", { month: "short" }).format(d);
    const da = new Intl.DateTimeFormat("en", { day: "2-digit" }).format(d);
    return `${da} ${mo} ${ye}`;
  }

  private getUserName(): string {
    for (const user of this.group.users!) {
      if (user.id == this.finance.spentFrom) {
        return user.name!.substring(0, 15);
      }
    }
    return "UNKNOWN";
  }

  private getUserImage(): string | undefined {
    for (const user of this.group.users!) {
      if (user.id == this.finance.spentFrom) {
        return user.image!.url;
      }
    }
    return undefined;
  }

  private mounted() {
    if ((this.finance.title ?? "").length > 0) {
      this.financeTitle = this.finance.title!;
    }
    this.hasProxyInsertIcon = this.finance.createdBy != this.finance.spentFrom;
  }
}
</script>

<style scoped lang="scss">
.card {
  padding: 2em;
}

.groupMemberCount {
  padding-right: 0.1em;
}

.financeHead {
  padding: 0.2em;
  font-size: 1em;
  padding-left: 3.4em;
  color: gray;
}

.totalSum {
  color: gray;
  font-size: 1em;
}

.myContent {
  margin: 0 !important;
}

.payment {
  font-weight: bold;
}

.myImage {
  margin-top: -3.1em;
  margin-left: -0.4em;
  margin-right: -0.75em;
  height: 3em;
  width: 3em;
}

.userImage {
  border: solid gray 0.1em;
  border-radius: 50%;
  object-fit: cover;
  height: 100%;
  width: 100%;
}

.myArticel {
  padding: 0;
  padding-left: 0.5em;
  padding-right: 0.25em;
}

.red {
  color: red;
}
.green {
  color: green;
}

.proxyInsertIcon {
  position: absolute;
  margin-top: 1.5em;
}
</style>
