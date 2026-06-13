<script lang="ts" setup>
import { ChannelFlag } from "#shared/types/backend";
import type { HangarChannel, HangarProject, ProjectChannel, User } from "#shared/types/backend";
import type { Header } from "#shared/types/components/SortableTable";

definePageMeta({
  projectPermsRequired: ["EditChannels"],
});

const props = defineProps<{
  user?: User;
  project?: HangarProject;
}>();
const i18n = useI18n();
const route = useRoute("user-project-channels");
const { channels, refreshChannels } = useProjectChannels(() => route.params.project);
const validations = useBackendData.validations;
const notifications = useNotificationStore();
const headers = [
  { name: "name", title: "Name", sortable: false },
  { name: "description", title: "Description", sortable: false },
  { name: "versionCount", title: "Versions", sortable: false },
  { name: "actions", title: "", sortable: false },
] as const satisfies Header<string>[];

useSeo(computed(() => ({ title: "Channels | " + props.project?.name, route, description: props.project?.description, image: props.project?.avatarUrl })));

async function deleteChannel(channel: HangarChannel) {
  await useInternalApi(`channels/${props.project?.id}/delete/${channel.id}`, "post")
    .then(() => {
      refreshChannels();
      notifications.warn(i18n.t("channel.modal.success.deletedChannel", [channel.name]));
    })
    .catch((err) => handleRequestError(err));
}

async function addChannel(channel: HangarChannel | ProjectChannel) {
  await useInternalApi(`channels/${props.project?.id}/create`, "post", {
    name: channel.name,
    description: channel.description,
    color: channel.color,
    flags: channel.flags,
  })
    .then(() => {
      refreshChannels();
      notifications.success(i18n.t("channel.modal.success.addedChannel", [channel.name]));
    })
    .catch((err) => handleRequestError(err));
}

async function editChannel(channel: HangarChannel | ProjectChannel) {
  if (!("id" in channel)) return;
  await useInternalApi(`channels/${props.project?.id}/edit`, "post", {
    id: channel.id,
    name: channel.name,
    description: channel.description,
    color: channel.color,
    flags: channel.flags,
  })
    .then(() => {
      refreshChannels();
      notifications.success(i18n.t("channel.modal.success.editedChannel", [channel.name]));
    })
    .catch((err) => handleRequestError(err));
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <Card class="!p-0 overflow-hidden">
      <div class="flex justify-end px-3 pt-3">
        <ChannelModal v-if="project" :project-id="project.id" @create="addChannel">
          <template #activator="{ on }">
            <Button
              v-if="channels && channels.length < validations.project.maxChannelCount"
              size="small"
              class="!h-8 !px-2 !py-1 text-sm"
              :disabled="channels.length >= validations.project.maxChannelCount"
              v-on="on"
            >
              <IconMdiPlus class="mr-1 text-base" />
              {{ i18n.t("channel.manage.add") }}
            </Button>
          </template>
        </ChannelModal>
      </div>

      <SortableTable v-if="channels" :headers="headers" :items="channels">
        <template #name="{ item }">
          <span class="inline-flex items-center gap-2 font-semibold">
            <span class="h-3 w-3 flex-shrink-0 rounded-sm" :style="{ backgroundColor: item.color }" />
            {{ item.name }}
          </span>
        </template>
        <template #description="{ item }">
          <span class="text-gray">{{ item.description }}</span>
        </template>
        <template #versionCount="{ item }">
          <span>{{ item.versionCount }}</span>
        </template>
        <template #actions="{ item }">
          <div class="flex justify-end gap-1">
            <ChannelModal v-if="project" :project-id="project.id" edit :channel="item" @create="editChannel">
              <template #activator="{ on }">
                <Button button-type="borderless" class="!h-9 !w-9 !p-0" aria-label="Edit channel" v-on="on">
                  <IconMdiPencil />
                </Button>
              </template>
            </ChannelModal>
            <button
              v-if="channels.length !== 1 && item.versionCount === 0"
              type="button"
              class="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-transparent transition-all duration-250 hover:border-red-600 hover:bg-red-900/50 disabled:cursor-not-allowed disabled:opacity-50"
              :disabled="item.flags.includes(ChannelFlag.FROZEN)"
              aria-label="Delete channel"
              @click="deleteChannel(item)"
            >
              <IconMdiDeleteOutline />
            </button>
          </div>
        </template>
        <template #empty>
          <div class="py-10 text-center text-gray">No channels found.</div>
        </template>
      </SortableTable>
    </Card>
  </div>
</template>
