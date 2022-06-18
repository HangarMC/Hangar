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
import PageTitle from "~/components/design/PageTitle.vue";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import Button from "~/components/design/Button.vue";
import VisibilityChangerModal from "~/components/modals/VisibilityChangerModal.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const flags = await useFlags().catch((e) => handleRequestError(e, ctx, i18n));
const loading = ref<{ [key: number]: boolean }>({});
if (flags && flags.value) {
  for (const flag of flags.value) {
    loading.value[flag.id] = false;
  }
}

useHead(useSeo(i18n.t("flagReview.title"), null, route, null));

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
  <PageTitle>{{ i18n.t("flagReview.title") }}</PageTitle>
  <template v-if="flags.length > 0">
    <Card v-for="flag in flags" :key="flag.id" class="flex gap-2 items-center">
      <UserAvatar :username="flag.reportedByName"></UserAvatar>
      <div class="flex flex-col flex-grow">
        <h2>
          {{
            i18n.t("flagReview.line1", [flag.reportedByName, `${flag.projectNamespace.owner}/${flag.projectNamespace.slug}`, i18n.d(flag.createdAt, "time")])
          }}
          <router-link :to="`/${flag.projectNamespace.owner}/${flag.projectNamespace.slug}`" target="_blank">
            <icon-mdi-open-in-new class="inline ml-1"></icon-mdi-open-in-new>
          </router-link>
        </h2>
        <small>{{ i18n.t("flagReview.line2", [i18n.t(flag.reason)]) }}</small>
        <small>{{ i18n.t("flagReview.line3", [flag.comment]) }}</small>
      </div>
      <Link fix-href="$util.forumUrl(flag.reportedByName)">{{ i18n.t("flagReview.msgUser") }}</Link>
      <Link fix-href="$util.forumUrl(flag.projectNamespace.owner)">{{ i18n.t("flagReview.msgProjectOwner") }}</Link>
      <VisibilityChangerModal :prop-visibility="flag.projectVisibility" type="project" :post-url="`projects/visibility/${flag.projectId}`" />
      <Button :disabled="loading[flag.id]" @click="resolve(flag)">{{ i18n.t("flagReview.markResolved") }}</Button>
    </Card>
  </template>
  <div v-else>
    {{ i18n.t("flagReview.noFlags") }}
  </div>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["MOD_NOTES_AND_FLAGS"]
</route>
