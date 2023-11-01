<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { Project } from "hangar-api";
import { useRouter } from "vue-router";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { lastUpdated } from "~/composables/useTime";
import Tooltip from "~/components/design/Tooltip.vue";
import { Visibility, Tag } from "~/types/enums";
import CategoryLogo from "~/components/logos/categories/CategoryLogo.vue";
import PrettyTime from "~/components/design/PrettyTime.vue";
import IconMdiPuzzleOutline from "~icons/mdi/puzzle-outline";
import IconMdiBookshelf from "~icons/mdi/bookshelf";
import IconMdiLeaf from "~icons/mdi/leaf";
import IconMdiStar from "~icons/mdi/star";
import IconMdiDownload from "~icons/mdi/download";
import IconMdiCalendar from "~icons/mdi/calendar";
import IconMdiCancel from "~icons/mdi/cancel";
import IconMdiEyeOff from "~icons/mdi/eye-off";

const i18n = useI18n();
const router = useRouter();

const props = defineProps<{
  project: Project;
}>();

function open(event: PointerEvent) {
  const url = "/" + props.project.namespace.owner + "/" + props.project.namespace.slug;
  if (event.ctrlKey) {
    window?.open(location.origin + url, "_blank")?.focus();
  } else {
    router.push(url);
  }
}
</script>

<template>
  <Card
    :class="{
      '!border-red-500 border-1px': project.visibility === Visibility.SOFT_DELETE,
      '!border-gray-300 !dark:border-gray-700 border-1px': project.visibility === Visibility.PUBLIC,
      'hover:background-card cursor-pointer': true,
    }"
    @click.prevent="open($event)"
  >
    <div class="flex space-x-4">
      <div>
        <UserAvatar :username="project.namespace.owner" :to="'/' + project.namespace.owner + '/' + project.name" :img-src="project.avatarUrl" size="md" />
      </div>
      <div class="overflow-clip overflow-hidden min-w-0">
        <div class="inline-flex items-center gap-x-1">
          <h2>
            <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
              <span class="text-xl font-bold">{{ project.name }}</span>
            </NuxtLink>
            <span class="text-sm">
              by
              <Link :to="'/' + project.namespace.owner" @click.stop="router.push('/' + project.namespace.owner)">{{ project.namespace.owner }}</Link>
            </span>
          </h2>
          <IconMdiCancel v-show="project.visibility === Visibility.SOFT_DELETE" />
          <IconMdiEyeOff v-show="project.visibility !== Visibility.PUBLIC" />
        </div>

        <h3 v-if="project.description" class="mb-1">{{ project.description }}</h3>
        <div class="inline-flex items-center text-gray-500 dark:text-gray-400 lt-sm:hidden">
          <CategoryLogo :category="project.category" :size="16" class="mr-1" />
          {{ i18n.t("project.category." + project.category) }}
          <div v-if="project.settings" class="inline-flex ml-2 space-x-1">
            <span class="border-l-1 border-gray-500 dark:border-gray-400" />
            <span v-for="tag in project.settings.tags" :key="tag" class="inline-flex items-center">
              <Tooltip>
                <template #content>
                  {{ i18n.t("project.settings.tags." + tag + ".tooltip") }}
                </template>
                <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
                <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
                <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" />
              </Tooltip>
            </span>
          </div>
        </div>
      </div>
      <div class="flex-grow"></div>
      <div class="lt-sm:hidden flex flex-col flex-shrink-0 min-w-40">
        <span class="inline-flex items-center">
          <IconMdiStar class="mx-1" /> {{ project.stats.stars.toLocaleString("en-US") }} {{ i18n.t("project.info.stars", project.stats.stars) }}
        </span>
        <span class="inline-flex items-center">
          <IconMdiDownload class="mx-1" />
          {{ project.stats.downloads.toLocaleString("en-US") }} {{ i18n.t("project.info.totalDownloads", project.stats.downloads) }}
        </span>
        <Tooltip>
          <template #content> {{ i18n.t("project.info.lastUpdatedTooltip") }}<PrettyTime :time="project.lastUpdated" long /> </template>
          <span class="inline-flex items-center"><IconMdiCalendar class="mx-1" /><PrettyTime :time="project.lastUpdated" short-relative /></span>
        </Tooltip>
      </div>
    </div>
    <div class="sm:hidden space-x-1 text-center mt-2">
      <span class="inline-flex items-center"><IconMdiCalendar class="mx-1" />{{ lastUpdated(project.lastUpdated) }}</span>
      <span class="inline-flex items-center"><IconMdiStar class="mx-1" /> {{ project.stats.stars }}</span>
      <span class="inline-flex items-center"><IconMdiDownload class="mx-1" />{{ project.stats.downloads }}</span>
    </div>
  </Card>
</template>
