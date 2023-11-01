<script lang="ts" setup>
import type { User } from "hangar-api";
import { useI18n } from "vue-i18n";
import type { HangarProject, ProjectChannel } from "hangar-internal";
import { useHead } from "@unhead/vue";
import { useRoute } from "vue-router";
import Card from "~/components/design/Card.vue";
import { ChannelFlag } from "~/types/enums";
import { useProjectChannels } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useInternalApi } from "~/composables/useApi";
import Table from "~/components/design/Table.vue";
import Tag from "~/components/Tag.vue";
import Button from "~/components/design/Button.vue";
import { useBackendData } from "~/store/backendData";
import ChannelModal from "~/components/modals/ChannelModal.vue";
import { useSeo } from "~/composables/useSeo";
import { useNotificationStore } from "~/store/notification";
import { definePageMeta } from "#imports";

definePageMeta({
  projectPermsRequired: ["EDIT_CHANNELS"],
});

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();
const i18n = useI18n();
const route = useRoute();
const channelData = await useProjectChannels(props.project.namespace.slug);
const channels = channelData.data;
const validations = useBackendData.validations;
const notifications = useNotificationStore();

useHead(useSeo("Channels | " + props.project.name, props.project.description, route, props.project.avatarUrl));

async function deleteChannel(channel: ProjectChannel) {
  await useInternalApi(`channels/${props.project.id}/delete/${channel.id}`, "post")
    .then(() => {
      channelData.refresh();
      notifications.warn(i18n.t("channel.modal.success.deletedChannel", [channel.name]));
    })
    .catch((e) => handleRequestError(e));
}

async function addChannel(channel: ProjectChannel) {
  await useInternalApi(`channels/${props.project.id}/create`, "post", {
    name: channel.name,
    description: channel.description,
    color: channel.color,
    flags: channel.flags,
  })
    .then(() => {
      channelData.refresh();
      notifications.success(i18n.t("channel.modal.success.addedChannel", [channel.name]));
    })
    .catch((e) => handleRequestError(e));
}

async function editChannel(channel: ProjectChannel) {
  if (!channel.id) return;
  await useInternalApi(`channels/${props.project.id}/edit`, "post", {
    id: channel.id,
    name: channel.name,
    description: channel.description,
    color: channel.color,
    flags: channel.flags,
  })
    .then(() => {
      channelData.refresh();
      notifications.success(i18n.t("channel.modal.success.editedChannel", [channel.name]));
    })
    .catch((e) => handleRequestError(e));
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
            <ChannelModal :project-id="props.project.id" edit :channel="channel" @create="editChannel">
              <template #activator="{ on, attrs }">
                <Button v-bind="attrs" v-on="on">
                  {{ i18n.t("channel.manage.editButton") }}
                </Button>
              </template>
            </ChannelModal>
          </td>
          <td v-if="channels.length !== 1">
            <Button v-if="channel.versionCount === 0" :disabled="channel.flags.indexOf(ChannelFlag.FROZEN) > -1" @click="deleteChannel(channel)">
              {{ i18n.t("channel.manage.deleteButton") }}
            </Button>
          </td>
        </tr>
      </tbody>
    </Table>

    <ChannelModal :project-id="props.project.id" @create="addChannel">
      <template #activator="{ on, attrs }">
        <Button
          v-if="channels.length < validations.project.maxChannelCount"
          :disabled="channels.length >= validations.project.maxChannelCount"
          class="mt-2"
          v-bind="attrs"
          v-on="on"
        >
          <IconMdiPlus />
          {{ i18n.t("channel.manage.add") }}
        </Button>
      </template>
    </ChannelModal>
  </Card>
</template>
