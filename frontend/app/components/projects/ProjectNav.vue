<script setup lang="ts">
import { NamedPermission } from "#shared/types/backend";
import type { HangarProject } from "#shared/types/backend";

const props = defineProps<{
  project?: HangarProject;
}>();
const i18n = useI18n();
const route = useRoute();

const slug = computed(() => {
  if (props.project) return props.project.namespace.owner + "/" + props.project.name;
  return "unknown/unknown";
});

function childRoute(route = ""): string {
  return `/${slug.value}${route}`;
}

const path = computed(() => (route.path.endsWith("/") ? route.path.slice(0, Math.max(0, route.path.length - 1)) : route.path));

function isActive(child = ""): boolean {
  const target = childRoute(child);
  return path.value === target || path.value.startsWith(target + "/");
}

const externalLinks = computed(() => props.project?.settings?.links?.filter((section) => section.type === "top").flatMap((section) => section.links) ?? []);
</script>

<template>
  <nav class="mt-3 mb-4 flex flex-wrap items-end justify-between gap-x-4 gap-y-1 border-b-2 border-gray-200 dark:border-gray-800">
    <div class="flex flex-wrap">
      <ProjectNavItem :to="childRoute()" :active="path === childRoute() || isActive('/pages')" :label="i18n.t('project.tabs.docs')">
        <IconMdiTextBoxOutline class="flex-shrink-0" />
      </ProjectNavItem>
      <ProjectNavItem :to="childRoute('/versions')" :active="isActive('/versions')" :label="i18n.t('project.tabs.versions')">
        <IconMdiArchiveOutline class="flex-shrink-0" />
      </ProjectNavItem>
      <ProjectNavItem
        v-if="hasPerms(NamedPermission.EditSubjectSettings)"
        :to="childRoute('/settings')"
        :active="isActive('/settings')"
        :label="i18n.t('project.tabs.settings')"
      >
        <IconMdiCogOutline class="flex-shrink-0" />
      </ProjectNavItem>
    </div>
    <div v-if="externalLinks.length > 0" class="flex flex-wrap items-center pb-0.5">
      <ProjectNavLink v-for="item in externalLinks" :key="item.id" :href="item.url" :label="item.name" />
    </div>
  </nav>
</template>
