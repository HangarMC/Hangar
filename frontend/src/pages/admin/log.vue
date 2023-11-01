<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useHead } from "@unhead/vue";
import type { PaginatedResult, Project, User } from "hangar-api";
import { computed, ref } from "vue";
import type { LoggedAction } from "hangar-internal";
import { debounce } from "lodash-es";
import PageTitle from "~/components/design/PageTitle.vue";
import Card from "~/components/design/Card.vue";
import SortableTable from "~/components/SortableTable.vue";
import Link from "~/components/design/Link.vue";
import MarkdownModal from "~/components/modals/MarkdownModal.vue";
import DiffModal from "~/components/modals/DiffModal.vue";
import Button from "~/components/design/Button.vue";
import { useSeo } from "~/composables/useSeo";
import { definePageMeta, hasPerms, useApi, useInternalApi, useRouter, watch } from "#imports";
import InputSelect from "~/components/ui/InputSelect.vue";
import { useBackendData } from "~/store/backendData";
import type { Header } from "~/types/components/SortableTable";
import { NamedPermission } from "~/types/enums";
import InputAutocomplete from "~/components/ui/InputAutocomplete.vue";

definePageMeta({
  globalPermsRequired: ["VIEW_LOGS"],
});

const i18n = useI18n();
const route = useRoute();
const router = useRouter();
const loggedActions = ref();

// TODO add support for sorting
const headers: Header[] = [
  { title: i18n.t("userActionLog.user"), name: "user", sortable: false },
  { title: i18n.t("userActionLog.address"), name: "address", sortable: false },
  { title: i18n.t("userActionLog.time"), name: "time", sortable: false },
  { title: i18n.t("userActionLog.action"), name: "action", sortable: false },
  { title: i18n.t("userActionLog.context"), name: "context", sortable: false },
  { title: i18n.t("userActionLog.oldState"), name: "oldState", sortable: false },
  { title: i18n.t("userActionLog.newState"), name: "newState", sortable: false },
];

if (!hasPerms(NamedPermission.VIEW_IP)) {
  headers.splice(1, 1);
}

const page = ref(0);
const sort = ref<string[]>([]);
const filter = ref<{
  user?: string;
  logAction?: string;
  authorName?: string;
  projectSlug?: string;
}>({});

if (route.query.authorName) {
  filter.value.authorName = route.query.authorName as string;
}
if (route.query.projectSlug) {
  filter.value.projectSlug = route.query.projectSlug as string;
}
if (route.query.user) {
  filter.value.user = route.query.user as string;
}
if (route.query.logAction) {
  filter.value.logAction = route.query.logAction as string;
}

const requestParams = computed(() => {
  const limit = 25;
  return {
    ...filter.value,
    limit,
    offset: page.value * limit,
    sort: sort.value,
  };
});

await update();

watch(filter, debounce(update, 500), { deep: true });

async function updateSort(col: string, sorter: Record<string, number>) {
  sort.value = [...Object.keys(sorter)]
    .map((k) => {
      const val = sorter[k];
      if (val === -1) return "-" + k;
      if (val === 1) return k;
      return null;
    })
    .filter((v) => v !== null) as string[];

  await update();
}

async function updatePage(newPage: number) {
  page.value = newPage;
  await update();
}

async function update() {
  loggedActions.value = await useInternalApi<PaginatedResult<LoggedAction>>("admin/log", "GET", requestParams.value);
  const { limit, offset, ...paramsWithoutLimit } = requestParams.value;
  await router.replace({ query: { ...paramsWithoutLimit } });
}

const userSearchResult = ref<string[]>([]);
const authorSearchResult = ref<string[]>([]);
const projectSearchResult = ref<string[]>([]);

async function searchUser(val: string) {
  userSearchResult.value = [];
  const users = await useApi<PaginatedResult<User>>("users", "get", {
    query: val,
    limit: 25,
    offset: 0,
  });
  userSearchResult.value = users.result.filter((u) => !u.isOrganization).map((u) => u.name);
}

async function searchAuthor(val: string) {
  authorSearchResult.value = [];
  const authors = await useApi<PaginatedResult<User>>("users", "get", {
    query: val,
    limit: 25,
    offset: 0,
  });
  authorSearchResult.value = authors.result.map((u) => u.name);
}

async function searchProject(val: string) {
  projectSearchResult.value = [];
  const projects = await useApi<PaginatedResult<Project>>("projects", "get", {
    q: val,
    limit: 25,
    offset: 0,
  });
  projectSearchResult.value = projects.result.map((u) => u.namespace.slug);
}

