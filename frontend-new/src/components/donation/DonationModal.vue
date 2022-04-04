<script lang="ts" setup>
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import InputText from "~/components/ui/InputText.vue";
import InputRadio from "~/components/ui/InputRadio.vue";
import Modal from "~/components/modals/Modal.vue";

const i18n = useI18n();

const props = defineProps<{
  donationTarget: string;
  donationEmail: string;
  returnUrl: string;
  cancelReturnUrl: string;
  oneTimeAmounts: number[];
  monthlyAmounts: number[];
  defaultAmount: number;
}>();
const amount = ref<string>(props.defaultAmount + "");
const monthly = ref(true);
</script>

<template>
  <Modal :title="i18n.t('donate.title', [donationTarget])">
    <template #default="{ on }">
      <form
        action="https://www.sandbox.paypal.com/cgi-bin/webscr"
        method="post"
        @submit="
          Window.open('', '', 'width=400,height=400,resizeable,scrollbars');
          $el.target = 'formpopup';
        "
      >
        <!-- https://developer.paypal.com/docs/paypal-payments-standard/integration-guide/Appx-websitestandard-htmlvariables/ -->
        <input type="hidden" name="business" :value="donationEmail" />
        <input type="hidden" name="return" :value="returnUrl" />
        <input type="hidden" name="cancel_return" :value="cancelReturnUrl" />
        <!-- return method, 2 = POST, with all variables -->
        <input type="hidden" name="rm" value="1" />
        <input
          type="hidden"
          name="item_name"
          :value="'Hangar: ' + (monthly ? i18n.t('donate.monthly') : i18n.t('donate.oneTime')) + ' Donation to ' + donationTarget"
        />
        <input type="hidden" name="quantity" value="1" />
        <!-- locale -->
        <input type="hidden" name="lc" value="US" />
        <input type="hidden" name="currency_code" value="USD" />
        <input type="hidden" name="charset" value="utf-8" />
        <input type="hidden" name="notify_url" value="https://hangar.minidigger.me/paypal/ipn" />
        <input type="hidden" name="custom" value="idk, maybe hangar user id?" />

        <template v-if="monthly">
          <input type="hidden" name="cmd" value="_xclick-subscriptions" />
          <input type="hidden" name="a3" :value="amount" />
          <input type="hidden" name="bn" :value="'Hangar_Subscribe_' + donationTarget + 'US'" />
          <input type="hidden" name="no_note" value="1" />
          <!-- 1 = do not prompt for an address -->
          <input type="hidden" name="no_shipping" value="1" />
          <!-- recurring -->
          <input type="hidden" name="src" value="1" />
          <!-- p3 + t3 = 1 month -->
          <input type="hidden" name="p3" value="1" />
          <input type="hidden" name="t3" value="M" />
        </template>
        <template v-else>
          <input type="hidden" name="cmd" value="_donations" />
          <input type="hidden" name="amount" :value="amount" />
          <input type="hidden" name="bn" :value="'Hangar_Donate_' + donationTarget + 'US'" />
        </template>

        <div class="flex flex-wrap">
          <div class="basis-full">
            <InputRadio v-model="monthly" :label="i18n.t('donate.monthly')" :value="true" />
            <InputRadio v-model="monthly" :label="i18n.t('donate.oneTime')" :value="false" />
          </div>
          <div class="basis-full">
            <InputRadio v-for="a in monthly ? monthlyAmounts : oneTimeAmounts" :key="a" v-model="amount" :value="a + ''"> ${{ a }} </InputRadio>
          </div>
          <div class="basis-full">
            <div>{{ i18n.t("donate.selectAmount") }}</div>
          </div>
          <div class="basis-full">
            <div class="input-group mb-3">
              <InputText v-model="amount" label="Amount in $" type="number" />
            </div>
            <small class="form-text">{{ i18n.t("donate.legal", [donationTarget]) }}</small>
            <div>
              <Button type="submit" button-type="primary">
                {{ i18n.t("donate.submit", [monthly ? i18n.t("donate.monthly") : i18n.t("donate.oneTime")]) }}
              </Button>
              <Button @click="on.click">
                {{ i18n.t("general.close") }}
              </Button>
              <img alt="" width="1" height="1" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" />
            </div>
          </div>
        </div>
      </form>
    </template>
    <template #activator="{ on }">
      <Button class="pr-2" v-on="on">
        <IconMdiCurrencyUsd />
        {{ i18n.t("general.donate") }}
      </Button>
    </template>
  </Modal>
</template>
