<script lang="ts" setup>
import { ProjectApproval } from "hangar-internal";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import Card from "~/components/design/Card.vue";
import AdminProjectList from "~/components/projects/AdminProjectList.vue";

const ctx = useContext();
const i18n = useI18n();
const data = await useInternalApi<{ needsApproval: ProjectApproval[]; waitingProjects: ProjectApproval[] }>("admin/approval/projects").catch((e) =>
  handleRequestError(e, ctx, i18n)
);
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <Card>
      <template #header>{{ i18n.t("projectApproval.awaitingChanges") }}</template>

      <AdminProjectList :projects="data.waitingProjects" />
    </Card>
    <Card>
      <template #header>{{ i18n.t("projectApproval.needsApproval") }}</template>

      <AdminProjectList :projects="data.needsApproval" />
    </Card>
  </div>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["REVIEWER"]
</route>
