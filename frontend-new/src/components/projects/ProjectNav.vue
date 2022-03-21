<script setup lang="ts">
import ProjectNavItem from "~/components/projects/ProjectNavItem.vue";
import { Project } from "hangar-api";
import { computed } from "vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";

const props = defineProps<{
  project: Project;
}>();

const slug = computed(() => {
  if (props.project) return props.project.namespace.owner + "/" + props.project.name;
  return "unknown/unknown";
});

function childRoute(route = ""): string {
  return `/${slug.value}${route}`;
}
</script>

<template>
  <nav class="pt-4 pb-2 flex flex-wrap">
    <!-- TODO: vue-i18n -->
    <ProjectNavItem :to="childRoute()">Home</ProjectNavItem>
    <ProjectNavItem :to="childRoute('/versions')">Versions</ProjectNavItem>
    <ProjectNavItem v-if="project.topicId" :to="childRoute('/discuss')">Discuss</ProjectNavItem>
    <ProjectNavItem v-if="hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)" :to="childRoute('/settings')">Settings</ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.homepage" :href="props.project.settings.homepage">Website</ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.issues" :href="props.project.settings.issues">Issues</ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.source" :href="props.project.settings.source">Source</ProjectNavItem>
    <ProjectNavItem v-if="props.project.settings.support" :href="props.project.settings.support">Support</ProjectNavItem>
  </nav>
</template>
