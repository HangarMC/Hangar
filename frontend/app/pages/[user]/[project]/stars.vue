<script lang="ts" setup>
import type { HangarProject } from "#shared/types/backend";

const route = useRoute("user-project-stars");
const i18n = useI18n();
const { stargazers, stargazersStatus } = useStargazers(() => route.params.project);

const props = defineProps<{
  project?: HangarProject;
}>();

useSeo(
  computed(() => ({
    title: i18n.t("project.stargazers") + " | " + props.project?.name,
    route,
    description: props.project?.description,
    image: props.project?.avatarUrl,
  }))
);
</script>

<template>
  <Card>
    <template #header>
      <PageTitle>{{ i18n.t("project.stargazers") }}</PageTitle>
    </template>

    <Skeleton v-if="stargazersStatus === 'loading'" />
    <div v-else-if="stargazers?.result" class="flex flex-wrap gap-4">
      <div v-for="stargazer in stargazers?.result" :key="stargazer.name">
        <div class="inline-flex items-center space-x-1">
          <UserAvatar size="xs" :username="stargazer.name" :avatar-url="stargazer.avatarUrl" />
          <Link :to="'/' + stargazer.name">{{ stargazer.name }}</Link>
        </div>
      </div>
    </div>
    <div v-else>
      {{ i18n.t("project.noStargazers") }}
    </div>
  </Card>
</template>
