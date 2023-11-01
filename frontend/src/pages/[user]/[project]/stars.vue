<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import { useHead } from "@unhead/vue";
import type { HangarProject } from "hangar-internal";
import Card from "~/components/design/Card.vue";
import PageTitle from "~/components/design/PageTitle.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { useStargazers } from "~/composables/useApiHelper";
import Link from "~/components/design/Link.vue";
import { useSeo } from "~/composables/useSeo";

const route = useRoute();
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

    <div v-if="stargazers?.result?.length > 0" class="flex flex-wrap gap-4">
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
