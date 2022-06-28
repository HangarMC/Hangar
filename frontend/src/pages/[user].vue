<script lang="ts" setup>
import { useOrganization, useUser } from "~/composables/useApiHelper";
import { useRoute, useRouter } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useErrorRedirect } from "~/lib/composables/useErrorRedirect";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const user = await useUser(route.params.user as string).catch((e) => handleRequestError(e, ctx, i18n));
let organization = null;
if (!user || !user.value) {
  await useRouter().push(useErrorRedirect(useRoute(), 404, "Not found"));
} else if (user.value?.isOrganization) {
  organization = await useOrganization(route.params.user as string).catch((e) => handleRequestError(e, ctx, i18n));
}
</script>

<template>
  <router-view v-if="user" v-slot="{ Component }">
    <Suspense>
      <component :is="Component" :user="user" :organization="organization" />
      <template #fallback> Loading... </template>
    </Suspense>
  </router-view>
</template>
