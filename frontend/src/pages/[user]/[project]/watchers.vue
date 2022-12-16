<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import { useHead } from "@vueuse/head";
import { HangarProject } from "hangar-internal";
import { handleRequestError } from "~/composables/useErrorHandling";
import Card from "~/lib/components/design/Card.vue";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { avatarUrl, projectIconUrl } from "~/composables/useUrlHelper";
import Alert from "~/lib/components/design/Alert.vue";
import { useWatchers } from "~/composables/useApiHelper";
import Link from "~/lib/components/design/Link.vue";
import { useSeo } from "~/composables/useSeo";

const route = useRoute();
const i18n = useI18n();
const watchers = await useWatchers(route.params.user as string, route.params.project as string).catch<any>((e) => handleRequestError(e, i18n));

const props = defineProps<{
  project: HangarProject;
}>();

useHead(
  useSeo(
    i18n.t("project.watchers") + " | " + props.project.name,
    props.project.description,
    route,
    projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)
  )
);
</script>

<template>
  <Card>
    <template #header>
      <PageTitle>{{ i18n.t("project.watchers") }}</PageTitle>
    </template>

    <div v-if="watchers.result && watchers.result.length > 0" class="flex flex-wrap gap-4">
      <div v-for="watcher in watchers.result" :key="watcher.name">
        <UserAvatar size="xs" :username="watcher.name" :avatar-url="avatarUrl(watcher.name)" />
        <Link :to="'/' + watcher.name">{{ watcher.name }}</Link>
      </div>
    </div>
    <Alert v-else type="warning">
      {{ i18n.t("project.noWatchers") }}
    </Alert>
  </Card>
</template>
