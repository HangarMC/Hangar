<script setup lang="ts">
import { PaginatedResult, Project } from "hangar-api";
import { PropType } from "vue";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { lastUpdated } from "~/composables/useTime";
import { useI18n } from "vue-i18n";
import Tooltip from "~/components/design/Tooltip.vue";
import Pagination from "~/components/Pagination.vue";

const i18n = useI18n();

const props = defineProps({
  projects: {
    type: Object as PropType<PaginatedResult<Project>>,
    required: true,
  },
});
</script>

<template>
  <Pagination :items="projects?.result">
    <template #default="{ item }">
      <Card class="flex space-x-4">
        <div>
          <UserAvatar
            :username="item.namespace.owner"
            :to="'/' + item.namespace.owner + '/' + item.name"
            :img-src="projectIconUrl(item.namespace.owner, item.name)"
            size="md"
          />
        </div>
        <div class="overflow-clip min-w-0">
          <p>
            <Link :to="'/' + item.namespace.owner + '/' + item.namespace.slug">{{ item.name }}</Link>
            by
            <Link :to="'/' + item.namespace.owner">{{ item.namespace.owner }}</Link>
          </p>
          <p>{{ item.description }}</p>
        </div>
        <div class="flex-grow"></div>
        <div class="<sm:hidden flex flex-col flex-shrink-0 min-w-40">
          <span class="inline-flex items-center">
            <IconMdiStar class="mx-1" /> {{ item.stats.stars }} {{ i18n.t("project.info.stars", item.stats.stars) }}
          </span>
          <span class="inline-flex items-center">
            <IconMdiDownload class="mx-1" /> {{ item.stats.downloads }} {{ i18n.t("project.info.totalDownloads", item.stats.downloads) }}
          </span>
          <Tooltip>
            <template #content>
              {{ i18n.t("project.info.lastUpdatedTooltip", [i18n.d(item.lastUpdated, "datetime")]) }}
            </template>
            <span class="inline-flex items-center"><IconMdiCalendar class="mx-1" />{{ lastUpdated(item.lastUpdated) }}</span>
          </Tooltip>
        </div>
      </Card>
    </template>
  </Pagination>
  <div v-if="projects?.result.length === 0">{{ i18n.t("hangar.projectSearch.noProjects") }}</div>
</template>
