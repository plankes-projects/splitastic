<template>
  <div class="box">
    <article class="media">
      <div class="media-left">
        <figure class="image is-64x64">
          <img class="roundImage" :src="group.image.url" alt="Image" />
        </figure>
        <div class="yourBalanceNumber has-text-centered" :class="balanceColorClass()">{{ balance }}€</div>
      </div>
      <div class="media-content">
        <div class="content">
          <p>
            <strong>{{ this.group.name }}</strong>
            <br />
            {{ this.group.description }}
          </p>
        </div>
        <div class="level is-mobile">
          <div class="level-left">
            <div class="level-item">
              <p class="groupMemberCount">{{ this.group.users.length }}</p>
              <b-icon icon="users" size="is-small"></b-icon>
            </div>
            <a @click="showGroupClicked(group.id)" class="level-item">
              <b-icon icon="info" size="is-small"></b-icon>
            </a>
            <a v-if="isOwner" @click="editGroupClicked(group.id)" class="level-item">
              <b-icon icon="edit" size="is-small"></b-icon>
            </a>
          </div>

          <div class="level-right">
            <div v-if="showFinanceLink" class="groupSectionButton">
              <a @click="financeClicked(group.id)" class="level-item">
                <b-icon icon="dollar-sign" size="is-small"></b-icon>
              </a>
            </div>

            <div v-if="showChoreLink" class="groupSectionButton">
              <a @click="choreClicked(group.id)" class="level-item">
                <b-icon icon="tasks" size="is-small"></b-icon>
              </a>
            </div>
          </div>
        </div>
      </div>
    </article>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";
import { Group } from "@/generated/api-axios";
import { RouterNames } from "@/untils/RouterNames";

@Component
export default class GroupCard extends Vue {
  @Prop() private group!: Group;

  get isOwner(): boolean {
    return this.group.owner == localStorage.userId;
  }

  get showFinanceLink(): boolean {
    return this.$route.name != RouterNames.FINANCE;
  }

  get showChoreLink(): boolean {
    return this.$route.name != RouterNames.CHORE;
  }

  private balanceColorClass() {
    return this.group.balance! < 0 ? "red" : "green";
  }

  get balance() {
    return this.group.balance!.toFixed(2);
  }

  private financeClicked(groupId: number) {
    this.$router.push({
      name: RouterNames.FINANCE,
      params: { groupId: groupId.toString() }
    });
  }

  private choreClicked(groupId: number) {
    this.$router.push({
      name: RouterNames.CHORE,
      params: { groupId: groupId.toString() }
    });
  }

  private editGroupClicked(groupId: number) {
    this.$router.push({
      name: RouterNames.EDIT_GROUP,
      params: { groupId: groupId.toString() }
    });
  }

  private showGroupClicked(groupId: number) {
    this.$router.push({
      name: RouterNames.SHOW_GROUP,
      params: { groupId: groupId.toString() }
    });
  }
}
</script>

<style scoped lang="scss">
.card {
  padding: 2em;
}

.groupMemberCount {
  padding-right: 0.3em;
}

.yourBalanceNumber {
  font-weight: bold;
}

.roundImage {
  border-radius: 50%;
  object-fit: cover;
  height: 100%;
  width: 100%;
}

.groupSectionButton {
  padding-left: 0.5em;
}
</style>
