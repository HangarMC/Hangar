<script lang="ts" setup>
import type { HangarProjectFlag, HangarProjectFlagNotification, Visibility } from "#shared/types/backend";

const props = defineProps<{
  resolved?: boolean;
  // when set, renders this fixed list read-only instead of fetching the resolved/unresolved queue
  flags?: HangarProjectFlag[];
}>();

const i18n = useI18n();
const isReadonly = computed(() => props.flags !== undefined);

const PAGE_SIZES = ["10", "25", "50"];
const page = ref(0);
const order = ref<"newest" | "oldest">("newest");
const perPage = ref(10);

// resolved reports are looked for by when they were dealt with, open ones by when they came in
const sorter = computed(() => (props.resolved ? "flagResolved" : "flagCreated"));
const { flags: queueFlags, refreshFlags } = useFlags(
  () => ({
    resolved: Boolean(props.resolved),
    limit: perPage.value,
    offset: page.value * perPage.value,
    sort: (order.value === "newest" ? "-" : "") + sorter.value,
  }),
  () => isReadonly.value
);

const loading = ref<{ [key: number]: boolean }>({});

const items = computed<HangarProjectFlag[]>(() => props.flags ?? queueFlags.value?.result ?? []);
const total = computed(() => (isReadonly.value ? items.value.length : (queueFlags.value?.pagination?.count ?? 0)));
const pages = computed(() => Math.ceil(total.value / perPage.value));
const visibleItems = computed(() => (isReadonly.value ? items.value.slice(page.value * perPage.value, (page.value + 1) * perPage.value) : items.value));

const orderOptions = computed(() => [
  { value: "newest" as const, label: props.resolved ? i18n.t("flagReview.sortRecentlyResolved") : i18n.t("flagReview.sortNewest") },
  { value: "oldest" as const, label: props.resolved ? i18n.t("flagReview.sortLeastRecentlyResolved") : i18n.t("flagReview.sortOldest") },
]);
const pageSizeOptions = PAGE_SIZES.map((size) => ({ value: size, label: size }));
const perPageModel = computed({
  get: () => String(perPage.value),
  set: (value: string) => (perPage.value = Number(value)),
});

watch([order, perPage, () => props.resolved], () => (page.value = 0));
// resolving the last entry of a page leaves it empty
watch(pages, (count) => {
  if (page.value > 0 && page.value >= count) {
    page.value = Math.max(0, count - 1);
  }
});

const headerTitle = computed(() => {
  if (isReadonly.value) {
    return i18n.t("flags.allReports");
  }
  return props.resolved ? i18n.t("flagReview.resolved") : i18n.t("flagReview.unresolved");
});
const emptyText = computed(() => (isReadonly.value ? i18n.t("flags.noFlags") : i18n.t("flagReview.noFlags")));

function visibilityName(visibility: Visibility) {
  const data = useBackendData.visibilities.find((v) => v.name === visibility);
  return data ? i18n.t(data.title) : visibility;
}

async function resolve(flag: HangarProjectFlag) {
  loading.value[flag.id] = true;
  try {
    await useInternalApi(`flags/${flag.id}/resolve/${props.resolved ? "false" : "true"}`, "POST");
    await refreshFlags();
  } catch (err) {
    handleRequestError(err);
  } finally {
    loading.value[flag.id] = false;
  }
}

// TODO: bake into hangarflag?
const notifications = ref<HangarProjectFlagNotification[]>([]);
const currentId = ref(-1);

async function getNotifications(flag: HangarProjectFlag) {
  if (currentId.value === flag.id) {
    return;
  }

  notifications.value = (await useInternalApi<HangarProjectFlagNotification[]>(`flags/${flag.id}/notifications`, "get").catch((err) =>
    handleRequestError(err)
  )) as HangarProjectFlagNotification[];
  currentId.value = flag.id;
}
</script>

