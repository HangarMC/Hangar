<template>
  <HangarModal target-id="donation-form" label-id="donation-label">
    <template v-slot:activator="slotProps">
      <slot name="activator" v-bind:targetId="slotProps.targetId"></slot>
    </template>
    <template v-slot:modal-content>
      <div class="modal-header">
        <h4>Donate to ....</h4>
        <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <div class="row">
          <div class="col-xs-12">
            <button type="button" @click="monthly = true" :class="'btn ' + (monthly ? 'btn-primary' : 'btn-default')">
              Monthly
            </button>
            <button type="button" @click="monthly = false" :class="'btn ' + (monthly ? 'btn-default' : 'btn-primary')">
              One time
            </button>
          </div>
        </div>

        <form action="https://sandbox.paypal.com/cgi-bin/webscr" method="post" @submit="popup($el)">
          <input type="hidden" name="business" value="minidigger-author@hangar.minidigger.me">

          <template v-if="monthly">
            <input type="hidden" name="cmd" value="_xclick-subscriptions">
            <input type="hidden" name="lc" value="US">
            <input type="hidden" name="no_note" value="1">
            <input type="hidden" name="no_shipping" value="1">
            <input type="hidden" name="src" value="1">
            <input type="hidden" name="p3" value="1">
            <input type="hidden" name="t3" value="M">
          </template>
          <template v-else>
            <input type="hidden" name="cmd" value="_donations">
          </template>

          <input type="hidden" name="currency_code" value="USD">
          <input type="hidden" name="charset" value="utf-8">
          <input type="hidden" name="notify_url" value="https://hangar.minidigger.me/paypal/ipn">

          <div class="row">
            <div class="col-xs-12">
              <button type="button" @click="amount = 10"
                      :class="'btn ' + (amount === 10 ? 'btn-primary' : 'btn-default')">$10
              </button>
              <button type="button" @click="amount = 20"
                      :class="'btn ' + (amount === 20 ? 'btn-primary' : 'btn-default')">$20
              </button>
              <button type="button" @click="amount = 30"
                      :class="'btn ' + (amount === 30 ? 'btn-primary' : 'btn-default')">$30
              </button>
            </div>
          </div>

          <div class="row">
            <div class="col-xs-12">
              <div>Select an amount above or enter an amount below</div>
            </div>
          </div>

          <div class="row">
            <div class="col-xs-12">
              <div>
                <input type="number" :name="monthly ? 'a3' : 'amount'" v-model="amount">
                <div>USD</div>
              </div>
              <small>By donating to X you agree to Y and that tacos are delicious</small>
              <div>
                <input type="submit" :value="'Donate ' + (monthly ? 'Monthly' : 'Once')" class="btn btn-primary" formtarget="_blank">
                <img alt="" width="1" height="1" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif">
              </div>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button id="close-modal" type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      </div>
    </template>
  </HangarModal>
</template>

<script>
import HangarModal from "@/components/HangarModal";

export default {
  name: "DonationModal",
  components: {HangarModal},
  data() {
    return {
      monthly: true,
      amount: 20,
    }
  },
  methods: {
    popup(form) {
      // window.open('', 'formpopup', 'width=400,height=400,resizeable,scrollbars');
      // form.target = 'formpopup';
    }
  }
}
</script>

<style scoped>

</style>
