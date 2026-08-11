<script lang="ts" setup>
import { upperFirst } from "scule";
import { NamedPermission } from "#shared/types/backend";
import type { HangarProject, PinnedVersion, Platform } from "#shared/types/backend";

const props = defineProps<{
  project?: HangarProject;
}>();

const config = useRuntimeConfig();
const i18n = useI18n();
const route = useRoute("user-project-pages-page");
const openProjectPages = useOpenProjectPages(route, props.project);

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
  <div class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-11/15 flex-grow overflow-auto">
      <ProjectPageMarkdown v-slot="{ editingPage, changeEditingPage, savePage }" :project="props.project" :page="props.project?.mainPage" main-page>
        <Card v-if="project?.mainPage?.contents" class="pb-0 overflow-clip overflow-hidden">
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
      <Card v-if="sponsors || hasPerms(NamedPermission.EditSubjectSettings)" class="mt-2 pb-0 overflow-clip overflow-visible">
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
          <p v-if="!sponsors && !editingSponsors" class="px-4 pb-4 text-gray-secondary">{{ i18n.t("project.sponsorsEmpty") }}</p>
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
      <Alert v-if="hasPerms(NamedPermission.EditSubjectSettings)" type="neutral" class="mt-2">
        <div>
          {{ i18n.t("project.bannersInfo") }}
          <Link :to="'/' + project?.namespace?.owner + '/' + project?.namespace?.slug + '/settings/banners'">
            {{ i18n.t("project.bannersInfoSettings") }}
          </Link>
        </div>
      </Alert>
    </section>
    <section class="basis-full md:basis-4/15 space-y-4 min-w-280px">
      <ProjectInfo :project="project" />
      <Card v-if="project?.pinnedVersions?.length">
        <template #header>
          <h2>{{ i18n.t("project.pinnedVersions") }}</h2>
        </template>
        <ul class="divide-y divide-gray-300 dark:divide-gray-700">
          <li v-for="(version, index) in project?.pinnedVersions" :key="`${index}-${version.name}`" class="relative flex items-center gap-2.5 py-2">
            <ChannelTile :channel="version.channel" />
            <div class="min-w-0 flex-1">
              <div class="truncate font-semibold">
                <NuxtLink :to="createPinnedVersionUrl(version)" class="after:(absolute inset-0 content-empty)">{{ version.name }}</NuxtLink>
              </div>
              <div class="mt-0.5 flex flex-wrap gap-x-3 gap-y-0.5 text-sm text-gray-secondary">
                <span v-for="(v, p) in version.platformDependenciesFormatted" :key="p" class="inline-flex items-center gap-1" :title="v.join(', ')">
                  <PlatformLogo :platform="p as Platform" :size="16" class="flex-shrink-0" />
                  <span class="tabular-nums">{{ collapseRanges(v) }}</span>
                </span>
              </div>
            </div>
            <DownloadButton v-if="project" :project="project" :pinned-version="version" small :show-versions="false" class="relative z-1 flex-shrink-0" />
          </li>
        </ul>
        <Skeleton v-if="!project" />
      </Card>
      <ProjectPageList :project="project" :open="openProjectPages" />

      <template v-for="section in project?.settings?.links">
        <Card v-if="section.type === 'sidebar'" :key="section.id">
          <template #header>
            <h3 class="text-base">{{ section.title }}</h3>
          </template>
          <div class="flex flex-col gap-0.5">
            <template v-for="link in section.links" :key="link.id">
              <Link :href="linkout(link.url)">{{ link.name }}</Link>
            </template>
          </div>
        </Card>
      </template>

      <MemberList
        v-if="project?.members"
        :members="project.members"
        :author="project.namespace.owner"
        :slug="project.name"
        :manage="false"
        :settings-link="`/${project.namespace.owner}/${project.namespace.slug}/settings/members`"
        class="overflow-visible"
      />
    </section>
  </div>
</template>
