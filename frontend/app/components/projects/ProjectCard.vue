<script setup lang="ts">
import { Tag, Visibility } from "#shared/types/backend";
import type { Platform, Project, ProjectCompact } from "#shared/types/backend";

const i18n = useI18n();
const router = useRouter();

const props = defineProps<{
  project: Project | ProjectCompact;
  canEdit?: boolean;
  pinned?: boolean;
}>();

const supportedPlatforms = computed<[Platform, string[]][]>(() =>
  "supportedPlatforms" in props.project ? (Object.entries(props.project.supportedPlatforms || {}) as [Platform, string[]][]) : []
);
const tags = computed<Tag[]>(() => ("settings" in props.project ? props.project.settings?.tags || [] : []));

async function togglePin() {
  await useInternalApi(`projects/project/${props.project.namespace.slug}/pin/${!props.pinned}`, "POST").catch(handleRequestError);
  router.go(0); // I am lazy
}
</script>

<template>
  <Card
    :class="{
      '!border-red-500 border-1px': project.visibility === Visibility.SoftDelete,
      '!border-gray-300 !dark:border-gray-700 border-1px': project.visibility === Visibility.Public,
      'relative group hover:background-card !overflow-hidden': true,
    }"
  >
    <div class="flex items-stretch gap-3">
      <!-- Fixed square, vertically centred: sized to the two-line content height, and any leftover
           space is split evenly instead of all landing under the avatar. -->
      <div class="h-88px w-88px flex-shrink-0 self-center">
        <UserAvatar :username="project.namespace.owner" :monogram-name="project.name" :img-src="project.avatarUrl" size="fill" disable-link />
      </div>

      <div class="flex min-w-0 flex-1 flex-col">
        <div class="flex min-w-0 items-center gap-x-1.5">
          <h3 class="min-w-0 truncate text-xl font-bold leading-tight">
            <!-- Stretched so the whole card is clickable without nesting the author link inside an anchor. -->
            <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug" class="after:(absolute inset-0 content-empty)">
              {{ project.name }}
            </NuxtLink>
          </h3>
          <IconMdiCancel v-if="project.visibility === Visibility.SoftDelete" class="flex-shrink-0 text-gray-secondary" />
          <IconMdiEyeOff v-else-if="project.visibility !== Visibility.Public" class="flex-shrink-0 text-gray-secondary" />
          <span class="flex-shrink-0 text-sm text-gray-secondary">{{ i18n.t("general.by") }}</span>
          <NuxtLink :to="'/' + project.namespace.owner" class="relative z-1 min-w-0 truncate text-sm font-bold color-primary hover:underline">
            {{ project.namespace.owner }}
          </NuxtLink>
          <button
            v-if="canEdit"
            class="relative z-1 ml-auto flex-shrink-0"
            :title="'Toggle pinned status for project ' + project.namespace.slug"
            @click.prevent="togglePin"
          >
            <IconMdiPinOff v-if="pinned" class="hidden group-hover:block" />
            <IconMdiPin v-else class="hidden group-hover:block" />
          </button>
        </div>

        <!-- Always two lines tall so every card in the list is the same height. -->
        <p class="mt-1 line-clamp-2 min-h-2.75rem text-base leading-snug">{{ project.description }}</p>

        <div class="mt-auto flex flex-wrap items-center gap-x-2.5 gap-y-1 pt-1 text-sm text-gray-secondary">
          <span v-for="[platform, versions] in supportedPlatforms" :key="platform" class="inline-flex items-center gap-1">
            <PlatformLogo :platform="platform" :size="16" class="flex-shrink-0" />
            <span class="tabular-nums">{{ versionRange(versions) }}</span>
          </span>

          <span class="inline-flex items-center gap-1">
            <CategoryLogo :category="project.category" :size="16" class="flex-shrink-0" />
            {{ i18n.t("project.category." + project.category) }}
          </span>

          <span v-for="tag in tags" :key="tag" class="inline-flex items-center gap-1 rounded background-card px-1.5 py-0.5 text-xs font-semibold">
            <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
            <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
            <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" />
            {{ i18n.t("project.settings.tags." + tag + ".title") }}
          </span>
        </div>
      </div>

      <div class="ml-auto hidden flex-shrink-0 flex-col items-end gap-0.5 self-center sm:flex">
        <span class="inline-flex items-center gap-1.5 font-semibold tabular-nums">
          <IconMdiDownload class="h-4 w-4 flex-shrink-0" />
          {{ project.stats.downloads.toLocaleString("en-US") }}
        </span>
        <span class="inline-flex items-center gap-1.5 font-semibold tabular-nums">
          <IconMdiStar class="h-4 w-4 flex-shrink-0" />
          {{ project.stats.stars.toLocaleString("en-US") }}
        </span>
        <Tooltip class="relative z-1">
          <template #content> {{ i18n.t("project.info.lastUpdatedTooltip") }}<PrettyTime :time="project.lastUpdated" long /> </template>
          <span class="whitespace-nowrap text-xs text-gray-secondary"><PrettyTime :time="project.lastUpdated" short-relative /></span>
        </Tooltip>
      </div>
    </div>

    <div class="mt-2 flex items-center justify-center gap-4 text-sm sm:hidden">
      <span class="inline-flex items-center gap-1 font-semibold tabular-nums"><IconMdiDownload />{{ project.stats.downloads.toLocaleString("en-US") }}</span>
      <span class="inline-flex items-center gap-1 font-semibold tabular-nums"><IconMdiStar />{{ project.stats.stars.toLocaleString("en-US") }}</span>
      <span class="inline-flex items-center gap-1 text-gray-secondary"><IconMdiCalendar /><PrettyTime :time="project.lastUpdated" short-relative /></span>
    </div>
  </Card>
</template>
