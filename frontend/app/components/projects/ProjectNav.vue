<script setup lang="ts">
import { NamedPermission } from "#shared/types/backend";
import type { HangarProject, HangarProjectPage } from "#shared/types/backend";

const props = defineProps<{
  project?: HangarProject;
}>();
const i18n = useI18n();
const route = useRoute();
const inSettings = computed(() => route.path.includes("/settings"));
const activeProjectTab = computed(() => route.path.split("/")[3] || "overview");
const activeSettingsTab = computed(() => {
  const settingsIndex = route.path.split("/").indexOf("settings");
  return route.path.split("/")[settingsIndex + 1] || "general";
});
const settingsTabs = computed(() => {
  const tabs = [
    { value: "general", title: i18n.t("project.settings.tabs.general") },
    { value: "links", title: i18n.t("project.settings.tabs.links") },
    { value: "banners", title: i18n.t("project.settings.tabs.banners") },
  ];
  if (hasPerms(NamedPermission.IsSubjectOwner) || hasPerms(NamedPermission.DeleteProject) || hasPerms(NamedPermission.HardDeleteProject)) {
    tabs.push({ value: "management", title: i18n.t("project.settings.tabs.management") });
  }
  return tabs;
});

const slug = computed(() => {
  if (props.project) return props.project.namespace.owner + "/" + props.project.namespace.slug;
  return "unknown/unknown";
});

function childRoute(route = ""): string {
  return `/${slug.value}${route}`;
}

function flattenPages(pages: HangarProjectPage[] = []): HangarProjectPage[] {
  return pages.flatMap((page) => [page, ...flattenPages(page.children)]);
}

const projectPages = computed(() => flattenPages(props.project?.pages).filter((page) => !page.home));
const externalLinks = computed(() => props.project?.settings?.links.flatMap((section) => section.links) ?? []);
const tableOfContents = computed(
  () =>
    parseMarkdown(props.project?.mainPage?.contents)
      .headings?.map((heading) => ({
        ...heading,
        text: stripAllHtml(heading.text),
      }))
      .filter((heading) => heading.text.trim().length > 0) ?? []
);

const internalGroup = ref<HTMLElement>();
const externalGroup = ref<HTMLElement>();
const internalFade = reactive({ left: false, right: false });
const externalFade = reactive({ left: false, right: false });

function updateFade(element: HTMLElement | undefined, fade: { left: boolean; right: boolean }) {
  if (!element) return;
  fade.left = element.scrollLeft > 1;
  fade.right = element.scrollLeft + element.clientWidth < element.scrollWidth - 1;
}

function updateFades() {
  updateFade(internalGroup.value, internalFade);
  updateFade(externalGroup.value, externalFade);
}

onMounted(() => {
  const resizeObserver = new ResizeObserver(updateFades);
  if (internalGroup.value) resizeObserver.observe(internalGroup.value);
  if (externalGroup.value) resizeObserver.observe(externalGroup.value);
  nextTick(updateFades);
  onBeforeUnmount(() => resizeObserver.disconnect());
});

watch([projectPages, externalLinks], () => nextTick(updateFades));
</script>

