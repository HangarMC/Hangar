<script lang="ts" setup>
import { useRoute, useRouter } from "vue-router";
import { useHead } from "@unhead/vue";
import { useI18n } from "vue-i18n";
import { useLocalStorage } from "@vueuse/core";
import { computed } from "vue";
import { useSeo } from "~/composables/useSeo";
import Card from "~/components/design/Card.vue";
import Button from "~/components/design/Button.vue";
import Link from "~/components/design/Link.vue";

const route = useRoute();
const remoteUrl = Array.isArray(route.query.remoteUrl) ? route.query.remoteUrl[0] : route.query.remoteUrl;
const i18n = useI18n();
const router = useRouter();

const trustedHosts = useLocalStorage("trustedHosts", [] as string[]);
const host = computed<string | null>(() => {
  if (remoteUrl) {
    try {
      return new URL(remoteUrl as string).host;
    } catch {
      return null;
    }
  } else {
    return null;
  }
});

if (host.value && trustedHosts.value.includes(host.value!)) {
  go();
}

function trust() {
  if (host.value) {
    trustedHosts.value.push(host.value!);
  }
  go();
}

function go() {
  location.href = remoteUrl as string;
}

async function back() {
  if (!window.history.state.back) {
    window.close(); // close tab
  } else {
    await router.back();
  }
}

useHead(useSeo(i18n.t("linkout.title"), null, route, null));
</script>
<template>
  <Card>
    <template #header>
      <h1>{{ i18n.t("linkout.title") }}</h1>
    </template>
    {{ i18n.t("linkout.text", [remoteUrl]) }}
    <template #footer>
      <Link @click="trust">
        <Button size="medium">{{ i18n.t("linkout.trust") }}</Button>
      </Link>
      <Link class="ml-2" :href="remoteUrl" target="_self" rel="noopener noreferrer">
        <Button size="medium">{{ i18n.t("linkout.continue") }}</Button>
      </Link>
      <Button button-type="secondary" size="medium" class="ml-2" @click="back">{{ i18n.t("linkout.abort") }}</Button>
    </template>
  </Card>
</template>
