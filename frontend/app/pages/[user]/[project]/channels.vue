<script lang="ts" setup>
import { ChannelFlag } from "#shared/types/backend";
import type { HangarChannel, HangarProject, ProjectChannel, User } from "#shared/types/backend";

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
  <Card>
    <template #header>{{ i18n.t("channel.manage.title") }}</template>
    <p class="mb-2">{{ i18n.t("channel.manage.subtitle") }}</p>

    <Table v-if="channels" class="w-full">
      <thead>
        <tr>
          <th>
            <span class="inline-flex items-center gap-1"><IconMdiTag />{{ i18n.t("channel.manage.channelName") }}</span>
          </th>
          <th>
            <span class="inline-flex items-center gap-1">{{ i18n.t("channel.manage.channelDescription") }}</span>
          </th>
          <th>
            <span class="inline-flex items-center gap-1"><IconMdiFormatListNumbered />{{ i18n.t("channel.manage.versionCount") }}</span>
          </th>
          <th />
          <th v-if="channels.length !== 1" />
        </tr>
      </thead>
      <tbody>
        <tr v-for="channel in channels" :key="channel.name">
          <td><Tag :name="channel.name" :color="{ background: channel.color }" :tooltip="channel.description" /></td>
          <td>{{ channel.description }}</td>
          <td>{{ channel.versionCount }}</td>
          <td>
            <ChannelModal v-if="project" :project-id="project.id" edit :channel="channel" @create="editChannel">
              <template #activator="{ on }">
                <Button v-on="on">
                  {{ i18n.t("channel.manage.editButton") }}
                </Button>
              </template>
            </ChannelModal>
          </td>
          <td v-if="channels.length !== 1">
            <Button v-if="channel.versionCount === 0" :disabled="channel.flags.includes(ChannelFlag.FROZEN)" @click="deleteChannel(channel)">
              {{ i18n.t("channel.manage.deleteButton") }}
            </Button>
          </td>
        </tr>
      </tbody>
    </Table>

    <ChannelModal v-if="project" :project-id="project.id" @create="addChannel">
      <template #activator="{ on }">
        <Button
          v-if="channels && channels.length < validations.project.maxChannelCount"
          :disabled="channels.length >= validations.project.maxChannelCount"
          class="mt-2"
          v-on="on"
        >
          <IconMdiPlus />
          {{ i18n.t("channel.manage.add") }}
        </Button>
      </template>
    </ChannelModal>
  </Card>
</template>
