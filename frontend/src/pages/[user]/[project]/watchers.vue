<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import { useHead } from "@unhead/vue";
import type { HangarProject } from "hangar-internal";
import Card from "~/components/design/Card.vue";
import PageTitle from "~/components/design/PageTitle.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { useWatchers } from "~/composables/useApiHelper";
import Link from "~/components/design/Link.vue";
import { useSeo } from "~/composables/useSeo";

const route = useRoute();
const i18n = useI18n();
const watchers = await useWatchers(route.params.project as string);

const props = defineProps<{
  project: HangarProject;
}>();

useHead(useSeo(i18n.t("project.watchers") + " | " + props.project.name, props.project.description, route, props.project.avatarUrl));
</script>

<template>
  <Card>
    <template #header>
      <PageTitle>{{ i18n.t("project.watchers") }}</PageTitle>
    </template>

    <div v-if="watchers?.result?.length > 0" class="flex flex-wrap gap-4">
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
