<script lang="ts" setup>
import type { HangarProjectFlag, HangarProjectFlagNotification, PaginatedResultHangarProjectFlag, Visibility } from "#shared/types/backend";

const props = defineProps<{
  resolved?: boolean;
  // when set, renders this fixed list read-only instead of fetching the resolved/unresolved queue
  flags?: HangarProjectFlag[];
}>();

const i18n = useI18n();
const isReadonly = computed(() => props.flags !== undefined);

function useQueueFlags() {
  if (isReadonly.value) {
    return { flags: undefined };
  }
  return props.resolved ? useResolvedFlags() : useUnresolvedFlags();
}
const { flags: queueFlags } = useQueueFlags();

const loading = ref<{ [key: number]: boolean }>({});

const items = computed<HangarProjectFlag[]>(() => props.flags ?? queueFlags?.value?.result ?? []);

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

function resolve(flag: HangarProjectFlag) {
  loading.value[flag.id] = true;
  useInternalApi(`flags/${flag.id}/resolve/${props.resolved ? "false" : "true"}`, "POST")
    .catch<any>((err) => handleRequestError(err))
    .then(async () => {
      if (!(queueFlags && queueFlags.value)) {
        return;
      }

      const newFlags = await useInternalApi<PaginatedResultHangarProjectFlag>("flags/" + (props.resolved ? "resolved" : "unresolved")).catch((err) =>
        handleRequestError(err)
      );
      if (newFlags) {
        queueFlags.value = newFlags;
      }
    })
    .finally(() => {
      loading.value[flag.id] = false;
    });
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
    <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
      <h2 class="flex-grow text-lg font-bold">{{ headerTitle }}</h2>
      <span class="text-sm text-gray-secondary tabular-nums">{{ items.length }}</span>
    </div>

    <ul v-if="items.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
      <Pagination :items="items">
        <template #default="{ item }">
          <li class="px-4 py-3">
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
                  <span class="text-xs text-gray-secondary" :title="i18n.d(item.createdAt, 'time')">{{ lastUpdated(new Date(item.createdAt)) }}</span>
                  <span v-if="item.resolved && item.resolvedByName" class="text-xs text-gray-secondary">
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
        </template>
        <template #pagination="{ page, pages, updatePage }">
          <li class="p-3">
            <PaginationButtons :page="page" :pages="pages" @update:page="updatePage" />
          </li>
        </template>
      </Pagination>
    </ul>

    <div v-else class="flex flex-col items-center px-4 py-10 text-center">
      <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
        <IconMdiFlagOutline />
      </div>
      <p class="text-gray-secondary">{{ emptyText }}</p>
    </div>
  </Card>
</template>
