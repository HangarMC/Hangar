<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { HangarProject } from "hangar-internal";
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
    <ProjectNavItem v-if="props.project.settings.homepage" :href="linkout(props.project.settings.homepage)">
      {{ i18n.t("project.tabs.homepage") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.issues" :href="linkout(props.project.settings.issues)">
      {{ i18n.t("project.tabs.issues") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.source" :href="linkout(props.project.settings.source)">
      {{ i18n.t("project.tabs.source") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.support" :href="linkout(props.project.settings.support)">
      {{ i18n.t("project.tabs.support") }}
    </ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.wiki" :href="linkout(props.project.settings.wiki)">
      {{ i18n.t("project.tabs.wiki") }}
    </ProjectNavItem>
  </nav>
</template>
