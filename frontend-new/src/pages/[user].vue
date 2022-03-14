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
  useRouter().push(useErrorRedirect(useRoute(), 404, "Not found"));
}
</script>

<template>
  <div>user parent</div>
  <router-view :user="user"></router-view>
</template>