<template>
  <Card flat padding="none">
    <div class="flex flex-wrap items-center gap-x-3 gap-y-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
      <h2 class="text-lg font-bold">{{ headerTitle }}</h2>
      <span class="flex-grow text-sm text-gray-secondary tabular-nums">{{ total }}</span>
      <template v-if="!isReadonly && total > 0">
        <SegmentedControl v-model="order" :options="orderOptions" :aria-label="i18n.t('flagReview.sortLabel')" />
        <SegmentedControl v-model="perPageModel" :options="pageSizeOptions" :aria-label="i18n.t('flagReview.perPage')" />
      </template>
    </div>

    <ul v-if="visibleItems.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
      <li v-for="item in visibleItems" :key="item.id" class="px-4 py-3">
        <div class="flex flex-col gap-3 sm:flex-row sm:items-start">
          <div
            class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg text-lg"
            :class="item.resolved ? 'bg-lime-500/15 text-lime-500' : 'bg-amber-500/15 text-amber-500'"
          >
            <IconMdiFlagCheckered v-if="item.resolved" />
            <IconMdiFlag v-else />
          </div>

          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-center gap-x-1.5">
              <template v-if="isReadonly">
                <span class="text-gray-secondary">{{ i18n.t("flagReview.reportedByLabel") }}</span>
                <Link :to="'/' + item.reportedByName" target="_blank">{{ item.reportedByName }}</Link>
              </template>
              <template v-else>
                <Link :to="'/' + item.reportedByName" target="_blank">{{ item.reportedByName }}</Link>
                <span class="text-gray-secondary">{{ i18n.t("flagReview.reportedWord") }}</span>
                <Link :to="`/${item.projectNamespace.owner}/${item.projectNamespace.slug}`" target="_blank">
                  {{ `${item.projectNamespace.owner}/${item.projectNamespace.slug}` }}
                </Link>
              </template>
            </div>

            <div class="mt-1.5 flex flex-wrap items-center gap-2">
              <Chip v-if="isReadonly" :tone="item.resolved ? 'green' : 'amber'">
                <IconMdiFlagCheckered v-if="item.resolved" />
                <IconMdiFlagOutline v-else />
                {{ item.resolved ? i18n.t("flagReview.statusResolved") : i18n.t("flagReview.statusOpen") }}
              </Chip>
              <Chip :tone="isReadonly ? 'neutral' : 'amber'">
                <IconMdiFlagOutline />
                {{ i18n.t(item.reason) }}
              </Chip>
              <Chip v-if="!item.resolved && !isReadonly" tone="neutral">
                {{ i18n.t("flagReview.projectVisibility", [visibilityName(item.projectVisibility)]) }}
              </Chip>
              <span class="text-xs text-gray-secondary" :title="i18n.d(item.createdAt, 'time')">
                {{ i18n.t("flagReview.reportedAt", [lastUpdated(new Date(item.createdAt))]) }}
              </span>
              <span v-if="item.resolved && item.resolvedAt" class="text-xs text-gray-secondary" :title="i18n.d(item.resolvedAt, 'time')">
                &middot; {{ i18n.t("flagReview.resolvedAtBy", [lastUpdated(new Date(item.resolvedAt)), item.resolvedByName]) }}
              </span>
              <span v-else-if="item.resolved && item.resolvedByName" class="text-xs text-gray-secondary">
                &middot; {{ i18n.t("flagReview.resolvedBy", [item.resolvedByName]) }}
              </span>
            </div>

            <p v-if="item.comment" class="mt-1.5 text-sm text-gray-secondary">{{ i18n.t("flagReview.line3", [item.comment]) }}</p>
          </div>

          <template v-if="!isReadonly">
            <Button v-if="resolved" variant="outline" tone="neutral" size="sm" :loading="loading[item.id]" class="flex-shrink-0" @click="resolve(item)">
              <IconMdiUndo />
              {{ i18n.t("flagReview.markUnresolved") }}
            </Button>
            <Button v-else size="sm" :loading="loading[item.id]" class="flex-shrink-0" @click="resolve(item)">
              <IconMdiCheck />
              {{ i18n.t("flagReview.markResolved") }}
            </Button>
          </template>
        </div>

        <template v-if="!isReadonly">
          <div class="mt-3 flex flex-wrap items-center gap-2 sm:pl-12">
            <ReportNotificationModal variant="outline" tone="neutral" size="sm" :flag="item" :send-to-reporter="false" />
            <ReportNotificationModal variant="outline" tone="neutral" size="sm" :flag="item" :send-to-reporter="true" />
            <VisibilityChangerModal
              v-if="!resolved"
              variant="outline"
              tone="neutral"
              size="sm"
              type="project"
              :prop-visibility="item.projectVisibility"
              :post-url="`projects/visibility/${item.projectId}`"
            />
            <Button v-if="currentId !== item.id" variant="ghost" tone="neutral" size="sm" @click="getNotifications(item)">
              <IconMdiBellOutline />
              {{ i18n.t("flagReview.loadNotifications") }}
            </Button>
          </div>

          <ul v-if="currentId === item.id" class="mt-3 flex flex-col gap-2 rounded-md background-card px-3 py-2 sm:ml-12">
            <li v-if="notifications.length === 0" class="text-sm text-gray-secondary">{{ i18n.t("flagReview.noNotifications") }}</li>
            <li v-for="notification in notifications" v-else :key="notification.id" class="flex items-start gap-1.5 text-xs">
              <IconMdiInformationOutline v-if="notification.type === 'info'" class="mt-0.5 flex-shrink-0 text-sky-500" />
              <IconMdiAlertOutline v-else class="mt-0.5 flex-shrink-0 text-red-500" />
              <span>
                From {{ notification.originUserName }} to {{ notification.userId === item.userId ? "the reporter" : "the project's members" }}:
                {{ i18n.t(notification.message[0]!, notification.message.slice(1)).split(":")[1] }}
              </span>
            </li>
          </ul>
        </template>
      </li>
      <li v-if="pages > 1" class="p-3">
        <PaginationButtons :page="page" :pages="pages" @update:page="(value) => (page = value)" />
      </li>
    </ul>

    <div v-else class="flex flex-col items-center px-4 py-10 text-center">
      <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
        <IconMdiFlagOutline />
      </div>
      <p class="text-gray-secondary">{{ emptyText }}</p>
    </div>
  </Card>
</template>
