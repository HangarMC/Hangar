<script lang="ts" setup>
import { upperFirst } from "scule";
import { NamedPermission } from "#shared/types/backend";
import type { HangarProject, PinnedVersion, User, Platform } from "#shared/types/backend";

const props = defineProps<{
  user?: User;
  project?: HangarProject;
}>();

const config = useRuntimeConfig();
const i18n = useI18n();
const route = useRoute("user-project-pages-page");

const sponsors = ref(props.project?.settings?.sponsors);
const editingSponsors = ref(false);

function saveSponsors(content: string) {
  useInternalApi(`projects/project/${props.project?.namespace?.slug}/sponsors`, "post", {
    content,
  })
    .then(() => {
      sponsors.value = content;
      editingSponsors.value = false;
    })
    .catch((err) => handleRequestError(err, "page.new.error.save"));
}

function createPinnedVersionUrl(version: PinnedVersion): string {
  return `/${props.project?.namespace?.owner}/${props.project?.namespace?.slug}/versions/${version.name}`;
}

const platform = computed(() =>
  upperFirst(Object.keys(props.project?.pinnedVersions?.[0]?.platformDependenciesFormatted || { Minecraft: "dum" })?.[0]?.toLowerCase() || "Minecraft")
);

useSeo(
  computed(() => ({
    title: `${props.project?.name} - ${platform.value} Plugin`,
    route,
    description: `${props.project?.description} - Download the ${platform.value} Plugin ${props.project?.name} by ${props.project?.namespace?.owner} on Hangar`,
    image: props.project?.avatarUrl,
    additionalScripts: [
      {
        type: "application/ld+json",
        textContent: JSON.stringify({
          "@context": "https://schema.org",
          "@type": "WebContent",
          author: {
            "@type": "Person",
            name: props.project?.namespace.owner,
            url: config.public.host + "/" + props.project?.namespace?.owner,
          },
          name: props.project?.name,
          datePublished: props.project?.createdAt,
          dateCreated: props.project?.createdAt,
          url: config.public.host + route.path,
        }),
        key: "project",
      },
    ],
  }))
);
</script>

