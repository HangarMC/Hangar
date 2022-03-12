<script lang="ts" setup>
import { PropType } from "vue";
import { User } from "hangar-api";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useProject } from "~/composables/useApiHelper";
import { useRoute } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";

defineProps({
  user: {
    type: Object as PropType<User>,
    required: true,
  },
});

const ctx = useContext();
const i18n = useI18n();
const { params } = useRoute();
const project = await useProject(params.user as string, params.project as string).catch((e) => handleRequestError(e, ctx, i18n));
</script>

<template>
  <div>project parent</div>
  <router-view :user="user" :project="project"></router-view>
</template>
