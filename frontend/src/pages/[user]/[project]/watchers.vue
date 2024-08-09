<script lang="ts" setup>
import type { HangarProject } from "~/types/backend";

const route = useRoute("user-project-watchers");
const i18n = useI18n();
const { watchers, watchersStatus } = useWatchers(() => route.params.project);

const props = defineProps<{
  project?: HangarProject;
}>();

useHead(useSeo(i18n.t("project.watchers") + " | " + props.project?.name, props.project?.description, route, props.project?.avatarUrl));
</script>

<template>
  <Card>
    <template #header>
      <PageTitle>{{ i18n.t("project.watchers") }}</PageTitle>
    </template>

    <Skeleton v-if="watchersStatus === 'loading'" />
    <div v-else-if="watchers?.result?.length" class="flex flex-wrap gap-4">
      <div v-for="watcher in watchers?.result" :key="watcher.name">
        <div class="inline-flex items-center space-x-1">
          <UserAvatar size="xs" :username="watcher.name" :avatar-url="watcher.avatarUrl" />
          <Link :to="'/' + watcher.name">{{ watcher.name }}</Link>
        </div>
      </div>
    </div>
    <div v-else>
      {{ i18n.t("project.noWatchers") }}
    </div>
  </Card>
</template>
