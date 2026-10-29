<script lang="ts" setup>
import type { ProjectApprovals } from "#shared/types/backend";

definePageMeta({
  globalPermsRequired: ["Reviewer"],
});

const i18n = useI18n();
const route = useRoute("admin-approval-projects");
const data = (await useInternalApi<ProjectApprovals>("admin/approval/projects").catch((err) => handleRequestError(err))) as ProjectApprovals;

const needsApproval = computed(() => data?.needsApproval ?? []);
const waitingProjects = computed(() => data?.waitingProjects ?? []);

useSeo(computed(() => ({ title: i18n.t("projectApproval.title"), route })));

function visibilityTitle(visibility: string) {
  const value = useBackendData.visibilities.find((v) => v.name === visibility);
  return value ? i18n.t(value.title) : visibility;
}
</script>

<template>
  <div>
    <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold">{{ i18n.t("projectApproval.title") }}</h1>
        <p class="mt-1 text-gray-secondary">{{ i18n.t("projectApproval.subtitle") }}</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <Button variant="outline" tone="neutral" to="/admin/approval/versions">
          <IconMdiFormatListChecks />
          {{ i18n.t("versionApproval.title") }}
        </Button>
      </div>
    </div>

    <div class="flex flex-col gap-4">
      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("projectApproval.needsApproval") }}</h2>
          <span class="text-sm text-gray-secondary tabular-nums">{{ needsApproval.length }}</span>
        </div>

        <ul v-if="needsApproval.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
          <Pagination :items="needsApproval" :items-per-page="15">
            <template #default="{ item: project }">
              <li class="flex flex-col gap-3 px-4 py-3 sm:flex-row sm:items-start">
                <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-amber-500/15 text-lg text-amber-500">
                  <IconMdiFolderAlertOutline />
                </div>
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-2">
                    <NuxtLink :to="`/${project.namespace.owner}/${project.namespace.slug}`" target="_blank" class="font-semibold">
                      {{ project.namespace.owner }}/{{ project.namespace.slug }}
                    </NuxtLink>
                    <Chip tone="neutral">{{ visibilityTitle(project.visibility) }}</Chip>
                  </div>
                  <div class="mt-0.5 text-xs text-gray-secondary">{{ i18n.t("projectApproval.requestedBy", [project.changeRequester]) }}</div>
                  <Markdown v-if="project.comment" :raw="project.comment" inline :show-toc="false" class="mt-1 text-sm" />
                </div>
                <VisibilityChangerModal
                  size="sm"
                  type="project"
                  class="flex-shrink-0 self-end sm:self-center"
                  :prop-visibility="project.visibility"
                  :post-url="`projects/visibility/${project.projectId}`"
                />
              </li>
            </template>
            <template #pagination="{ page, pages, updatePage }">
              <li class="p-3">
                <PaginationButtons :page="page" :pages="pages" @update:page="updatePage" />
              </li>
            </template>
          </Pagination>
        </ul>
        <div v-else class="flex flex-col items-center px-4 py-10 text-center">
          <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
            <IconMdiCheckDecagramOutline />
          </div>
          <p class="text-gray-secondary">{{ i18n.t("projectApproval.queueClear") }}</p>
        </div>
      </Card>

      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("projectApproval.awaitingChanges") }}</h2>
          <span class="text-sm text-gray-secondary tabular-nums">{{ waitingProjects.length }}</span>
        </div>

        <ul v-if="waitingProjects.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
          <Pagination :items="waitingProjects" :items-per-page="15">
            <template #default="{ item: project }">
              <li class="flex flex-col gap-3 px-4 py-3 sm:flex-row sm:items-start">
                <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-sky-500/15 text-lg text-sky-500">
                  <IconMdiClockOutline />
                </div>
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-2">
                    <NuxtLink :to="`/${project.namespace.owner}/${project.namespace.slug}`" target="_blank" class="font-semibold">
                      {{ project.namespace.owner }}/{{ project.namespace.slug }}
                    </NuxtLink>
                    <Chip tone="neutral">{{ visibilityTitle(project.visibility) }}</Chip>
                  </div>
                  <div class="mt-0.5 text-xs text-gray-secondary">{{ i18n.t("projectApproval.requestedBy", [project.changeRequester]) }}</div>
                  <Markdown v-if="project.comment" :raw="project.comment" inline :show-toc="false" class="mt-1 text-sm" />
                </div>
                <VisibilityChangerModal
                  size="sm"
                  variant="outline"
                  tone="neutral"
                  type="project"
                  class="flex-shrink-0 self-end sm:self-center"
                  :prop-visibility="project.visibility"
                  :post-url="`projects/visibility/${project.projectId}`"
                />
              </li>
            </template>
            <template #pagination="{ page, pages, updatePage }">
              <li class="p-3">
                <PaginationButtons :page="page" :pages="pages" @update:page="updatePage" />
              </li>
            </template>
          </Pagination>
        </ul>
        <div v-else class="flex flex-col items-center px-4 py-10 text-center">
          <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
            <IconMdiCheckDecagramOutline />
          </div>
          <p class="text-gray-secondary">{{ i18n.t("projectApproval.noAwaitingChanges") }}</p>
        </div>
      </Card>
    </div>
  </div>
</template>
