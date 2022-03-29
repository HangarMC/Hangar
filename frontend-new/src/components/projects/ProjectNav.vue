<script setup lang="ts">
import ProjectNavItem from "~/components/projects/ProjectNavItem.vue";
import { computed } from "vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import { useI18n } from "vue-i18n";
import { HangarProject } from "hangar-internal";

const props = defineProps<{
  project: HangarProject;
}>();
const i18n = useI18n();

const slug = computed(() => {
  if (props.project) return props.project.namespace.owner + "/" + props.project.name;
  return "unknown/unknown";
});

function childRoute(route = ""): string {
  return `/${slug.value}${route}`;
}
</script>

<template>
  <nav class="mt-3 mb-4 flex flex-wrap border-b-2 border-neutral-200 dark:border-neutral-800">
    <ProjectNavItem :to="childRoute()">
      {{ i18n.t("project.tabs.docs") }}
    </ProjectNavItem>
    <ProjectNavItem :to="childRoute('/versions')">
      {{ i18n.t("project.tabs.versions") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="project.topicId" :to="childRoute('/discuss')">
      {{ i18n.t("project.tabs.discuss") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)" :to="childRoute('/settings')">
      {{ i18n.t("project.tabs.settings") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.homepage" :href="props.project.settings.homepage">
      {{ i18n.t("project.tabs.homepage") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.issues" :href="props.project.settings.issues">
      {{ i18n.t("project.tabs.issues") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.source" :href="props.project.settings.source">
      {{ i18n.t("project.tabs.source") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.support" :href="props.project.settings.support">
      {{ i18n.t("project.tabs.support") }}
    </ProjectNavItem>
  </nav>
</template>
