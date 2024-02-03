<script lang="ts" setup>
interface ApprovalProjects {
  needsApproval: ProjectApproval[];
  waitingProjects: ProjectApproval[];
}

definePageMeta({
  globalPermsRequired: ["Reviewer"],
});

const i18n = useI18n();
const route = useRoute("admin-approval-projects");
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
