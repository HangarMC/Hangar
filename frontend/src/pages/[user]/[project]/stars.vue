<script lang="ts" setup>
import type { HangarProject } from "hangar-internal";

const route = useRoute<"user-project-stars">();
const i18n = useI18n();
const stargazers = await useStargazers(route.params.project as string);

const props = defineProps<{
  project: HangarProject;
}>();

useHead(useSeo(i18n.t("project.stargazers") + " | " + props.project.name, props.project.description, route, props.project.avatarUrl));
</script>

<template>
  <Card>
    <template #header>
      <PageTitle>{{ i18n.t("project.stargazers") }}</PageTitle>
    </template>

    <div v-if="stargazers?.result" class="flex flex-wrap gap-4">
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
