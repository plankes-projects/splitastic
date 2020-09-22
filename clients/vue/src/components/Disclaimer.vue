<template>
  <b-message type="is-danger">
    <b>Disclaimer</b>
    <p>
      Splitastic is a private project and is hosted on a private server.
      You can use Splitastic for free and it does not generate any revenue.
      Use Splitastic on your own risk.
      All data could be lost in the unlikely case of hardware failure!
    </p>
  </b-message>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

@Component
export default class Disclaimer extends Vue {}
</script>

<style scoped lang="scss">
</style>
