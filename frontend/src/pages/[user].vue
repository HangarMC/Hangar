<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import type { Ref } from "vue";
import { computed, ref } from "vue";
import type { User } from "hangar-api";
import { useOrganization, useUser } from "~/composables/useApiHelper";
import { useErrorRedirect } from "~/composables/useErrorRedirect";
import { createError, navigateTo } from "#imports";

const route = useRoute();
const user = ref();
const organization = ref();
const blocking = computed(() => route.name === "user");
if (blocking.value) {
  const u = await useUser(route.params.user as string);
  await cb(u);
} else {
  useUser(route.params.user as string).then(cb);
}

async function cb(u: Ref<User | null>) {
  if (!u || !u.value) {
    throw useErrorRedirect(useRoute(), 404, "Not found");
  } else if (route.params.user !== u.value?.name) {
    const newPath = route.fullPath.replace(route.params.user as string, u.value!.name);
    console.debug("Redirect to " + newPath + " from (" + route.fullPath + ")");
    await navigateTo(newPath);
    throw createError("dummy");
  } else if (u.value?.isOrganization) {
    organization.value = (await useOrganization(route.params.user as string)).value;
  }
  user.value = u.value;
}
</script>

<template>
  <router-view v-if="!blocking || user" v-slot="{ Component }">
    <Suspense>
      <component :is="Component" :user="user" :organization="organization" />
    </Suspense>
  </router-view>
</template>
