<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import { useOrganization, useUser } from "~/composables/useApiHelper";
import { useErrorRedirect } from "~/lib/composables/useErrorRedirect";

const i18n = useI18n();
const route = useRoute();
const user = await useUser(route.params.user as string);
let organization = null;
if (!user || !user.value) {
  throw useErrorRedirect(useRoute(), 404, "Not found");
} else if (user.value?.isOrganization) {
  organization = await useOrganization(route.params.user as string);
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
