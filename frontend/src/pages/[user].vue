<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import { Ref } from "vue";
import { Organization } from "hangar-internal";
import { useOrganization, useUser } from "~/composables/useApiHelper";
import { useErrorRedirect } from "~/lib/composables/useErrorRedirect";
import { createError, navigateTo } from "#imports";
import Delayed from "~/lib/components/design/Delayed.vue";

const i18n = useI18n();
const route = useRoute();
const user = await useUser(route.params.user as string);
let organization: Ref<Organization | null> | undefined;
if (!user || !user.value) {
  throw useErrorRedirect(useRoute(), 404, "Not found");
} else if (route.params.user !== user.value?.name) {
  const newPath = route.fullPath.replace(route.params.user as string, user.value?.name);
  console.debug("Redirect to " + newPath + " from (" + route.fullPath + ")");
  await navigateTo(newPath);
  throw createError("dummy");
} else if (user.value?.isOrganization) {
  organization = await useOrganization(route.params.user as string);
}
</script>

<template>
  <router-view v-if="user" v-slot="{ Component }">
    <Suspense>
      <component :is="Component" :user="user" :organization="organization" />
      <template #fallback><Delayed> Loading... </Delayed></template>
    </Suspense>
  </router-view>
</template>
