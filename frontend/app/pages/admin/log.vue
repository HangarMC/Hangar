<script lang="ts" setup>
import type { Component } from "vue";
import IconMdiFolderOutline from "~icons/mdi/folder-outline";
import IconMdiPackageVariantClosed from "~icons/mdi/package-variant-closed";
import IconMdiFileDocumentOutline from "~icons/mdi/file-document-outline";
import IconMdiAccountOutline from "~icons/mdi/account-outline";
import IconMdiAccountGroupOutline from "~icons/mdi/account-group-outline";
import { Context } from "#shared/types/backend";
import type { HangarLoggedAction } from "#shared/types/backend";

definePageMeta({
  globalPermsRequired: ["ViewLogs"],
});

const i18n = useI18n();
const route = useRoute("admin-log");
const router = useRouter();

const PAGE_SIZE = 50;
const ALL_ACTIONS = "all";

const CONTEXTS: Record<Context, { icon: Component; tone: string }> = {
  [Context.PROJECT]: { icon: IconMdiFolderOutline, tone: "bg-sky-500/15 text-sky-500" },
  [Context.VERSION]: { icon: IconMdiPackageVariantClosed, tone: "bg-violet-500/15 text-violet-500" },
  [Context.PAGE]: { icon: IconMdiFileDocumentOutline, tone: "bg-amber-500/15 text-amber-500" },
  [Context.USER]: { icon: IconMdiAccountOutline, tone: "bg-lime-500/15 text-lime-500" },
  [Context.ORGANIZATION]: { icon: IconMdiAccountGroupOutline, tone: "bg-pink-500/15 text-pink-500" },
};

function queryString(key: string) {
  const value = route.query[key];
  return typeof value === "string" && value.length > 0 ? value : undefined;
}

const filter = reactive({
  user: queryString("user"),
  subjectName: queryString("subjectName"),
  logAction: queryString("logAction") ?? ALL_ACTIONS,
  authorName: queryString("authorName"),
  projectSlug: queryString("projectSlug"),
  dateFrom: queryString("dateFrom") ?? "",
  dateTo: queryString("dateTo") ?? "",
});

const page = ref(Math.max(0, Number.parseInt(queryString("page") ?? "1") - 1) || 0);
const order = ref<"newest" | "oldest">(queryString("order") === "oldest" ? "oldest" : "newest");

const activeFilters = computed(() =>
  [
    filter.user,
    filter.subjectName,
    filter.logAction === ALL_ACTIONS ? undefined : filter.logAction,
    filter.authorName,
    filter.projectSlug,
    filter.dateFrom,
    filter.dateTo,
  ].filter(Boolean)
);

function clearFilters() {
  filter.user = undefined;
  filter.subjectName = undefined;
  filter.logAction = ALL_ACTIONS;
  filter.authorName = undefined;
  filter.projectSlug = undefined;
  filter.dateFrom = "";
  filter.dateTo = "";
}

// a filtered result set is a different list, so the current offset is meaningless
watch(filter, () => (page.value = 0));

const query = computed(() => {
  const result: Record<string, string> = {};
  if (filter.user) result.user = filter.user;
  if (filter.subjectName) result.subjectName = filter.subjectName;
  if (filter.logAction && filter.logAction !== ALL_ACTIONS) result.logAction = filter.logAction;
  if (filter.authorName) result.authorName = filter.authorName;
  if (filter.projectSlug) result.projectSlug = filter.projectSlug;
  if (filter.dateFrom) result.dateFrom = filter.dateFrom;
  if (filter.dateTo) result.dateTo = filter.dateTo;
  if (page.value > 0) result.page = String(page.value + 1);
  if (order.value === "oldest") result.order = "oldest";
  return result;
});

watch(query, (value) => router.replace({ query: value }));

const requestParams = computed(() => ({
  user: filter.user || undefined,
  subjectName: filter.subjectName || undefined,
  logAction: filter.logAction === ALL_ACTIONS ? undefined : filter.logAction,
  authorName: filter.authorName || undefined,
  projectSlug: filter.projectSlug || undefined,
  dateFrom: filter.dateFrom || undefined,
  dateTo: filter.dateTo || undefined,
  limit: PAGE_SIZE,
  offset: page.value * PAGE_SIZE,
  sort: [order.value === "oldest" ? "time" : "-time"],
}));

const { actionLogs, actionLogsStatus } = useActionLogs(() => requestParams.value);

const pages = computed(() => Math.ceil((actionLogs.value?.pagination.count || 0) / PAGE_SIZE));

function actionLabel(pgLoggedAction: string) {
  const key =
    "userActionLog.types." +
    pgLoggedAction
      .split("_")
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join("");
  return i18n.te(key) ? i18n.t(key) : pgLoggedAction;
}

const actionOptions = computed(() => [
  { value: ALL_ACTIONS, text: i18n.t("userActionLog.allActions") },
  ...useBackendData.loggedActions.map((action) => ({ value: action, text: actionLabel(action) })).sort((a, b) => a.text.localeCompare(b.text)),
]);

