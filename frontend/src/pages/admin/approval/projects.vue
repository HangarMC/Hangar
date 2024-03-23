<script lang="ts" setup>
import type { ProjectApprovals } from "~/types/backend";

definePageMeta({
  globalPermsRequired: ["Reviewer"],
});

const i18n = useI18n();
const route = useRoute("admin-approval-projects");
const data = (await useInternalApi<ProjectApprovals>("admin/approval/projects").catch((e) => handleRequestError(e))) as ProjectApprovals;

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
