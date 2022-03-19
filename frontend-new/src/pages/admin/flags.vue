<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useFlags } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { ref } from "vue";
import { Flag } from "hangar-internal";
import { useInternalApi } from "~/composables/useApi";
import UserAvatar from "~/components/UserAvatar.vue";
import { prettyDateTime } from "~/composables/useDate";

const ctx = useContext();
const i18n = useI18n();
const { params } = useRoute();
const flags = await useFlags().catch((e) => handleRequestError(e, ctx, i18n));
const loading = ref<{ [key: number]: boolean }>({});
if (flags && flags.value) {
  for (const flag of flags.value) {
    loading.value[flag.id] = false;
  }
}

function resolve(flag: Flag) {
  loading.value[flag.id] = true;
  useInternalApi<Flag[]>(`flags/${flag.id}/resolve/true`, false, "POST")
    .catch<any>((e) => handleRequestError(e, ctx, i18n))
    .then(async () => {
      if (flags && flags.value) {
        const newFlags = await useInternalApi<Flag[]>("flags/", false).catch((e) => handleRequestError(e, ctx, i18n));
        if (newFlags) {
          flags.value = newFlags;
        }
      }
    })
    .finally(() => {
      loading.value[flag.id] = false;
    });
}
</script>

<template>
  <h1>{{ i18n.t("flagReview.title") }}</h1>
  <template v-if="flags.length > 0">
    <div v-for="flag in flags" :key="flag.id">
      <UserAvatar :username="flag.reportedByName"></UserAvatar>
      <h2>
        <!-- this is client only, as the date format causes hydration mismatches... -->
        <!-- I think the proper fix is using vue-i18n for date format -->
        <ClientOnly>{{
          i18n.t("flagReview.line1", [flag.reportedByName, `${flag.projectNamespace.owner}/${flag.projectNamespace.slug}`, prettyDateTime(flag.createdAt)])
        }}</ClientOnly>
        <router-link :to="`/${flag.projectNamespace.owner}/${flag.projectNamespace.slug}`" target="_blank">open in new </router-link>
      </h2>
      <h3>{{ i18n.t("flagReview.line2", [i18n.t(flag.reason)]) }}</h3>
      <h3>{{ i18n.t("flagReview.line3", [flag.comment]) }}</h3>
      <a fixHref="$util.forumUrl(flag.reportedByName)">{{ i18n.t("flagReview.msgUser") }}</a>
      <a fixHref="$util.forumUrl(flag.projectNamespace.owner)">{{ i18n.t("flagReview.msgProjectOwner") }}</a>
      <!-- todo modal for visibility change -->
      <button :disabled="loading[flag.id]" @click="resolve(flag)">{{ i18n.t("flagReview.markResolved") }}</button>
    </div>
  </template>
  <div v-else>
    {{ i18n.t("flagReview.noFlags") }}
  </div>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["MOD_NOTES_AND_FLAGS"]
</route>
