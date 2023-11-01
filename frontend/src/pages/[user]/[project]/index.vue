<script lang="ts" setup>
import type { User } from "hangar-api";
import { useI18n } from "vue-i18n";
import type { HangarProject, PinnedVersion } from "hangar-internal";
import { useRoute, useRouter } from "vue-router";
import { ref } from "vue";
import Card from "~/components/design/Card.vue";
import ProjectInfo from "~/components/projects/ProjectInfo.vue";
import MemberList from "~/components/projects/MemberList.vue";
import { MarkdownEditor } from "#components";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import Markdown from "~/components/Markdown.vue";
import ProjectPageList from "~/components/projects/ProjectPageList.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import Tag from "~/components/Tag.vue";
import PlatformLogo from "~/components/logos/platforms/PlatformLogo.vue";
import DownloadButton from "~/components/projects/DownloadButton.vue";
import { useOpenProjectPages } from "~/composables/useOpenProjectPages";
import ProjectPageMarkdown from "~/components/projects/ProjectPageMarkdown.vue";
import { useBackendData } from "~/store/backendData";
import Tooltip from "~/components/design/Tooltip.vue";
import Link from "~/components/design/Link.vue";
import { required } from "~/composables/useValidationHelpers";
import { linkout } from "~/composables/useUrlHelper";

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();

const i18n = useI18n();
const route = useRoute();
const router = useRouter();
const openProjectPages = await useOpenProjectPages(route, props.project);

const sponsors = ref(props.project.settings.sponsors);
const editingSponsors = ref(false);
function saveSponsors(content: string) {
  useInternalApi(`projects/project/${props.project.namespace.owner}/${props.project.namespace.slug}/sponsors`, "post", {
    content,
  })
    .then(() => {
      sponsors.value = content;
      editingSponsors.value = false;
    })
    .catch((e) => handleRequestError(e, "page.new.error.save"));
}

function createPinnedVersionUrl(version: PinnedVersion): string {
  return `/${props.project.namespace.owner}/${props.project.namespace.slug}/versions/${version.name}`;
}

// useSeo is in ProjectPageMarkdown
</script>

<template>
  <div class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-11/15 flex-grow overflow-auto">
      <ProjectPageMarkdown v-slot="{ page, editingPage, changeEditingPage, savePage }" :project="props.project" main-page>
        <Card v-if="page?.contents" class="pb-0 overflow-clip overflow-hidden">
          <ClientOnly v-if="hasPerms(NamedPermission.EDIT_PAGE)">
            <MarkdownEditor
              :editing="editingPage"
              :raw="page.contents"
              :deletable="false"
              :saveable="true"
              :cancellable="true"
              :maxlength="useBackendData.validations.project.pageContent?.max"
              :rules="[required()]"
              @update:editing="changeEditingPage"
              @save="savePage"
            />
            <template #fallback>
              <Markdown :raw="page.contents" />
            </template>
          </ClientOnly>
          <!--We have to blow up v-model:editing into :editing and @update:editing as we are inside a scope--->
          <Markdown v-else :raw="page.contents" />
        </Card>
      </ProjectPageMarkdown>
      <Card v-if="sponsors || hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)" class="mt-2 pb-0 overflow-clip overflow-visible">
        <ClientOnly v-if="hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)">
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
                <h1 class="ml-4 text-2xl">{{ i18n.t("project.sponsors") }}</h1>
                <Tooltip class="overflow-visible">
                  <template #content> {{ i18n.t("project.sponsorsTooltip") }} </template>
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
    </section>
    <section class="basis-full md:basis-4/15 space-y-4 min-w-280px">
      <ProjectInfo :project="project" />
      <Card>
        <template #header>
          <h3>{{ i18n.t("project.pinnedVersions") }}</h3>
        </template>
        <ul class="divide-y divide-blue-500/50">
          <li v-for="(version, index) in project.pinnedVersions" :key="`${index}-${version.name}`" class="p-1 py-2">
            <div class="flex">
              <NuxtLink :to="createPinnedVersionUrl(version)" class="flex-grow truncate">
                <div class="truncate">
                  <span class="font-semibold truncate">{{ version.name }}</span>
                </div>
              </NuxtLink>
              <div class="ml-1 space-y-2 flex flex-col">
                <Tag :name="version.channel.name" :color="{ background: version.channel.color }" :tooltip="version.channel.description" />
              </div>
            </div>
            <div class="flex pt-1">
              <NuxtLink :to="createPinnedVersionUrl(version)" class="flex-grow">
                <div class="inline-flex items-center mt-1">
                  <div class="flex flex-col">
                    <div v-for="(v, p) in version.platformDependenciesFormatted" :key="p" class="flex flex-row items-center">
                      <PlatformLogo :key="p" :platform="p" :size="20" class="mr-1 flex-shrink-0" />
                      <span :key="p" class="text-0.875rem light:text-gray-600">{{ v }}</span>
                    </div>
                  </div>
                </div>
              </NuxtLink>
              <div class="ml-1 space-y-2 flex flex-col mt-1">
                <DownloadButton :project="project" :pinned-version="version" small :show-versions="false" class="self-end" />
              </div>
            </div>
          </li>
        </ul>
      </Card>
      <ProjectPageList :project="project" :open="openProjectPages" />

      <template v-for="section in project.settings.links">
        <Card v-if="section.type === 'sidebar'" :key="section.id">
          <template #header>
            <h3>{{ section.title }}</h3>
          </template>
          <div class="flex flex-col">
            <template v-for="link in section.links" :key="link.id">
              <Link :href="linkout(link.url)">{{ link.name }}</Link>
            </template>
          </div>
        </Card>
      </template>

      <MemberList :members="project.members" :author="project.owner.name" :slug="project.name" :owner="project.owner.userId" class="overflow-visible" />
    </section>
  </div>
</template>
