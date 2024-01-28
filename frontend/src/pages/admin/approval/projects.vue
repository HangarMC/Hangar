<script lang="ts" setup>
import type { ProjectApproval } from "hangar-internal";

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
