<script lang="ts" setup>
import type { Header } from "#shared/types/components/SortableTable";
import type { HangarProject, User } from "#shared/types/backend";

definePageMeta({
  projectPermsRequired: ["ModNotesAndFlags"],
});

const props = defineProps<{
  user?: User;
  project?: HangarProject;
}>();
const i18n = useI18n();
const route = useRoute("user-project-flags");
const { flags } = useProjectFlags(() => route.params.project);

const headers = [
  { title: "Submitter", name: "user" },
  { title: "Reason", name: "reason" },
  { title: "Comment", name: "comment" },
  { title: "When", name: "createdAt" },
  { title: "Resolved", name: "resolved" },
] as const satisfies Header<string>[];

useSeo(computed(() => ({ title: "Flags | " + props.project?.name, route, description: props.project?.description, image: props.project?.avatarUrl })));
</script>

<template>
  <Card>
    <template #header>
      {{ i18n.t("flags.header") }}
      <Skeleton v-if="!project" />
      <Link v-else :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
        {{ project.namespace.owner + "/" + project.namespace.slug }}
      </Link>
    </template>

    <SortableTable :items="flags || []" :headers="headers">
      <template #empty>
        <Alert type="info">
          {{ i18n.t("flags.noFlags") }}
        </Alert>
      </template>
      <template #user="{ item }">
        <Link :to="'/' + item.reportedByName">{{ item.reportedByName }}</Link>
      </template>
      <template #reason="{ item }">
        {{ i18n.t(item.reason) }}
      </template>
      <template #createdAt="{ item }">
        {{ i18n.d(item.createdAt, "time") }}
      </template>
      <template #resolved="{ item }">
        <span v-if="item.resolved">{{ i18n.t("flags.resolved", [item.resolvedByName, i18n.d(item.resolvedAt, "date")]) }}</span>
        <span v-else v-text="i18n.t('flags.notResolved')" />
      </template>
    </SortableTable>
  </Card>
</template>
