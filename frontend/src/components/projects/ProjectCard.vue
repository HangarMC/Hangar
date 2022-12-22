<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { Project } from "hangar-api";
import Card from "~/lib/components/design/Card.vue";
import Link from "~/lib/components/design/Link.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { lastUpdated } from "~/lib/composables/useTime";
import Tooltip from "~/lib/components/design/Tooltip.vue";
import { Visibility } from "~/types/enums";
import CategoryLogo from "~/components/logos/categories/CategoryLogo.vue";
import ComingSoon from "~/lib/components/design/ComingSoon.vue";

const i18n = useI18n();

const props = defineProps<{
  project: Project;
}>();

function getBorderClasses(): string {
  if (props.project.visibility === Visibility.SOFT_DELETE) {
    return "!border-red-500 border-1px";
  }
  return props.project.visibility === Visibility.PUBLIC ? "!border-gray-300 !dark:border-gray-700 border-1px" : "";
}
</script>

<template>
  <Card :class="getBorderClasses()">
    <div class="flex space-x-4">
      <div>
        <UserAvatar
          :username="project.namespace.owner"
          :to="'/' + project.namespace.owner + '/' + project.name"
          :img-src="projectIconUrl(project.namespace.owner, project.name)"
          size="md"
        />
      </div>
      <div class="overflow-clip overflow-hidden min-w-0">
        <h2 class="inline-flex items-center gap-x-1">
          <span>
            <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">{{ project.name }}</Link>
            by
            <Link :to="'/' + project.namespace.owner">{{ project.namespace.owner }}</Link>
          </span>
          <IconMdiCancel v-if="project.visibility === Visibility.SOFT_DELETE"></IconMdiCancel>
          <IconMdiEyeOff v-else-if="project.visibility !== Visibility.PUBLIC"></IconMdiEyeOff>
        </h2>

        <h3 v-if="project.description" class="mb-1">{{ project.description }}</h3>
        <span class="<sm:hidden text-gray-500 dark:text-gray-400 flex flex-row items-center">
          <CategoryLogo :category="project.category" :size="16" class="mr-1" />
          {{ i18n.t("project.category." + project.category) }}
        </span>
      </div>
      <div class="flex-grow"></div>
      <div class="<sm:hidden flex flex-col flex-shrink-0 min-w-40">
        <span class="inline-flex items-center">
          <IconMdiStar class="mx-1" /> {{ project.stats.stars }} {{ i18n.t("project.info.stars", project.stats.stars) }}
        </span>
        <span class="inline-flex items-center">
          <IconMdiDownload class="mx-1" />
          <ComingSoon>{{ project.stats.downloads }} {{ i18n.t("project.info.totalDownloads", project.stats.downloads) }}</ComingSoon>
        </span>
        <Tooltip>
          <template #content>
            {{ i18n.t("project.info.lastUpdatedTooltip", [i18n.d(project.lastUpdated, "datetime")]) }}
          </template>
          <span class="inline-flex items-center"><IconMdiCalendar class="mx-1" />{{ lastUpdated(project.lastUpdated) }}</span>
        </Tooltip>
      </div>
    </div>
    <div class="sm:hidden space-x-1 text-center mt-2">
      <span class="inline-flex items-center"><IconMdiCalendar class="mx-1" />{{ lastUpdated(project.lastUpdated) }}</span>
      <span class="inline-flex items-center"> <IconMdiStar class="mx-1" /> {{ project.stats.stars }} </span>
      <span class="inline-flex items-center">
        <IconMdiDownload class="mx-1" /><ComingSoon short>{{ project.stats.downloads }}</ComingSoon>
      </span>
    </div>
  </Card>
</template>
