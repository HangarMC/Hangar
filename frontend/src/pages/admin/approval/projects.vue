<script lang="ts" setup>
import { ProjectApproval } from "hangar-internal";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import Card from "~/lib/components/design/Card.vue";
import AdminProjectList from "~/components/projects/AdminProjectList.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useRoute } from "vue-router";

interface ApprovalProjects {
  needsApproval: ProjectApproval[];
  waitingProjects: ProjectApproval[];
}

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const data: ApprovalProjects = (await useInternalApi<ApprovalProjects>("admin/approval/projects").catch((e) =>
  handleRequestError(e, ctx, i18n)
)) as ApprovalProjects;

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

<route lang="yaml">
meta:
  requireGlobalPerm: ["REVIEWER"]
</route>
