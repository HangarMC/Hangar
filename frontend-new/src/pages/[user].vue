<script lang="ts" setup>
import { useUser } from "~/composables/useApiHelper";
import { useRoute, useRouter } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useErrorRedirect } from "~/composables/useErrorRedirect";

const ctx = useContext();
const i18n = useI18n();
const user = await useUser(useRoute().params.user as string).catch((e) => handleRequestError(e, ctx, i18n));
if (!user) {
  // TODO check if user is an org here
  await useRouter().push(useErrorRedirect(useRoute(), 404, "Not found"));
}
</script>

<template>
  <router-view v-if="user" v-slot="{ Component }">
    <component :is="Component" :user="user" :dum="user" />
  </router-view>
</template>