function shiftDays(date: Date, days: number) {
  const shifted = new Date(date);
  shifted.setDate(shifted.getDate() + days);
  return shifted;
}

function dayLabel(day: string) {
  const now = new Date();
  if (day === toISODateString(now)) return i18n.t("general.today");
  if (day === toISODateString(shiftDays(now, -1))) return i18n.t("general.yesterday");
  return i18n.d(fromISOString(day), "date");
}

type Entry = { key: string; item: HangarLoggedAction; link?: { to: string; text: string }; summary?: string; details: boolean };

const groups = computed(() => {
  const result: { day: string; label: string; entries: Entry[] }[] = [];
  const items = actionLogs.value?.result || [];
  let index = 0;
  for (const item of items) {
    const day = toISODateString(new Date(item.createdAt));
    let group = result.at(-1);
    if (group?.day !== day) {
      group = { day, label: dayLabel(day), entries: [] };
      result.push(group);
    }
    group.entries.push({ key: String(index++), item, link: contextLink(item), summary: summary(item), details: Boolean(item.oldState || item.newState) });
  }
  return result;
});

const expanded = ref(new Set<string>());
watch(requestParams, () => expanded.value.clear());

function toggle(key: string) {
  if (expanded.value.has(key)) expanded.value.delete(key);
  else expanded.value.add(key);
}

function contextLink(item: HangarLoggedAction) {
  const project = item.project?.owner ? `/${item.project.owner}/${item.project.slug}` : undefined;
  if (project && item.page) return { to: `${project}/pages/${item.page.slug}`, text: `${item.project.owner}/${item.project.slug}/${item.page.slug}` };
  if (project && item.version) {
    return { to: `${project}/versions/${item.version.versionString}`, text: `${item.project.owner}/${item.project.slug}/${item.version.versionString}` };
  }
  if (project) return { to: project, text: `${item.project.owner}/${item.project.slug}` };
  return item.subject?.name ? { to: `/${item.subject.name}`, text: item.subject.name } : undefined;
}

function isMarkdown(item: HangarLoggedAction) {
  return item.contextType === Context.PAGE || item.action.pgLoggedAction === "version_description_changed";
}

function isIcon(item: HangarLoggedAction) {
  return item.action.pgLoggedAction === "project_icon_changed";
}

function translated(value?: string) {
  return value && i18n.te(value) ? i18n.t(value) : value;
}

function stateSides(item: HangarLoggedAction) {
  return [
    { label: i18n.t("userActionLog.oldState"), value: item.oldState },
    { label: i18n.t("userActionLog.newState"), value: item.newState },
  ];
}

// the state pair is the entry's payload; on one line it tells you what changed without opening anything
function summary(item: HangarLoggedAction) {
  if (isMarkdown(item) || isIcon(item)) return;
  const parts = [translated(item.oldState), translated(item.newState)].filter(Boolean);
  return parts.length > 0 ? parts.join(" → ") : undefined;
}

