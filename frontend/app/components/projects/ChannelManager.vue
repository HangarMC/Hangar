<script lang="ts" setup>
import { ChannelFlag } from "#shared/types/backend";
import type { HangarChannel, HangarProject, ProjectChannel } from "#shared/types/backend";

const props = defineProps<{
  project?: HangarProject;
}>();

const i18n = useI18n();
const route = useRoute("user-project-settings-slug");
const { channels, refreshChannels } = useProjectChannels(() => route.params.project);
const validations = useBackendData.validations;
const notifications = useNotificationStore();

const maxChannels = computed(() => validations.project.maxChannelCount);
const atLimit = computed(() => (channels.value?.length ?? 0) >= maxChannels.value);

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
  <div>
    <ul v-if="channels?.length" class="divide-y divide-gray-300 border-t border-gray-300 dark:divide-gray-700 dark:border-gray-700">
      <li v-for="channel in channels" :key="channel.name" class="flex flex-wrap items-center gap-x-3 gap-y-2 py-2.5">
        <ChannelTile :channel="channel" />

        <div class="min-w-0 flex-1">
          <div class="flex items-center gap-2">
            <span class="truncate font-semibold">{{ channel.name }}</span>
            <Tooltip v-if="channel.flags.includes(ChannelFlag.FROZEN)">
              <template #content>{{ i18n.t("channel.manage.frozen") }}</template>
              <IconMdiLock class="flex-shrink-0 text-gray-secondary" />
            </Tooltip>
          </div>
          <p class="truncate text-sm text-gray-secondary">{{ channel.description || "—" }}</p>
        </div>

        <span class="inline-flex flex-shrink-0 items-center gap-1.5 text-sm text-gray-secondary tabular-nums">
          <IconMdiFormatListNumbered class="flex-shrink-0" />
          {{ i18n.t("channel.manage.versionCount") }}: {{ channel.versionCount }}
        </span>

        <div class="flex flex-shrink-0 items-center gap-1">
          <ChannelModal v-if="project" :project-id="project.id" edit :channel="channel" @create="editChannel">
            <template #activator="{ on }">
              <Button variant="ghost" tone="neutral" size="sm" icon-only :title="i18n.t('general.edit')" :aria-label="i18n.t('general.edit')" v-on="on">
                <IconMdiPencil />
              </Button>
            </template>
          </ChannelModal>
          <Button
            v-if="channels.length > 1 && channel.versionCount === 0 && !channel.flags.includes(ChannelFlag.FROZEN)"
            variant="ghost"
            tone="danger"
            size="sm"
            icon-only
            :title="i18n.t('channel.manage.deleteButton')"
            :aria-label="i18n.t('channel.manage.deleteButton')"
            @click="deleteChannel(channel)"
          >
            <IconMdiDelete />
          </Button>
        </div>
      </li>
    </ul>
    <p v-else class="border-t border-gray-300 py-6 text-center text-sm text-gray-secondary dark:border-gray-700">{{ i18n.t("channel.manage.empty") }}</p>

    <div class="flex flex-wrap items-center gap-3 border-t border-gray-300 pt-4 dark:border-gray-700">
      <ChannelModal v-if="project" :project-id="project.id" @create="addChannel">
        <template #activator="{ on }">
          <Button variant="outline" tone="neutral" :disabled="atLimit" v-on="on">
            <IconMdiPlus />
            {{ i18n.t("channel.manage.add") }}
          </Button>
        </template>
      </ChannelModal>
      <span class="text-sm text-gray-secondary tabular-nums">{{ channels?.length ?? 0 }}/{{ maxChannels }}</span>
    </div>
  </div>
</template>
