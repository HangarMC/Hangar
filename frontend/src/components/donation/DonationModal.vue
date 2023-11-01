<script lang="ts" setup>
import { onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { useNotificationStore } from "~/store/notification";
import { useAuthStore } from "~/store/auth";
import Button from "~/components/design/Button.vue";
import { useConfig } from "~/composables/useConfig";

const i18n = useI18n();
const notifications = useNotificationStore();
const authStore = useAuthStore();

const props = defineProps<{
  donationTarget: string;
  donationSubject: string;
}>();

let button: HTMLElement | null = null;

onMounted(() => {
  if (!import.meta.env.SSR) {
    // load script
    const script = document.createElement("script");
    script.setAttribute("src", "https://www.paypalobjects.com/donate/sdk/donate-sdk.js");
    const config = useConfig();
    script.onload = () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore

      PayPal.Donation.Button({
        env: config.paypalEnv,
        business: props.donationSubject,
        image: {
          src: "https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif",
          title: "PayPal - The safer, easier way to pay online!",
          alt: "Donate with PayPal button",
        },
        onComplete: function (params: Record<string, string>) {
          notifications.success(i18n.t("donate.success", [params.amt, params.cc, props.donationTarget]), true, -1);
          console.log("donate success", params);
        },
        item_name: "Hangar: Donation to " + props.donationTarget,
        bn: "Hangar_Donate_" + props.donationTarget + "US",
        notify_url: config.paypalIpn,
        custom: authStore.user?.id || "anonymous",
      }).render("#paypal-donate-button-container");

      button = document.querySelector("#paypal-donate-button-container img") as HTMLElement | null;
      if (button) {
        button.style.display = "none";
      }
    };
    document.head.append(script);
  }
});

function click() {
  if (button) {
    button.click();
  } else {
    notifications.error(i18n.t("donate.buttonError"));
  }
}
</script>

<template>
  <div id="paypal-donate-button-container"></div>
  <Button size="medium" @click="click">{{ i18n.t("general.donate") }}</Button>
</template>
