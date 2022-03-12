<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useAuthors } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";

const ctx = useContext();
const i18n = useI18n();
const { params } = useRoute();
const authors = await useAuthors().catch((e) => handleRequestError(e, ctx, i18n));
</script>

<template>
  <h1>Authors</h1>
  <div>{{ authors }}</div>
</template>
