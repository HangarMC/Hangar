<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";
import Card from "~/lib/components/design/Card.vue";
import { useI18n } from "vue-i18n";
import Button from "~/lib/components/design/Button.vue";
import Link from "~/lib/components/design/Link.vue";
import { useLocalStorage } from "@vueuse/core";
import { computed } from "vue";

const route = useRoute();
const remoteUrl = route.query.remoteUrl;
const i18n = useI18n();

const trustedHosts = useLocalStorage("trustedHosts", [] as string[]);
const host = computed<string | null>(() => (remoteUrl ? new URL(remoteUrl as string).host : null));

if (host.value && trustedHosts.value.includes(host.value)) {
  go();
}

function trust() {
  if (host.value) {
    trustedHosts.value.push(host.value);
  }
  go();
}

function go() {
  location.href = remoteUrl as string;
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
      <Button button-type="secondary" size="medium" class="ml-2" @click="$router.back()">{{ i18n.t("linkout.abort") }}</Button>
    </template>
  </Card>
</template>
