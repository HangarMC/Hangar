<script lang="ts" setup>
import PageTitle from "~/components/design/PageTitle.vue";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useActionLogs } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import Card from "~/components/design/Card.vue";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import Link from "~/components/design/Link.vue";
import MarkdownModal from "~/components/modals/MarkdownModal.vue";
import DiffModal from "~/components/modals/DiffModal.vue";
import Button from "~/components/design/Button.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const loggedActions = await useActionLogs().catch((e) => handleRequestError(e, ctx, i18n));

const headers = [
  { title: i18n.t("userActionLog.user"), name: "user", sortable: false },
  { title: i18n.t("userActionLog.address"), name: "address", sortable: false },
  { title: i18n.t("userActionLog.time"), name: "time", sortable: false },
  { title: i18n.t("userActionLog.action"), name: "action", sortable: false },
  { title: i18n.t("userActionLog.context"), name: "context", sortable: false },
  { title: i18n.t("userActionLog.oldState"), name: "oldState", sortable: false },
  { title: i18n.t("userActionLog.newState"), name: "newState", sortable: false },
] as Header[];
// TODO add support for loading more

useHead(useSeo(i18n.t("userActionLog.title"), null, route, null));
</script>

<template>
  <PageTitle>{{ i18n.t("userActionLog.title") }}</PageTitle>
  <Card>
    <SortableTable :headers="headers" :items="loggedActions?.result">
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
        <template v-if="item.page">
          <Link :to="'/' + item.project.owner + '/' + item.project.slug + '/pages/' + item.page.slug">
            {{ item.project.owner + "/" + item.project.slug + "/" + item.page.slug }}
          </Link>
        </template>
        <template v-else-if="item.version">
          <Link :to="'/' + item.project.owner + '/' + item.project.slug + '/versions/' + item.version.versionString">
            {{ `${item.project.owner}/${item.project.slug}/${item.version.versionString}/${item.version.platforms[0].toLowerCase()}` }}
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
        <template v-if="item.contextType === 'PAGE' && item.oldState">
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
        <template v-if="item.contextType === 'PAGE'">
          <MarkdownModal :markdown="item.newState" :title="i18n.t('userActionLog.markdownView')">
            <template #activator="{ on }">
              <Button size="small" v-on="on">
                {{ i18n.t("userActionLog.markdownView") }}
              </Button>
            </template>
          </MarkdownModal>
          <DiffModal :left="item.oldState" :right="item.newState" :title="i18n.t('userActionLog.diffView')">
            <template #activator="{ on }">
              <Button size="small" class="ml-2" v-on="on">
                {{ i18n.t("userActionLog.diffView") }}
              </Button>
            </template>
          </DiffModal>
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
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["VIEW_LOGS"]
</route>
