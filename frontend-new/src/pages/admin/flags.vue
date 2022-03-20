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
import PageTitle from "~/components/design/PageTitle.vue";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import Button from "~/components/design/Button.vue";

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
  <PageTitle>{{ i18n.t("flagReview.title") }}</PageTitle>
  <template v-if="flags.length > 0">
    <Card v-for="flag in flags" :key="flag.id" class="flex gap-2 items-center">
      <UserAvatar :username="flag.reportedByName"></UserAvatar>
      <div class="flex flex-col flex-grow">
        <h2>
          <!-- this is client only, as the date format causes hydration mismatches... -->
          <!-- I think the proper fix is using vue-i18n for date format -->
          <ClientOnly
            >{{
              i18n.t("flagReview.line1", [flag.reportedByName, `${flag.projectNamespace.owner}/${flag.projectNamespace.slug}`, prettyDateTime(flag.createdAt)])
            }}
          </ClientOnly>
          <router-link :to="`/${flag.projectNamespace.owner}/${flag.projectNamespace.slug}`" target="_blank">
            <icon-mdi-open-in-new class="inline ml-1"></icon-mdi-open-in-new>
          </router-link>
        </h2>
        <small>{{ i18n.t("flagReview.line2", [i18n.t(flag.reason)]) }}</small>
        <small>{{ i18n.t("flagReview.line3", [flag.comment]) }}</small>
      </div>
      <Link fix-href="$util.forumUrl(flag.reportedByName)">{{ i18n.t("flagReview.msgUser") }}</Link>
      <Link fix-href="$util.forumUrl(flag.projectNamespace.owner)">{{ i18n.t("flagReview.msgProjectOwner") }}</Link>
      <!-- todo modal for visibility change -->
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
