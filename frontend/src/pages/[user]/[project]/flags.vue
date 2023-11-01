<script lang="ts" setup>
import type { User } from "hangar-api";
import { useI18n } from "vue-i18n";
import type { HangarProject } from "hangar-internal";
import { useHead } from "@unhead/vue";
import { useRoute } from "vue-router";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import SortableTable from "~/components/SortableTable.vue";
import Alert from "~/components/design/Alert.vue";
import { useProjectFlags } from "~/composables/useApiHelper";
import { useSeo } from "~/composables/useSeo";
import { definePageMeta } from "#imports";
import type { Header } from "~/types/components/SortableTable";

definePageMeta({
  projectPermsRequired: ["MOD_NOTES_AND_FLAGS"],
});

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();
const i18n = useI18n();
const route = useRoute();
const flags = await useProjectFlags(props.project.id);

const headers: Header[] = [
  { title: "Submitter", name: "user" },
  { title: "Reason", name: "reason" },
  { title: "Comment", name: "comment" },
  { title: "When", name: "createdAt" },
  { title: "Resolved", name: "resolved" },
];

useHead(useSeo("Flags | " + props.project.name, props.project.description, route, props.project.avatarUrl));
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
