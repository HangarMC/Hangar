<script lang="ts" setup>
import type { HangarProjectFlag, HangarProjectFlagNotification, PaginatedResultHangarProjectFlag } from "#shared/types/backend";

const props = defineProps<{
  resolved: boolean;
}>();

const i18n = useI18n();
const { flags } = props.resolved ? useResolvedFlags() : useUnresolvedFlags();
const loading = ref<{ [key: number]: boolean }>({});

function resolve(flag: HangarProjectFlag) {
  loading.value[flag.id] = true;
  useInternalApi(`flags/${flag.id}/resolve/${props.resolved ? "false" : "true"}`, "POST")
    .catch<any>((err) => handleRequestError(err))
    .then(async () => {
      if (flags && flags.value) {
        const newFlags = await useInternalApi<PaginatedResultHangarProjectFlag>("flags/" + (props.resolved ? "resolved" : "unresolved")).catch((err) =>
          handleRequestError(err)
        );
        if (newFlags) {
          flags.value = newFlags;
        }
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
  <template v-if="flags && flags.result && flags.result.length > 0">
    <Pagination :items="flags.result">
      <template #default="{ item }">
        <Card class="mb-2">
          <div class="flex space-x-1 items-center">
            <UserAvatar :username="item.reportedByName" size="sm" class="flex-shrink-0" />
            <div class="flex flex-col flex-grow">
              <h2>
                <Link :to="'/' + item.reportedByName" target="_blank">
                  {{ item.reportedByName }}
                </Link>

                reported
                <Link :to="`/${item.projectNamespace.owner}/${item.projectNamespace.slug}`" target="_blank">
                  {{ `${item.projectNamespace.owner}/${item.projectNamespace.slug}` }}
                </Link>
                ({{ i18n.d(item.createdAt, "time") }})
              </h2>
              <small>{{ i18n.t("flagReview.line2", [i18n.t(item.reason)]) }}</small>
              <small>{{ i18n.t("flagReview.line3", [item.comment]) }}</small>
            </div>

            <template v-if="resolved">
              <Button v-if="currentId !== item.id" @click="getNotifications(item)">Load notifications</Button>
              <Button :disabled="loading[item.id]" @click="resolve(item)">
                <IconMdiCheck class="mr-1" />
                {{ i18n.t("flagReview.markUnresolved") }}
              </Button>
            </template>
            <template v-else>
              <span class="pr-1">Currently {{ item.projectVisibility }}</span>
              <VisibilityChangerModal :prop-visibility="item.projectVisibility" type="project" :post-url="`projects/visibility/${item.projectId}`" />
              <Button :disabled="loading[item.id]" @click="resolve(item)">
                <IconMdiCheck class="mr-1" />
                {{ i18n.t("flagReview.markResolved") }}
              </Button>
            </template>
          </div>

          <!-- todo: make this actually look good and work well -->
          <div class="flex-col mt-2">
            <div class="inline-flex items-center">
              <ReportNotificationModal :flag="item" :send-to-reporter="false" />
              <ReportNotificationModal :flag="item" :send-to-reporter="true" />
              <Button v-if="currentId !== item.id && !resolved" @click="getNotifications(item)">Load notifications</Button>
            </div>
            <ul v-if="currentId === item.id" class="mt-1">
              <li v-if="notifications.length === 0">Empty!</li>
              <li v-for="notification in notifications" v-else :key="notification.id" class="text-xs">
                <span class="inline-flex">
                  <IconMdiInformationOutline v-if="notification.type === 'info'" class="text-blue-400 mr-1" />
                  <IconMdiAlertOutline v-else class="text-red-500 mr-1" />
                  From {{ notification.originUserName }} to {{ notification.userId === item.userId ? "the reporter" : "the project's members" }}:
                  {{ i18n.t(notification.message[0]!, notification.message.slice(1)).split(":")[1] }}
                </span>
              </li>
            </ul>
          </div>
        </Card>
      </template>
    </Pagination>
  </template>
  <div v-else>
    {{ i18n.t("flagReview.noFlags") }}
  </div>
</template>
