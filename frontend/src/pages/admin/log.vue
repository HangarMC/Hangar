<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useHead } from "@vueuse/head";
import { PaginatedResult } from "hangar-api";
import { computed, ref } from "vue";
import { LoggedAction } from "hangar-internal";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import { useActionLogs } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import Card from "~/lib/components/design/Card.vue";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import Link from "~/lib/components/design/Link.vue";
import MarkdownModal from "~/components/modals/MarkdownModal.vue";
import DiffModal from "~/components/modals/DiffModal.vue";
import Button from "~/lib/components/design/Button.vue";
import { useSeo } from "~/composables/useSeo";
import { definePageMeta, useInternalApi } from "#imports";

definePageMeta({
  globalPermsRequired: ["VIEW_LOGS"],
});

const i18n = useI18n();
const route = useRoute();
const loggedActions = await useActionLogs().catch((e) => handleRequestError(e));

// TODO add support for sorting
const headers = [
  { title: i18n.t("userActionLog.user"), name: "user", sortable: false },
  { title: i18n.t("userActionLog.address"), name: "address", sortable: false },
  { title: i18n.t("userActionLog.time"), name: "time", sortable: false },
  { title: i18n.t("userActionLog.action"), name: "action", sortable: false },
  { title: i18n.t("userActionLog.context"), name: "context", sortable: false },
  { title: i18n.t("userActionLog.oldState"), name: "oldState", sortable: false },
  { title: i18n.t("userActionLog.newState"), name: "newState", sortable: false },
] as Header[];

const page = ref(0);
const sort = ref<string[]>([]);
const requestParams = computed(() => {
  const limit = 25;
  return {
    limit,
    offset: page.value * limit,
    sort: sort.value,
  };
});

async function updateSort(col: string, sorter: Record<string, number>) {
  sort.value = [...Object.keys(sorter)]
    .map((k) => {
      const val = sorter[k];
      if (val === -1) return "-" + k;
      if (val === 1) return k;
      return null;
    })
    .filter((v) => v !== null) as string[];

  await update();
}

async function updatePage(newPage: number) {
  page.value = newPage;
  await update();
}

async function update() {
  loggedActions.value = await useInternalApi<PaginatedResult<LoggedAction>>("admin/log/", "GET", requestParams.value);
}

useHead(useSeo(i18n.t("userActionLog.title"), null, route, null));
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("userActionLog.title") }}</PageTitle>
    <Card>
      <SortableTable
        :headers="headers"
        :items="loggedActions?.result"
        :server-pagination="loggedActions?.pagination"
        @update:sort="updateSort"
        @update:page="updatePage"
      >
        <template #item_user="{ item }">
          <Link :to="'/' + item.userName">{{ item.userName }}</Link>
        </template>
        <template #item_time="{ item }">
          {{ i18n.d(item.createdAt, "time") }}
        </template>
        <template #item_action="{ item }">
          {{ i18n.t(item.action.description) }}
        </template>
        <template #item_context="{ item }">
          <template v-if="item.project && item.page">
            <Link :to="'/' + item.project.owner + '/' + item.project.slug + '/pages/' + item.page.slug">
              {{ item.project.owner + "/" + item.project.slug + "/" + item.page.slug }}
            </Link>
          </template>
          <template v-else-if="item.version && item.project">
            <Link :to="'/' + item.project.owner + '/' + item.project.slug + '/versions/' + item.version.versionString">
              {{ `${item.project.owner}/${item.project.slug}/${item.version.versionString}` }}
            </Link>
          </template>
          <template v-else-if="item.project && item.project.owner">
            <Link :to="'/' + item.project.owner + '/' + item.project.slug">{{ item.project.owner + "/" + item.project.slug }} </Link>
          </template>
          <template v-else-if="item.subject">
            <Link :to="'/' + item.subject.name">{{ item.subject.name }}</Link>
          </template>
        </template>
        <template #item_oldState="{ item }">
          <template v-if="(item.contextType === 'PAGE' || item.action.pgLoggedAction === 'version_description_changed') && item.oldState">
            <MarkdownModal :markdown="item.oldState" :title="i18n.t('userActionLog.markdownView')">
              <template #activator="{ on }">
                <Button size="small" v-on="on">
                  {{ i18n.t("userActionLog.markdownView") }}
                </Button>
              </template>
            </MarkdownModal>
          </template>
          <template v-else-if="item.action.pgLoggedAction === 'project_icon_changed'">
            <span v-if="item.oldState === '#empty'">default</span>
            <img v-else class="inline-img" :src="'data:image/png;base64,' + item.oldState" alt="" />
          </template>
          <template v-else>
            <span>{{ item.oldState && i18n.te(item.oldState) ? i18n.t(item.oldState) : item.oldState }}</span>
          </template>
        </template>
        <template #item_newState="{ item }">
          <template v-if="item.contextType === 'PAGE' || item.action.pgLoggedAction === 'version_description_changed'">
            <div class="flex gap-2">
              <MarkdownModal :markdown="item.newState" :title="i18n.t('userActionLog.markdownView')">
                <template #activator="{ on }">
                  <Button size="small" v-on="on">
                    {{ i18n.t("userActionLog.markdownView") }}
                  </Button>
                </template>
              </MarkdownModal>
              <DiffModal :left="item.oldState" :right="item.newState" :title="i18n.t('userActionLog.diffView')">
                <template #activator="{ on }">
                  <Button size="small" v-on="on">
                    {{ i18n.t("userActionLog.diffView") }}
                  </Button>
                </template>
              </DiffModal>
            </div>
          </template>
          <template v-else-if="item.action.pgLoggedAction === 'project_icon_changed'">
            <span v-if="item.newState === '#empty'">default</span>
            <img v-else class="inline-img" :src="'data:image/png;base64,' + item.newState" alt="" />
          </template>
          <template v-else>
            <span>{{ i18n.te(item.newState) ? i18n.t(item.newState) : item.newState }}</span>
          </template>
        </template>
      </SortableTable>
    </Card>
  </div>
</template>
