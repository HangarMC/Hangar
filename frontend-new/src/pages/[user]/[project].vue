<script lang="ts" setup>
import { PropType } from "vue";
import { User } from "hangar-api";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useProject } from "~/composables/useApiHelper";
import { useRoute, useRouter } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useErrorRedirect } from "~/composables/useErrorRedirect";
import ProjectHeader from "~/components/projects/ProjectHeader.vue";

defineProps({
  user: {
    type: Object as PropType<User>,
    required: true,
  },
});

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const project = await useProject(route.params.user as string, route.params.project as string).catch((e) => handleRequestError(e, ctx, i18n));
if (!project) {
  useRouter().push(useErrorRedirect(route, 404, "Not found"));
}
</script>

<template>
  <ProjectHeader :user="user" :project="project"></ProjectHeader>
  <router-view :user="user" :project="project"></router-view>
</template>
