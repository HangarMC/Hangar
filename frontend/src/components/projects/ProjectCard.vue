<script setup lang="ts">
import { type Project, Tag, Visibility } from "~/types/backend";

const i18n = useI18n();
const router = useRouter();

defineProps<{
  project: Project;
}>();
</script>

<template>
  <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
    <Card
      :class="{
        '!border-red-500 border-1px': project.visibility === Visibility.SoftDelete,
        '!border-gray-300 !dark:border-gray-700 border-1px': project.visibility === Visibility.Public,
        'hover:background-card': true,
      }"
    >
      <div class="flex space-x-4">
        <div>
          <UserAvatar :username="project.namespace.owner" :img-src="project.avatarUrl" size="md" disable-link />
        </div>
        <div class="overflow-clip overflow-hidden min-w-0">
          <div class="inline-flex items-center gap-x-1">
            <h2>
              <span class="text-xl font-bold">{{ project.name }}</span>
              <span class="text-sm">
                by
                <object type="html/sucks">
                  <Link :to="'/' + project.namespace.owner" @click.stop="router.push('/' + project.namespace.owner)">{{ project.namespace.owner }}</Link>
                </object>
              </span>
            </h2>
            <IconMdiCancel v-show="project.visibility === Visibility.SoftDelete" />
            <IconMdiEyeOff v-show="project.visibility !== Visibility.Public" />
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
  </NuxtLink>
</template>
