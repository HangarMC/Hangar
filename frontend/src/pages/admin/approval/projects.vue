<script lang="ts" setup>
import type { ProjectApproval } from "hangar-internal";
import { useI18n } from "vue-i18n";
import { useHead } from "@unhead/vue";
import { useRoute } from "vue-router";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import Card from "~/components/design/Card.vue";
import AdminProjectList from "~/components/projects/AdminProjectList.vue";
import { useSeo } from "~/composables/useSeo";
import { definePageMeta } from "#imports";

interface ApprovalProjects {
  needsApproval: ProjectApproval[];
  waitingProjects: ProjectApproval[];
}

definePageMeta({
  globalPermsRequired: ["REVIEWER"],
});

const i18n = useI18n();
const route = useRoute();
const data: ApprovalProjects = (await useInternalApi<ApprovalProjects>("admin/approval/projects").catch((e) => handleRequestError(e))) as ApprovalProjects;

useHead(useSeo(i18n.t("projectApproval.title"), null, route, null));
</script>

<template>
  <div class="space-y-3">
    <Card>
      <template #header>{{ i18n.t("projectApproval.needsApproval") }}</template>
      <AdminProjectList :projects="data.needsApproval" />
    </Card>
    <Card>
      <template #header>{{ i18n.t("projectApproval.awaitingChanges") }}</template>
      <AdminProjectList :projects="data.waitingProjects" />
    </Card>
  </div>
</template>