useSeo(computed(() => ({ title: i18n.t("userActionLog.title"), route })));
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <h1 class="text-3xl font-bold">{{ i18n.t("userActionLog.title") }}</h1>
        <p v-if="actionLogs?.pagination" class="mt-1 text-gray-secondary tabular-nums">
          {{ i18n.t("userActionLog.entries", actionLogs.pagination.count) }}
        </p>
      </div>
      <SegmentedControl
        v-model="order"
        :options="[
          { value: 'newest', label: i18n.t('userActionLog.sortNewest') },
          { value: 'oldest', label: i18n.t('userActionLog.sortOldest') },
        ]"
        :aria-label="i18n.t('userActionLog.time')"
      />
    </div>

    <Card flat padding="sm">
      <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <UserSearchInput v-model="filter.user" name="userfilter" :label="i18n.t('userActionLog.user')" />
        <InputSelect v-model="filter.logAction" :values="actionOptions" :label="i18n.t('userActionLog.action')" />
        <UserSearchInput v-model="filter.subjectName" name="subjectfilter" include-organizations :label="i18n.t('userActionLog.subject')" />
        <UserSearchInput v-model="filter.authorName" name="authorfilter" include-organizations :label="i18n.t('userActionLog.author')" />
        <ProjectSearchInput v-model="filter.projectSlug" name="projectfilter" :label="i18n.t('userActionLog.projectSlug')" />
        <InputDate v-model="filter.dateFrom" :label="i18n.t('userActionLog.dateFrom')" />
        <InputDate v-model="filter.dateTo" :label="i18n.t('userActionLog.dateTo')" />
        <Button v-if="activeFilters.length > 0" variant="outline" tone="neutral" class="self-center justify-self-start" @click="clearFilters">
          <IconMdiFilterRemoveOutline />
          {{ i18n.t("userActionLog.clearFilters") }}
        </Button>
      </div>
    </Card>

    <div v-if="groups.length > 0" class="flex flex-col gap-4">
      <section v-for="group in groups" :key="group.day">
        <h2 class="mb-2 text-xs font-semibold text-gray-secondary uppercase tracking-wide">{{ group.label }}</h2>
        <Card flat padding="none">
          <ul class="divide-y divide-gray-300 dark:divide-gray-700">
            <li v-for="entry in group.entries" :key="entry.key" class="px-3 py-2.5">
              <div class="flex items-start gap-3">
                <div class="mt-0.5 h-8 w-8 flex flex-shrink-0 items-center justify-center rounded-lg text-base" :class="CONTEXTS[entry.item.contextType].tone">
                  <component :is="CONTEXTS[entry.item.contextType].icon" />
                </div>
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-baseline gap-x-2">
                    <Link :to="'/' + entry.item.userName" class="font-semibold">{{ entry.item.userName }}</Link>
                    <span class="text-sm">{{ i18n.t(entry.item.action.description) }}</span>
                  </div>
                  <Link v-if="entry.link" :to="entry.link.to" class="block truncate text-sm">{{ entry.link.text }}</Link>
                  <p v-if="entry.summary" class="truncate text-xs text-gray-secondary">{{ entry.summary }}</p>
                </div>
                <div class="flex flex-shrink-0 items-center gap-2">
                  <span v-if="entry.item.address" class="hidden text-xs text-gray-secondary font-mono sm:inline">{{ entry.item.address }}</span>
                  <span class="text-xs text-gray-secondary tabular-nums">{{ i18n.d(entry.item.createdAt, "clock") }}</span>
                  <Button
                    v-if="entry.details"
                    variant="ghost"
                    tone="neutral"
                    size="sm"
                    icon-only
                    :aria-expanded="expanded.has(entry.key)"
                    :aria-label="i18n.t('userActionLog.details')"
                    :title="i18n.t('userActionLog.details')"
                    @click="toggle(entry.key)"
                  >
                    <IconMdiChevronDown class="transition-transform" :class="{ 'rotate-180': expanded.has(entry.key) }" />
                  </Button>
                </div>
              </div>

              <div v-if="expanded.has(entry.key)" class="mt-2 grid gap-3 rounded-md background-card p-3 ml-11 sm:grid-cols-2">
                <div v-for="side in stateSides(entry.item)" :key="side.label" class="min-w-0">
                  <div class="mb-1 text-xs font-semibold text-gray-secondary uppercase tracking-wide">{{ side.label }}</div>
                  <template v-if="isIcon(entry.item)">
                    <span v-if="!side.value || side.value === '#empty'" class="text-sm text-gray-secondary">{{ i18n.t("userActionLog.defaultIcon") }}</span>
                    <img v-else class="h-10 w-10 rounded" :src="'data:image/png;base64,' + side.value" alt="" />
                  </template>
                  <MarkdownModal v-else-if="isMarkdown(entry.item) && side.value" :markdown-input="side.value" :title="i18n.t('userActionLog.markdownView')">
                    <template #activator="{ on }">
                      <Button variant="outline" tone="neutral" size="sm" v-on="on">
                        <IconMdiLanguageMarkdown />
                        {{ i18n.t("userActionLog.markdownView") }}
                      </Button>
                    </template>
                  </MarkdownModal>
                  <span v-else-if="side.value" class="text-sm break-words">{{ translated(side.value) }}</span>
                  <span v-else class="text-sm text-gray-secondary">{{ i18n.t("userActionLog.emptyValue") }}</span>
                </div>
                <div v-if="isMarkdown(entry.item)" class="sm:col-span-2">
                  <DiffModal :left="entry.item.oldState" :right="entry.item.newState" :title="i18n.t('userActionLog.diffView')">
                    <template #activator="{ on }">
                      <Button variant="outline" tone="neutral" size="sm" v-on="on">
                        <IconMdiFileCompare />
                        {{ i18n.t("userActionLog.diffView") }}
                      </Button>
                    </template>
                  </DiffModal>
                </div>
                <div v-if="entry.item.address" class="text-xs text-gray-secondary font-mono sm:hidden">{{ entry.item.address }}</div>
              </div>
            </li>
          </ul>
        </Card>
      </section>

      <PaginationButtons v-if="pages > 1" :page="page" :pages="pages" @update:page="(p) => (page = p)" />
    </div>

    <Card v-else-if="actionLogsStatus !== 'loading'" flat class="flex flex-col items-center py-10 text-center">
      <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
        <IconMdiClipboardTextClockOutline />
      </div>
      <p class="text-gray-secondary">{{ i18n.t("userActionLog.empty") }}</p>
      <Button v-if="activeFilters.length > 0" class="mt-4" variant="outline" tone="neutral" @click="clearFilters">
        {{ i18n.t("userActionLog.clearFilters") }}
      </Button>
    </Card>
  </div>
</template>