<template>
  <nav class="my-4 flex min-w-0 items-center gap-2 overflow-hidden">
    <Transition name="fade">
      <DropdownButton
        v-if="tableOfContents.length > 0"
        :button-arrow="false"
        button-size="medium"
        button-type="transparent"
        button-class="!h-11 !w-11 !min-w-11 !p-0"
        placement="bottom-start"
      >
        <template #button-label>
          <IconMdiFormatListBulleted class="text-lg" />
        </template>
        <template #default="{ close }">
          <div class="flex max-h-lg min-w-56 max-w-sm flex-col gap-1 overflow-y-auto px-2 py-1.5">
            <a
              v-for="heading in tableOfContents"
              :key="heading.id"
              class="flex min-w-0 items-center rounded-lg border border-transparent px-3 py-2 font-semibold decoration-none transition-all duration-250 hover:border-gray-300 hover:bg-gray-100 dark:hover:border-gray-700 dark:hover:bg-gray-800"
              :class="'toc-' + heading.level"
              :href="`#${heading.id}`"
              :title="heading.text"
              @click="close"
            >
              <span class="truncate">{{ heading.text }}</span>
            </a>
          </div>
        </template>
      </DropdownButton>
    </Transition>

    <div
      class="nav-group background-default relative min-w-0 max-w-3/5 flex-[0_0_auto] overflow-hidden rounded-lg border dark:border-gray-800"
      :class="internalFade"
    >
      <div
        ref="internalGroup"
        class="flex max-w-full items-center gap-1 overflow-x-auto whitespace-nowrap p-1"
        @scroll="updateFade(internalGroup, internalFade)"
      >
        <ProjectNavItem :to="childRoute()" title="Overview" :active="activeProjectTab === 'overview'" compact> Overview </ProjectNavItem>
        <ProjectNavItem :to="childRoute('/versions')" :title="i18n.t('project.tabs.versions')" :active="activeProjectTab === 'versions'" compact>
          {{ i18n.t("project.tabs.versions") }}
        </ProjectNavItem>
        <ProjectNavItem
          v-for="page in projectPages"
          :key="page.id"
          :to="childRoute(`/pages/${page.slug}`)"
          :title="page.name"
          :active="activeProjectTab === 'pages' && route.path.split('/')[4] === page.slug"
          compact
        >
          {{ page.name }}
        </ProjectNavItem>
        <ProjectNavItem
          v-if="hasPerms(NamedPermission.EditChannels)"
          :to="childRoute('/channels')"
          :title="i18n.t('project.tabs.channels')"
          :active="activeProjectTab === 'channels'"
          compact
        >
          {{ i18n.t("project.tabs.channels") }}
        </ProjectNavItem>
        <ProjectNavItem
          v-if="hasPerms(NamedPermission.EditSubjectSettings)"
          :to="childRoute('/settings')"
          :title="i18n.t('project.tabs.settings')"
          :active="inSettings"
          compact
        >
          {{ i18n.t("project.tabs.settings") }}
        </ProjectNavItem>
      </div>
    </div>

    <div
      v-if="inSettings"
      class="nav-group background-default relative min-w-0 max-w-full flex-[0_1_auto] overflow-hidden rounded-lg border dark:border-gray-800"
    >
      <div class="flex max-w-full items-center gap-1 overflow-x-auto whitespace-nowrap p-1">
        <ProjectNavItem
          v-for="tab in settingsTabs"
          :key="tab.value"
          :to="childRoute(`/settings/${tab.value}`)"
          :title="tab.title"
          :active="activeSettingsTab === tab.value"
          compact
        >
          {{ tab.title }}
        </ProjectNavItem>
      </div>
    </div>

    <div
      v-if="!inSettings && externalLinks.length > 0"
      class="nav-group background-default relative min-w-0 max-w-full flex-[0_1_auto] overflow-hidden rounded-lg border dark:border-gray-800"
      :class="externalFade"
    >
      <div
        ref="externalGroup"
        class="flex max-w-full items-center gap-1 overflow-x-auto whitespace-nowrap p-1"
        @scroll="updateFade(externalGroup, externalFade)"
      >
        <ProjectNavItem v-for="item in externalLinks" :key="item.id" :href="item.url" :title="item.name">
          {{ item.name }}
        </ProjectNavItem>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.nav-group::before,
.nav-group::after {
  position: absolute;
  z-index: 2;
  top: 0;
  bottom: 0;
  width: 2.25rem;
  pointer-events: none;
  content: "";
  opacity: 0;
  transition: opacity 150ms ease;
}

.nav-group::before {
  left: 0;
  border-radius: 0.75rem 0 0 0.75rem;
  background: linear-gradient(to right, var(--charcoal-600) 0, var(--charcoal-600) 0.5rem, transparent 100%);
}

.nav-group::after {
  right: 0;
  border-radius: 0 0.75rem 0.75rem 0;
  background: linear-gradient(to left, var(--charcoal-600) 0, var(--charcoal-600) 0.5rem, transparent 100%);
}

.nav-group.left::before,
.nav-group.right::after {
  opacity: 1;
}

:global(.light) .nav-group::before {
  background: linear-gradient(to right, var(--gray-50) 0, var(--gray-50) 0.5rem, transparent 100%);
}

:global(.light) .nav-group::after {
  background: linear-gradient(to left, var(--gray-50) 0, var(--gray-50) 0.5rem, transparent 100%);
}

.toc-2 {
  margin-left: 0.5rem;
  padding-left: 1rem;
}

.toc-3 {
  margin-left: 1rem;
  padding-left: 1rem;
}

.toc-4 {
  margin-left: 1.5rem;
  padding-left: 1rem;
}
</style>
