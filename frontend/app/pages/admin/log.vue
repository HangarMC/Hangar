<script lang="ts" setup>
import type { Header } from "#shared/types/components/SortableTable";
import { NamedPermission } from "#shared/types/backend";
import type { PaginatedResultProject, PaginatedResultUser } from "#shared/types/backend";

definePageMeta({
  globalPermsRequired: ["ViewLogs"],
});

const i18n = useI18n();
const route = useRoute("admin-log");
const router = useRouter();

// TODO add support for sorting
const headers = [
  { title: i18n.t("userActionLog.user"), name: "userName", sortable: false },
  { title: i18n.t("userActionLog.address"), name: "address", sortable: false },
  { title: i18n.t("userActionLog.time"), name: "time", sortable: false },
  { title: i18n.t("userActionLog.action"), name: "action", sortable: false },
  { title: i18n.t("userActionLog.context"), name: "context", sortable: false },
  { title: i18n.t("userActionLog.oldState"), name: "oldState", sortable: false },
  { title: i18n.t("userActionLog.newState"), name: "newState", sortable: false },
] as const satisfies Header<string>[];

if (!hasPerms(NamedPermission.ViewIp)) {
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
const { actionLogs } = useActionLogs(() => requestParams.value, router);

function updateSort(col: string, sorter: Record<string, number>) {
  sort.value = Object.keys(sorter)
    .map((k) => {
      const val = sorter[k];
      if (val === -1) return "-" + k;
      if (val === 1) return k;
      return;
    })
    .filter((v) => v !== undefined) as string[];
}

const userSearchResult = ref<string[]>([]);
const authorSearchResult = ref<string[]>([]);
const projectSearchResult = ref<string[]>([]);

async function searchUser(val?: string) {
  userSearchResult.value = [];
  const users = await useApi<PaginatedResultUser>("users", "get", {
    query: val,
    limit: 25,
    offset: 0,
  });
  userSearchResult.value = users.result.filter((u) => !u.isOrganization).map((u) => u.name);
}

async function searchAuthor(val?: string) {
  authorSearchResult.value = [];
  const authors = await useApi<PaginatedResultUser>("users", "get", {
    query: val,
    limit: 25,
    offset: 0,
  });
  authorSearchResult.value = authors.result.map((u) => u.name);
}

async function searchProject(val?: string) {
  projectSearchResult.value = [];
  const projects = await useApi<PaginatedResultProject>("projects", "get", {
    q: val,
    limit: 25,
    offset: 0,
  });
  projectSearchResult.value = projects.result.map((u) => u.namespace.slug);
}

useSeo(computed(() => ({ title: i18n.t("userActionLog.title"), route })));
</script>

<template>
  <div>
    <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold">{{ i18n.t("userActionLog.title") }}</h1>
        <p v-if="actionLogs?.pagination" class="mt-1 text-gray-secondary tabular-nums">{{ actionLogs.pagination.count.toLocaleString("en-US") }} entries</p>
      </div>
    </div>

    <Card flat class="mb-4">
      <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <InputAutocomplete id="userfilter" v-model="filter.user" :values="userSearchResult" :label="i18n.t('userActionLog.user')" @search="searchUser" />
        <div>
          <InputDropdown v-model="filter.logAction" :values="useBackendData.loggedActions" :label="i18n.t('userActionLog.action')" />
          <Button v-if="filter.logAction" variant="ghost" tone="neutral" size="sm" class="mt-1" @click="filter.logAction = undefined">
            <IconMdiClose />
            Clear selected action
          </Button>
        </div>
        <InputAutocomplete id="authorfilter" v-model="filter.authorName" :values="authorSearchResult" label="Author Name" @search="searchAuthor" />
        <InputAutocomplete id="projectfilter" v-model="filter.projectSlug" :values="projectSearchResult" label="Project Slug" @search="searchProject" />
      </div>
    </Card>

    <Card flat padding="none">
      <SortableTable
        :headers="headers"
        :items="actionLogs?.result || []"
        :server-pagination="actionLogs?.pagination"
        @update:sort="updateSort"
        @update:page="(p) => (page = p)"
      >
        <template #userName="{ item }">
          <Link :to="'/' + item.userName">{{ item.userName }}</Link>
        </template>
        <template #address="{ item }">
          <span class="text-xs text-gray-secondary font-mono">{{ item.address }}</span>
        </template>
        <template #time="{ item }">
          <span class="whitespace-nowrap text-sm text-gray-secondary tabular-nums">{{ i18n.d(item.createdAt, "time") }}</span>
        </template>
        <template #action="{ item }">
          <span class="text-sm font-medium">{{ i18n.t(item.action.description) }}</span>
        </template>
        <template #empty>
          <div class="flex flex-col items-center px-4 py-10 text-center">
            <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
              <IconMdiClipboardTextClockOutline />
            </div>
            <p class="text-gray-secondary">No log entries match these filters.</p>
          </div>
        </template>
        <template #context="{ item }">
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
        <template #oldState="{ item }">
          <template v-if="(item.contextType === 'PAGE' || item.action.pgLoggedAction === 'version_description_changed') && item.oldState">
            <MarkdownModal :markdown-input="item.oldState" :title="i18n.t('userActionLog.markdownView')">
              <template #activator="{ on }">
                <Button
                  variant="outline"
                  tone="neutral"
                  size="sm"
                  icon-only
                  :title="i18n.t('userActionLog.markdownView')"
                  :aria-label="i18n.t('userActionLog.markdownView')"
                  v-on="on"
                >
                  <IconMdiLanguageMarkdown />
                </Button>
              </template>
            </MarkdownModal>
          </template>
          <template v-else-if="item.action.pgLoggedAction === 'project_icon_changed'">
            <span v-if="item.oldState === '#empty'" class="text-sm text-gray-secondary">default</span>
            <img v-else class="h-8 w-8 rounded" :src="'data:image/png;base64,' + item.oldState" alt="" />
          </template>
          <template v-else>
            <span class="text-sm text-gray-secondary">{{ item.oldState && i18n.te(item.oldState) ? i18n.t(item.oldState) : item.oldState }}</span>
          </template>
        </template>
        <template #newState="{ item }">
          <template v-if="item.contextType === 'PAGE' || item.action.pgLoggedAction === 'version_description_changed'">
            <div class="flex gap-1">
              <MarkdownModal :markdown-input="item.newState" :title="i18n.t('userActionLog.markdownView')">
                <template #activator="{ on }">
                  <Button
                    variant="outline"
                    tone="neutral"
                    size="sm"
                    icon-only
                    :title="i18n.t('userActionLog.markdownView')"
                    :aria-label="i18n.t('userActionLog.markdownView')"
                    v-on="on"
                  >
                    <IconMdiLanguageMarkdown />
                  </Button>
                </template>
              </MarkdownModal>
              <DiffModal :left="item.oldState" :right="item.newState" :title="i18n.t('userActionLog.diffView')">
                <template #activator="{ on }">
                  <Button
                    variant="outline"
                    tone="neutral"
                    size="sm"
                    icon-only
                    :title="i18n.t('userActionLog.diffView')"
                    :aria-label="i18n.t('userActionLog.diffView')"
                    v-on="on"
                  >
                    <IconMdiFileCompare />
                  </Button>
                </template>
              </DiffModal>
            </div>
          </template>
          <template v-else-if="item.action.pgLoggedAction === 'project_icon_changed'">
            <span v-if="item.newState === '#empty'" class="text-sm text-gray-secondary">default</span>
            <img v-else class="h-8 w-8 rounded" :src="'data:image/png;base64,' + item.newState" alt="" />
          </template>
          <template v-else>
            <span class="text-sm text-gray-secondary">{{ i18n.te(item.newState) ? i18n.t(item.newState) : item.newState }}</span>
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

main[data-page="admin-log"] .simple-table th {
  font-size: 0.75rem;
  letter-spacing: 0.03em;
  text-transform: uppercase;
  color: #6b7280;
}

main[data-page="admin-log"] .simple-table td {
  vertical-align: top;
}

main[data-page="admin-log"] .simple-table tbody tr:hover td {
  background-color: rgb(0 0 0 / 3%);
}

.dark main[data-page="admin-log"] .simple-table tbody tr:hover td {
  background-color: rgb(255 255 255 / 4%);
}
</style>