useHead(useSeo(i18n.t("userActionLog.title"), null, route, null));
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("userActionLog.title") }}</PageTitle>
    <Card>
      <div class="flex mb-4 gap-2">
        <div class="basis-3/12">
          <InputAutocomplete
            id="userfilter"
            v-model="filter.user"
            :values="userSearchResult"
            :label="i18n.t('organization.settings.transferModal.transferTo')"
            @search="searchUser"
          />
        </div>
        <div class="basis-3/12">
          <InputSelect v-model="filter.logAction" :values="useBackendData.loggedActions" label="Action" />
          <span v-if="filter.logAction" class="flex justify-center" cursor="pointer" @click="filter.logAction = undefined"> Clear selected action </span>
        </div>
        <div class="basis-3/12">
          <InputAutocomplete id="authorfilter" v-model="filter.authorName" :values="authorSearchResult" label="Author Name" @search="searchAuthor" />
        </div>
        <div class="basis-3/12">
          <InputAutocomplete id="projectfilter" v-model="filter.projectSlug" :values="projectSearchResult" label="Project Slug" @search="searchProject" />
        </div>
      </div>

      <SortableTable
        :loading="true"
        :headers="headers"
        :items="loggedActions?.result"
        :server-pagination="loggedActions?.pagination"
        @update:sort="updateSort"
        @update:page="updatePage"
      >
        <template #item_user="{ item }">
          <Link :to="'/' + item.userName">{{ item.userName }}</Link>
        </template>
        <template #item_time="{ item }">
          {{ i18n.d(item.createdAt, "time") }}
        </template>
        <template #item_action="{ item }">
          {{ i18n.t(item.action.description) }}
        </template>
        <template #item_context="{ item }">
          <template v-if="item.project && item.page">
            <Link :to="'/' + item.project.owner + '/' + item.project.slug + '/pages/' + item.page.slug">
              {{ item.project.owner + "/" + item.project.slug + "/" + item.page.slug }}
            </Link>
          </template>
          <template v-else-if="item.version && item.project">
            <Link :to="'/' + item.project.owner + '/' + item.project.slug + '/versions/' + item.version.versionString">
              {{ `${item.project.owner}/${item.project.slug}/${item.version.versionString}` }}
            </Link>
          </template>
          <template v-else-if="item.project && item.project.owner">
            <Link :to="'/' + item.project.owner + '/' + item.project.slug">{{ item.project.owner + "/" + item.project.slug }} </Link>
          </template>
          <template v-else-if="item.subject">
            <Link :to="'/' + item.subject.name">{{ item.subject.name }}</Link>
          </template>
        </template>
        <template #item_oldState="{ item }">
          <template v-if="(item.contextType === 'PAGE' || item.action.pgLoggedAction === 'version_description_changed') && item.oldState">
            <MarkdownModal :markdown-input="item.oldState" :title="i18n.t('userActionLog.markdownView')">
              <template #activator="{ on }">
                <Button size="small" v-on="on">
                  {{ i18n.t("userActionLog.markdownView") }}
                </Button>
              </template>
            </MarkdownModal>
          </template>
          <template v-else-if="item.action.pgLoggedAction === 'project_icon_changed'">
            <span v-if="item.oldState === '#empty'">default</span>
            <img v-else class="inline-img" :src="'data:image/png;base64,' + item.oldState" alt="" />
          </template>
          <template v-else>
            <span>{{ item.oldState && i18n.te(item.oldState) ? i18n.t(item.oldState) : item.oldState }}</span>
          </template>
        </template>
        <template #item_newState="{ item }">
          <template v-if="item.contextType === 'PAGE' || item.action.pgLoggedAction === 'version_description_changed'">
            <div class="flex gap-2">
              <MarkdownModal :markdown-input="item.newState" :title="i18n.t('userActionLog.markdownView')">
                <template #activator="{ on }">
                  <Button size="small" v-on="on">
                    {{ i18n.t("userActionLog.markdownView") }}
                  </Button>
                </template>
              </MarkdownModal>
              <DiffModal :left="item.oldState" :right="item.newState" :title="i18n.t('userActionLog.diffView')">
                <template #activator="{ on }">
                  <Button size="small" v-on="on">
                    {{ i18n.t("userActionLog.diffView") }}
                  </Button>
                </template>
              </DiffModal>
            </div>
          </template>
          <template v-else-if="item.action.pgLoggedAction === 'project_icon_changed'">
            <span v-if="item.newState === '#empty'">default</span>
            <img v-else class="inline-img" :src="'data:image/png;base64,' + item.newState" alt="" />
          </template>
          <template v-else>
            <span>{{ i18n.te(item.newState) ? i18n.t(item.newState) : item.newState }}</span>
          </template>
        </template>
      </SortableTable>
    </Card>
  </div>
</template>

<style>
main[data-page="admin-log"] .max-w-screen-xl {
  max-width: 100% !important;
}
</style>
