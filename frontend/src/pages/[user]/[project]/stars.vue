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
import { useStargazers } from "~/composables/useApiHelper";
import Link from "~/lib/components/design/Link.vue";
import { useSeo } from "~/composables/useSeo";

const route = useRoute();
const i18n = useI18n();
const stargazers = await useStargazers(route.params.user as string, route.params.project as string).catch<any>((e) => handleRequestError(e));

const props = defineProps<{
  project: HangarProject;
}>();

useHead(
  useSeo(
    i18n.t("project.stargazers") + " | " + props.project.name,
    props.project.description,
    route,
    projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)
  )
);
</script>

<template>
  <Card>
    <template #header>
      <PageTitle>{{ i18n.t("project.stargazers") }}</PageTitle>
    </template>

    <div v-if="stargazers.result && stargazers.result.length > 0" class="flex flex-wrap gap-4">
      <div v-for="stargazer in stargazers.result" :key="stargazer.name">
        <UserAvatar size="xs" :username="stargazer.name" :avatar-url="avatarUrl(stargazer.name)" />
        <Link :to="'/' + stargazer.name">{{ stargazer.name }}</Link>
      </div>
    </div>
    <Alert v-else type="warning">
      {{ i18n.t("project.noStargazers") }}
    </Alert>
  </Card>
</template>
