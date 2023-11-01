<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { HangarProject } from "hangar-internal";
import ProjectNavItem from "~/components/projects/ProjectNavItem.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import { linkout } from "~/composables/useUrlHelper";

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
  <nav class="mt-3 mb-4 flex flex-wrap border-b-2 border-gray-200 dark:border-gray-800">
    <ProjectNavItem :to="childRoute()">
      {{ i18n.t("project.tabs.docs") }}
    </ProjectNavItem>
    <ProjectNavItem :to="childRoute('/versions')">
      {{ i18n.t("project.tabs.versions") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="hasPerms(NamedPermission.EDIT_CHANNELS)" :to="childRoute('/channels')">
      {{ i18n.t("project.tabs.channels") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="project.topicId" :to="childRoute('/discuss')">
      {{ i18n.t("project.tabs.discuss") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)" :to="childRoute('/settings')">
      {{ i18n.t("project.tabs.settings") }}
    </ProjectNavItem>
    <template v-for="section in props.project.settings.links">
      <template v-if="section.type === 'top'">
        <ProjectNavItem v-for="item in section.links" :key="item.id" :href="item.url">
          {{ item.name }}
        </ProjectNavItem>
      </template>
    </template>
  </nav>
</template>
