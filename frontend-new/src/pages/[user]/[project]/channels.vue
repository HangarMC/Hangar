<script lang="ts" setup>
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import { User } from "hangar-api";
import { useI18n } from "vue-i18n";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import Alert from "~/components/design/Alert.vue";
import { useContext } from "vite-ssr/vue";
import { useProjectChannels } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { HangarProject, ProjectChannel } from "hangar-internal";
import { useInternalApi } from "~/composables/useApi";
import Table from "~/components/design/Table.vue";
import Tag from "~/components/Tag.vue";
import Button from "~/components/design/Button.vue";
import { useBackendDataStore } from "~/store/backendData";
import ChannelModal from "~/components/modals/ChannelModal.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { useRoute } from "vue-router";

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();
const i18n = useI18n();
const ctx = useContext();
const route = useRoute();
const channels = await useProjectChannels(props.project.namespace.owner, props.project.namespace.slug).catch((e) => handleRequestError(e, ctx, i18n));
const validations = useBackendDataStore().validations;

useHead(
  useSeo("Channels | " + props.project.name, props.project.description, route, projectIconUrl(props.project.namespace.owner, props.project.namespace.slug))
);

async function refreshChannels() {
  const newChannels = await useInternalApi<ProjectChannel[]>(`channels/${props.project.namespace.owner}/${props.project.namespace.slug}`, false).catch((e) =>
    handleRequestError(e, ctx, i18n)
  );
  if (channels && newChannels) {
    channels.value = newChannels;
  }
}

async function deleteChannel(channel: ProjectChannel) {
  await useInternalApi(`channels/${props.project.id}/delete/${channel.id}`, true, "post");
  await refreshChannels();
}

async function addChannel(channel: ProjectChannel) {
  if (!channel.id) return;
  await useInternalApi(`channels/${props.project.id}/create`, true, "post", {
    name: channel.name,
    color: channel.color,
    nonReviewed: channel.nonReviewed,
  }).catch((e) => handleRequestError(e, ctx, i18n));
  await refreshChannels();
}

async function editChannel(channel: ProjectChannel) {
  if (!channel.id) return;
  await useInternalApi(`channels/${props.project.id}/edit`, true, "post", {
    id: channel.id,
    name: channel.name,
    color: channel.color,
    nonReviewed: channel.nonReviewed,
  }).catch((e) => handleRequestError(e, ctx, i18n));
  await refreshChannels();
}
</script>

<template>
  <Card>
    <template #header>{{ i18n.t("channel.manage.title") }}</template>
    <p class="mb-2">{{ i18n.t("channel.manage.subtitle") }}</p>

    <Table class="w-full">
      <thead>
        <tr>
          <th><IconMdiTag />{{ i18n.t("channel.manage.channelName") }}</th>
          <th><IconMdiFormatListNumbered />{{ i18n.t("channel.manage.versionCount") }}</th>
          <th><IconMdiFileFind />{{ i18n.t("channel.manage.reviewed") }}</th>
          <th><IconMdiPencil />{{ i18n.t("channel.manage.edit") }}</th>
          <th v-if="channels.length !== 1"><IconMdiDelete />{{ i18n.t("channel.manage.trash") }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="channel in channels" :key="channel.name">
          <td><Tag :name="channel.name" :color="{ background: channel.color }" /></td>
          <td>{{ channel.versionCount }}</td>
          <td>
            <IconMdiCheckboxBlankCircleOutline v-if="channel.nonReviewed" />
            <IconMdiCheckCircle v-else />
          </td>
          <td>
            <ChannelModal :project-id="props.project.id" edit :channel="channel" @create="editChannel">
              <template #activator="{ on, attrs }">
                <Button v-bind="attrs" :disabled="!channel.editable" v-on="on">
                  {{ i18n.t("channel.manage.editButton") }}
                </Button>
              </template>
            </ChannelModal>
          </td>
          <td v-if="channels.length !== 1">
            <Button v-if="channel.versionCount === 0" :disabled="!channel.editable" @click="deleteChannel(channel)">
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

<route lang="yaml">
meta:
  requireProjectPerm: ["EDIT_TAGS"]
</route>
