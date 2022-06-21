<script lang="ts" setup>
import { User } from "hangar-api";
import Card from "~/components/design/Card.vue";
import { useI18n } from "vue-i18n";
import ProjectInfo from "~/components/projects/ProjectInfo.vue";
import { HangarProject, PinnedVersion } from "hangar-internal";
import MemberList from "~/components/projects/MemberList.vue";
import MarkdownEditor from "~/components/MarkdownEditor.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import { useRoute, useRouter } from "vue-router";
import { useContext } from "vite-ssr/vue";
import Markdown from "~/components/Markdown.vue";
import ProjectPageList from "~/components/projects/ProjectPageList.vue";
import { useProjectPage } from "~/composables/useProjectPage";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { ref } from "vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useBackendDataStore } from "~/store/backendData";

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();
const i18n = useI18n();
const backendData = useBackendDataStore();
const ctx = useContext();
const route = useRoute();
const context = useContext();
const router = useRouter();
const { editingPage, open, savePage, page } = await useProjectPage(route, router, context, i18n, props.project);

const sponsors = ref(props.project.settings.sponsors);
const editingSponsors = ref(false);
function saveSponsors(content: string) {
  useInternalApi(`projects/project/${props.project.namespace.owner}/${props.project.namespace.slug}/sponsors`, true, "post", {
    content,
  })
    .then(() => {
      if (sponsors.value) {
        sponsors.value = content;
      }
      editingSponsors.value = false;
    })
    .catch((e) => handleRequestError(e, ctx, i18n, "page.new.error.save"));
}

function createPinnedVersionUrl(version: PinnedVersion, platformIdx: number): string {
  return `${props.project.namespace.owner}/${props.project.namespace.slug}/versions/${version.versionString}/${version.platforms[platformIdx].toLowerCase()}`;
}

useHead(useSeo(props.project.name, props.project.description, route, projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)));
</script>

<template>
  <div class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-9/12 flex-grow overflow-auto">
      <Card v-if="page?.contents" class="p-0 pb-6 overflow-clip overflow-hidden">
        <MarkdownEditor
          v-if="hasPerms(NamedPermission.EDIT_PAGE)"
          v-model:editing="editingPage"
          :raw="page.contents"
          :deletable="false"
          :saveable="true"
          :cancellable="true"
          @save="savePage"
        />
        <Markdown v-else :raw="page.contents" />
      </Card>
      <Card v-if="sponsors || hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)" class="mt-2 p-0 pb-6 overflow-clip overflow-hidden">
        <MarkdownEditor
          v-if="hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)"
          v-model:editing="editingSponsors"
          :raw="sponsors"
          :deletable="false"
          :saveable="true"
          :cancellable="true"
          :maxlength="500"
          :title="i18n.t('project.sponsors')"
          class="pt-0"
          @save="saveSponsors"
        />
        <template v-else>
          <h1 class="mt-3 ml-5 text-xl">{{ i18n.t("project.sponsors") }}</h1>
          <Markdown :raw="sponsors" class="pt-0" />
        </template>
      </Card>
    </section>
    <section class="basis-full md:basis-3/12 space-y-4 min-w-280px">
      <ProjectInfo :project="project" />
      <Card>
        <template #header>{{ i18n.t("project.pinnedVersions") }}</template>
        <ul v-if="backendData.platforms" class="divide-y divide-blue-500/50">
          <li v-for="(version, index) in project.pinnedVersions" :key="`${index}-${version.versionString}`">
            <template v-if="version.platforms.length === 1">
              <router-link :to="createPinnedVersionUrl(version, 0)">
                {{ `${version.versionString} for ${backendData.platforms.get(version.platforms[0]).name}` }}
              </router-link>
            </template>
            <template v-else>
              {{ `${version.versionString} for ` }}
              <router-link v-for="(platform, idx) in version.platforms" :key="`${idx}-${platform}`" :to="createPinnedVersionUrl(version, idx)">
                {{ `${backendData.platforms.get(platform).name},` }}
              </router-link>
            </template>
          </li>
        </ul>
      </Card>
      <ProjectPageList :project="project" :open="open" />
      <MemberList :members="project.members" :author="project.owner.name" :slug="project.name" class="overflow-visible" />
    </section>
  </div>
</template>
