<template>
  <div class>
    <div>
      <b-button @click="back" class="backButton" icon-left="backspace">BACK</b-button>
    </div>
    <Disclaimer></Disclaimer>

    <div class="faq">
      <p>
        <b>Why another chores/money splitting app you ask?</b>
      </p>
      <p>
        There is no free (no ads), opensource or self hosted alternative to all those apps out there.
        <br />Splitastic is here to change that!
      </p>
    </div>
    <div class="faq">
      <b>Contact the hoster the easy way</b>
      <br />
      {{email}}
    </div>
    <div class="faq">
      <b>Contribute or contact the dev</b>
      <br />TODO: link to github repo here
    </div>
    <div class="faq">
      <b>Send a coffee to the dev</b>
      <br />
      <br />
      <form action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_top">
        <input type="hidden" name="cmd" value="_donations" />
        <input type="hidden" name="business" value="NF3WSZDNMDGUL" />
        <input type="hidden" name="item_name" value="Splitastic Donation" />
        <input type="hidden" name="currency_code" value="EUR" />
        <input
          type="image"
          src="https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif"
          border="0"
          name="submit"
          title="PayPal - The safer, easier way to pay online!"
          alt="Donate with PayPal button"
        />
        <img alt border="0" src="https://www.paypal.com/en_US/i/scr/pixel.gif" width="1" height="1" />
      </form>

      <br />
      <br />Bitcoin
      <div>
        <a href="bitcoin:17HupgooacQV1kutoUXLaJ21zfVe9jkEiL?amount=0.002&label=Splitastic">
          <img class="btcImage" src="/img/btc.png" />
        </a>
        <p>17HupgooacQV1kutoUXLaJ21zfVe9jkEiL</p>
      </div>
    </div>
  </div>
</template>


<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import Disclaimer from "@/components/Disclaimer.vue";
import config from "@/../config";

@Component({
  components: {
    Disclaimer
  }
})
export default class About extends Vue {
  private email = config.aboutEmail;
  private back() {
    this.$router.back();
  }
}
</script>

<style lang="scss">
.backButton {
  margin: 1em;
}
.faq {
  padding: 1.5em;
  margin: 0.5em 1em 0.5em 1em;
  background-color: $transbarentBackground;
}

.btcImage {
  max-width: 10em;
}
</style>