<script lang="ts" setup>
import type { HangarProject, User } from "#shared/types/backend";

definePageMeta({
  projectPermsRequired: ["ModNotesAndFlags"],
});

const props = defineProps<{
  user?: User;
  project?: HangarProject;
}>();
const i18n = useI18n();
const route = useRoute("user-project-flags");
const { flags } = useProjectFlags(() => route.params.project);

useSeo(computed(() => ({ title: "Flags | " + props.project?.name, route, description: props.project?.description, image: props.project?.avatarUrl })));
</script>

<template>
  <div>
    <div class="mb-5">
      <h1 class="text-3xl font-bold">{{ i18n.t("flagReview.title") }}</h1>
      <p class="mt-1 text-gray-secondary">{{ i18n.t("flags.header") }}</p>
    </div>

    <Flags :flags="flags ?? []" />
  </div>
</template>
