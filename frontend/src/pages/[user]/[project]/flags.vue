<script lang="ts" setup>
import Card from "~/lib/components/design/Card.vue";
import Link from "~/lib/components/design/Link.vue";
import { User } from "hangar-api";
import { useI18n } from "vue-i18n";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import Alert from "~/lib/components/design/Alert.vue";
import { useContext } from "vite-ssr/vue";
import { useProjectFlags } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { Flag, HangarProject } from "hangar-internal";
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
const flags = await useProjectFlags(props.project.id).catch((e) => handleRequestError(e, ctx, i18n));

const headers = [
  { title: "Submitter", name: "user" },
  { title: "Reason", name: "reason" },
  { title: "Comment", name: "comment" },
  { title: "When", name: "createdAt" },
  { title: "Resolved", name: "resolved" },
] as Header[];

useHead(useSeo("Flags | " + props.project.name, props.project.description, route, projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)));
</script>

<template>
  <Card>
    <template #header>
      {{ i18n.t("flags.header") }}
      <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
        {{ project.namespace.owner + "/" + project.namespace.slug }}
      </Link>
    </template>

    <SortableTable v-if="flags" :items="flags" :headers="headers">
      <template #empty>
        <Alert type="info">
          {{ i18n.t("flags.noFlags") }}
        </Alert>
      </template>
      <template #item_user="{ item }">
        <Link :to="'/' + item.reportedByName">{{ item.reportedByName }}</Link>
      </template>
      <template #item_reason="{ item }">
        {{ i18n.t(item.reason) }}
      </template>
      <template #item_createdAt="{ item }">
        {{ i18n.d(item.createdAt, "time") }}
      </template>
      <template #item_resolved="{ item }">
        <span v-if="item.resolved">{{ i18n.t("flags.resolved", [item.resolvedByName, i18n.d(item.resolvedAt, "date")]) }}</span>
        <span v-else v-text="i18n.t('flags.notResolved')" />
      </template>
    </SortableTable>
  </Card>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["MOD_NOTES_AND_FLAGS"]
</route>
