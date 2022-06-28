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

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const data = await useInternalApi<{ needsApproval: ProjectApproval[]; waitingProjects: ProjectApproval[] }>("admin/approval/projects").catch((e) =>
  handleRequestError(e, ctx, i18n)
);

useHead(useSeo(i18n.t("projectApproval.title"), null, route, null));
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