<template>
  <div class="grid grid-cols-1 items-start gap-4 lg:grid-cols-[minmax(0,1fr)_300px] xl:grid-cols-[minmax(0,1fr)_320px]">
    <section class="min-w-0 overflow-auto">
      <ProjectPageMarkdown v-slot="{ editingPage, changeEditingPage, savePage }" :project="props.project" :page="props.project?.mainPage" main-page>
        <Card v-if="project?.mainPage?.contents" class="!p-0 pb-0 overflow-clip overflow-hidden">
          <ClientOnly v-if="hasPerms(NamedPermission.EditPage)">
            <MarkdownEditor
              :editing="editingPage"
              :raw="project?.mainPage.contents"
              :deletable="false"
              :saveable="true"
              :cancellable="true"
              :maxlength="useBackendData.validations.project.pageContent?.max"
              :rules="[required()]"
              @update:editing="changeEditingPage"
              @save="savePage"
            />
            <template #fallback>
              <Markdown :raw="project?.mainPage.contents" />
            </template>
          </ClientOnly>
          <!--We have to blow up v-model:editing into :editing and @update:editing as we are inside a scope--->
          <Markdown v-else :raw="project?.mainPage.contents" />
        </Card>
      </ProjectPageMarkdown>
      <Card v-if="sponsors || hasPerms(NamedPermission.EditSubjectSettings)" class="mt-4 pb-0 overflow-clip overflow-visible">
        <ClientOnly v-if="hasPerms(NamedPermission.EditSubjectSettings)">
          <MarkdownEditor
            v-model:editing="editingSponsors"
            :raw="sponsors"
            :deletable="false"
            :saveable="true"
            :cancellable="true"
            :maxlength="useBackendData.validations.project.sponsorsContent?.max"
            max-height="200px"
            @save="saveSponsors"
          >
            <template #title>
              <div class="inline-flex items-center mt-2 gap-1.5">
                <h2 class="ml-4 text-2xl">{{ i18n.t("project.sponsors") }}</h2>
                <Tooltip class="overflow-visible">
                  <template #content> {{ i18n.t("project.sponsorsTooltip") }}</template>
                  <IconMdiInformation class="mt-1 text-xl" />
                </Tooltip>
              </div>
            </template>
          </MarkdownEditor>
          <template #fallback>
            <h2 class="mt-3 ml-4 text-2xl">{{ i18n.t("project.sponsors") }}</h2>
            <Markdown :raw="sponsors" />
          </template>
        </ClientOnly>
        <template v-else>
          <h2 class="mt-3 ml-4 text-2xl">{{ i18n.t("project.sponsors") }}</h2>
          <Markdown :raw="sponsors" class="pt-0" />
        </template>
      </Card>
      <Alert v-if="hasPerms(NamedPermission.EditSubjectSettings)" type="neutral" class="mt-4">
        <div>
          {{ i18n.t("project.bannersInfo") }}&nbsp;
          <Link :to="'/' + project?.namespace?.owner + '/' + project?.namespace?.slug + '/settings/banners'">
            {{ i18n.t("project.bannersInfoSettings") }}
          </Link>
        </div>
      </Alert>
    </section>
    <aside class="space-y-4 self-start lg:sticky lg:top-4">
      <ProjectInfo :project="project" />
      <Card v-if="project?.pinnedVersions?.length" class="!p-0 overflow-hidden">
        <template #header>
          <div class="flex items-center gap-2 px-4 pt-3.5 pb-1">
            <h2>{{ i18n.t("project.pinnedVersions") }}</h2>
          </div>
        </template>
        <ul class="flex flex-col gap-2 px-3 pt-1 pb-3">
          <li
            v-for="(version, index) in project?.pinnedVersions"
            :key="`${index}-${version.name}`"
            class="overflow-hidden rounded-lg border border-gray-200 bg-gray-100/60 transition-colors hover:border-gray-300 dark:border-gray-800 dark:bg-charcoal-500/60 dark:hover:border-gray-700"
          >
            <div class="flex min-w-0 items-center gap-3 p-3">
              <NuxtLink :to="createPinnedVersionUrl(version)" class="min-w-0 flex-grow">
                <div class="mb-1 flex items-center gap-2">
                  <Tag :name="version.channel.name" :color="{ background: version.channel.color }" :tooltip="version.channel.description" />
                </div>
                <div class="truncate text-lg font-semibold">{{ version.name }}</div>
              </NuxtLink>
              <DownloadButton v-if="project" :project="project" :pinned-version="version" small :show-versions="false" />
            </div>
            <NuxtLink :to="createPinnedVersionUrl(version)" class="flex flex-wrap gap-1.5 border-t border-gray-200 px-3 py-2 dark:border-gray-700">
              <span
                v-for="(v, p) in version.platformDependenciesFormatted"
                :key="p"
                class="inline-flex min-w-0 items-center gap-1.5 rounded-md bg-gray-50 px-2 py-1 text-xs dark:bg-charcoal-600"
              >
                <PlatformLogo :platform="p as Platform" :size="16" class="flex-shrink-0" />
                <span class="truncate">{{ v.join(", ") }}</span>
              </span>
            </NuxtLink>
          </li>
        </ul>
        <Skeleton v-if="!project" />
      </Card>

      <template v-for="section in project?.settings?.links">
        <Card v-if="section.type === 'sidebar'" :key="section.id" class="!p-0 overflow-hidden">
          <template #header>
            <div class="flex items-center gap-2 px-4 pt-3.5 pb-1">
              <h2 class="min-w-0 truncate">{{ section.title }}</h2>
              <span class="ml-auto rounded-full bg-gray-100 px-2 py-0.5 text-xs font-normal text-gray dark:bg-charcoal-500">
                {{ section.links.length }}
              </span>
            </div>
          </template>
          <div class="flex flex-col gap-2 px-3 pt-1 pb-3">
            <Link
              v-for="link in section.links"
              :key="link.id"
              :href="linkout(link.url)"
              class="flex min-w-0 items-center gap-2 rounded-lg border border-gray-200 bg-gray-100/60 px-3 py-2 font-semibold transition-colors hover:border-gray-300 dark:border-gray-800 dark:bg-charcoal-500/60 dark:hover:border-gray-700"
            >
              <span class="min-w-0 flex-grow truncate">{{ link.name }}</span>
              <IconMdiOpenInNew class="flex-shrink-0 text-sm text-gray" />
            </Link>
          </div>
        </Card>
      </template>

      <MemberList v-if="project?.members" :members="project.members" :author="project.namespace.owner" :slug="project.name" class="overflow-visible" />
    </aside>
  </div>
</template>
