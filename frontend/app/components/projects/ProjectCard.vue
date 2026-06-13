<script setup lang="ts">
import { Tag, Visibility } from "#shared/types/backend";
import type { Project, ProjectCompact } from "#shared/types/backend";

const i18n = useI18n();
const router = useRouter();

const props = defineProps<{
  project: Project | ProjectCompact;
  canEdit?: boolean;
  pinned?: boolean;
}>();

const formatName = (name: String) => {
  return name.charAt(0).toUpperCase() + name.slice(1).toLowerCase();
};

const supportedPlatforms = computed(() => ("supportedPlatforms" in props.project ? Object.keys(props.project.supportedPlatforms) : []));

async function togglePin() {
  await useInternalApi(`/projects/project/${props.project.namespace.slug}/pin/${!props.pinned}`, "POST").catch(handleRequestError);
  router.go(0); // I am lazy
}
</script>

<template>
  <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug" class="block transition-transform duration-200">
    <Card
      class="relative"
      :class="{
        '!border-red-500 border-1px': project.visibility === Visibility.SoftDelete,
        '!border-gray-300 !dark:border-gray-800 border-1px': project.visibility === Visibility.Public,
        'hover:background-card group': true,
        'transition-all duration-200': true,
      }"
    >
      <div class="flex space-x-4">
        <div class="relative flex-shrink-0 overflow-hidden rounded-lg">
          <UserAvatar class="lt-xl:w-100px lt-xl:h-100px h-125px w-125px" :username="project.namespace.owner" :img-src="project.avatarUrl" disable-link />
          <button
            v-if="canEdit"
            class="absolute inset-0 flex cursor-pointer items-center justify-center bg-black/65 text-white opacity-0 backdrop-blur-[1px] transition-opacity duration-200 group-hover:opacity-100 focus-visible:opacity-100"
            :title="`${pinned ? 'Unpin' : 'Pin'} ${project.name}`"
            @click.prevent="togglePin"
          >
            <span class="inline-flex flex-col items-center gap-1 font-semibold">
              <IconMdiPinOff v-if="pinned" class="text-3xl" />
              <IconMdiPin v-else class="text-3xl" />
              {{ pinned ? "Unpin" : "Pin" }}
            </span>
          </button>
          <span v-else-if="pinned" class="absolute top-2 left-2 inline-flex rounded-md bg-black/65 p-1.5 color-primary" title="Pinned">
            <IconMdiPin />
          </span>
        </div>
        <div class="flex flex-col justify-between min-w-0 w-full">
          <div class="flex w-full">
            <div class="flex-1 w-75% overflow-x-hidden line-height-tight">
              <div class="inline-flex items-center gap-x-1.5">
                <h3>
                  <span class="text-xl font-bold truncate">{{ project.name }}&nbsp;</span>
                </h3>
                <IconMdiCancel v-if="project.visibility === Visibility.SoftDelete" />
                <IconMdiEyeOff v-if="project.visibility !== Visibility.Public" />
              </div>
              <span class="text-lg text-gray"> {{ i18n.t("general.by") }}&nbsp;</span>
              <span class="text-lg truncate">
                <object type="html/sucks">
                  <Link v-slot="{ classes }" custom>
                    <RouterLink :to="'/' + project.namespace.owner" :class="classes"> {{ project.namespace.owner }} </RouterLink>
                  </Link>
                </object>
              </span>
              <div v-if="'description' in project && project.description" class="mb-1 text-gray truncate">{{ project.description }}</div>
              <div v-else />
            </div>
            <div class="lt-sm:hidden flex-grow-0 flex-basis-auto flex flex-col items-end gap-1 pl-3 pb-2 border-b-2 border-charcoal-500">
              <span class="inline-flex items-center text-md">
                {{ project.stats.stars.toLocaleString("en-US") }} <IconMdiStar class="ml-1 text-primary-300" />
              </span>
              <span class="inline-flex items-center text-md"> {{ project.stats.downloads }} <IconMdiDownload class="ml-1 text-primary-300" /> </span>
              <Tooltip>
                <template #content> {{ i18n.t("project.info.lastUpdatedTooltip") }}<PrettyTime :time="project.lastUpdated" long /> </template>
                <span class="inline-flex items-center text-md"
                  ><PrettyTime :time="project.lastUpdated" short-relative /><IconMdiCalendar class="ml-1 text-primary-300"
                /></span>
              </Tooltip>
            </div>
          </div>
          <div class="lt-sm:hidden flex justify-between w-full">
            <div class="flex items-center">
              <CategoryLogo :category="project.category" :size="16" class="mr-1" />
              {{ i18n.t("project.category." + project.category) }}
              <div v-if="'settings' in project && project.settings" class="inline-flex ml-2 space-x-1">
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
            <div class="flex flex-wrap items-center justify-end gap-1">
              <span
                v-for="platform in supportedPlatforms"
                :key="platform"
                class="inline-flex items-center rounded-md border border-gray-200 bg-gray-100 px-2 py-1 text-xs dark:border-gray-700 dark:bg-charcoal-500"
              >
                <PlatformLogo :platform="platform" :size="15" class="mr-1.5" />
                {{ formatName(platform) }}
              </span>
            </div>
          </div>
        </div>
        <div class="flex-grow" />
      </div>
      <div class="xl:hidden flex items-center lt-sm:flex-col lt-sm:gap-2 justify-between w-full">
        <div class="xl:hiddenspace-x-1 mt-3 -mb-1">
          <span class="inline-flex items-center"><IconMdiCalendar class="mx-1 text-primary-300" />{{ lastUpdated(project.lastUpdated) }}</span>
          <span class="inline-flex items-center"><IconMdiStar class="mx-1 text-primary-300" /> {{ project.stats.stars }}</span>
          <span class="inline-flex items-center"><IconMdiDownload class="mx-1 text-primary-300" />{{ project.stats.downloads }}</span>
        </div>
      </div>
    </Card>
  </NuxtLink>
</template>
