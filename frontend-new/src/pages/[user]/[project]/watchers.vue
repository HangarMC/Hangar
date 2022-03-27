<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import { useContext } from "vite-ssr/vue";
import { handleRequestError } from "~/composables/useErrorHandling";
import Card from "~/components/design/Card.vue";
import PageTitle from "~/components/design/PageTitle.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { avatarUrl, projectIconUrl } from "~/composables/useUrlHelper";
import Alert from "~/components/design/Alert.vue";
import { useWatchers } from "~/composables/useApiHelper";
import Link from "~/components/design/Link.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { HangarProject } from "hangar-internal";

const route = useRoute();
const i18n = useI18n();
const ctx = useContext();
const watchers = await useWatchers(route.params.user as string, route.params.project as string).catch<any>((e) => handleRequestError(e, ctx, i18n));

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
    <Alert v-else>
      {{ i18n.t("project.noWatchers") }}
    </Alert>
  </Card>
</template>
