<script setup lang="ts">
import { PaginatedResult, Project } from "hangar-api";
import { PropType } from "vue";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { useI18n } from "vue-i18n";
import Tooltip from "~/components/design/Tooltip.vue";

const i18n = useI18n();

const props = defineProps({
  projects: {
    type: Object as PropType<PaginatedResult<Project>>,
    required: true,
  },
});

function lastUpdated(date: Date): string {
  date = new Date(date);
  const today: Date = new Date();
  const todayTime = today.getTime();
  const dateTime = date.getTime();
  const todayDays = Math.floor(todayTime / (1000 * 60 * 60 * 24));
  const dateDays = Math.floor(dateTime / (1000 * 60 * 60 * 24));
  if (todayDays === dateDays) {
    return i18n.t("general.today") + " " + i18n.d(date, "clock");
  } else if (todayDays === dateDays + 1) {
    return i18n.t("general.yesterday") + " " + i18n.d(date, "clock");
  } else if (todayDays - dateDays < 7) {
    return i18n.d(date, "shortweektime");
  } else {
    return i18n.d(date, "date");
  }
}
</script>

<template>
  <Card v-for="project in projects?.result" :key="project.name" class="flex mb-1 space-x-4">
    <div>
      <UserAvatar
        :username="project.namespace.owner"
        :to="'/' + project.namespace.owner + '/' + project.name"
        :img-src="projectIconUrl(project.namespace.owner, project.name)"
        size="md"
      />
    </div>
    <div class="overflow-clip">
      <p>
        <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">{{ project.name }}</Link>
        by
        <Link :to="'/' + project.namespace.owner">{{ project.namespace.owner }}</Link>
      </p>
      <p>{{ project.description }}</p>
    </div>
    <div class="flex-grow"></div>
    <div class="<sm:hidden flex flex-col flex-shrink-0 min-w-40">
      <span class="inline-flex items-center"
        ><IconMdiStar class="mx-1" /> {{ project.stats.stars }} {{ i18n.t("project.info.stars", project.stats.stars) }}</span
      >
      <span class="inline-flex items-center"
        ><IconMdiDownload class="mx-1" /> {{ project.stats.downloads }} {{ i18n.t("project.info.totalDownloads", project.stats.downloads) }}</span
      >
      <Tooltip :content="i18n.t('project.info.lastUpdatedTooltip', [i18n.d(project.lastUpdated, 'datetime')])">
        <span class="inline-flex items-center"><IconMdiCalendar class="mx-1" />{{ lastUpdated(project.lastUpdated) }}</span>
      </Tooltip>
    </div>
  </Card>
  <div v-if="projects?.result.length === 0">{{ i18n.t("hangar.projectSearch.noProjects") }}</div>
</template>
