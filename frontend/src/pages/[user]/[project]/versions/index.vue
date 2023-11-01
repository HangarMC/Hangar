<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import type { Version, PaginatedResult } from "hangar-api";
import type { Ref } from "vue";
import { computed, reactive, watch } from "vue";
import { useRoute } from "vue-router";
import type { HangarProject } from "hangar-internal";
import { useHead } from "@unhead/vue";
import Link from "~/components/design/Link.vue";
import { hasPerms } from "~/composables/usePerm";
import type { Platform } from "~/types/enums";
import { NamedPermission, Visibility } from "~/types/enums";
import Card from "~/components/design/Card.vue";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import Tag from "~/components/Tag.vue";
import Button from "~/components/design/Button.vue";
import { useBackendData } from "~/store/backendData";
import { useProjectChannels, useProjectVersions } from "~/composables/useApiHelper";
import { useSeo } from "~/composables/useSeo";
import Alert from "~/components/design/Alert.vue";
import Pagination from "~/components/design/Pagination.vue";
import PlatformLogo from "~/components/logos/platforms/PlatformLogo.vue";
import { ref } from "#imports";

const i18n = useI18n();
const route = useRoute();

const filter = reactive({
  channels: [] as string[],
  platforms: [] as Platform[],
  allChecked: {
    channels: true,
    platforms: true,
  },
});

const props = defineProps<{
  project: HangarProject;
}>();

const platforms = computed(() => [...(useBackendData.platforms?.values() || [])]);
const pagination = ref();
const page = ref(0);
const requestParams = computed(() => {
  const limit = 7;
  return {
    limit,
    offset: page.value * limit,
    channel: filter.channels,
    platform: filter.platforms,
  };
});

const results = await Promise.all([
  useProjectChannels(route.params.project as string),
  useProjectVersions(route.params.project as string, requestParams.value),
]);
const channels = results[0].data;
const versions = results[1];
filter.channels.push(...channels.value.map((c) => c.name));
filter.platforms.push(...platforms.value.map((p) => p.enumName));

useHead(useSeo("Versions | " + props.project.name, props.project.description, route, props.project.avatarUrl));

const pageChangeScrollAnchor: Ref<Element | null> = ref(null);

async function update(newPage: number) {
  page.value = newPage;
  versions.value = (await useProjectVersions(route.params.project as string, requestParams.value))?.value;
}

watch(
  filter,
  async () => {
    if (import.meta.env.SSR) return;
    if (!versions || !versions.value) return;
    if (filter.channels.length === 0 || filter.platforms.length === 0) {
      versions.value.pagination.count = 0;
      versions.value.result = [];
      return;
    }

    await update(0);
  },
  { deep: true }
);

function checkAllChannels() {
  filter.channels = filter.allChecked.channels ? channels.value.map((c) => c.name) : [];
}

function checkAllPlatforms() {
  filter.platforms = filter.allChecked.platforms ? platforms.value.map((c) => c.enumName) : [];
}

function updateChannelCheckAll() {
  filter.allChecked.channels = filter.channels.length === (channels.value.length || 0);
}

function updatePlatformCheckAll() {
  filter.allChecked.platforms = filter.platforms.length === platforms.value.length;
}

function getBorderClasses(version: Version): string {
  if (version.visibility === Visibility.SOFT_DELETE) {
    return "!border-red-500 border-1px";
  }
  return version.visibility === Visibility.PUBLIC ? "!border-gray-300 !dark:border-gray-700 border-1px" : "";
}

function getVisibilityTitle(visibility: Visibility) {
  const value = useBackendData.visibilities.find((v) => v.name === visibility);
  return value ? i18n.t(value.title) : null;
}
</script>

<template>
  <div ref="pageChangeScrollAnchor" class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-11/15 flex-grow">
      <ul>
        <Alert v-if="!versions || !versions.result || versions.result.length === 0" type="info"> {{ i18n.t("version.page.noVersions") }} </Alert>
        <Pagination
          v-else-if="versions"
          ref="pagination"
          :items="versions.result"
          :server-pagination="versions.pagination"
          :reset-anchor="pageChangeScrollAnchor"
          @update:page="update"
        >
          <template #default="{ item }">
            <li class="mb-2">
              <Card :class="getBorderClasses(item)" class="pb-1">
                <NuxtLink :to="`/${project.namespace.owner}/${project.namespace.slug}/versions/${item.name}`">
                  <div class="flex lt-lg:flex-wrap">
                    <div class="basis-full lg:(basis-6/15 pb-4) truncate">
                      <div class="flex flex-wrap items-center">
                        <h2 class="lg:basis-full lt-lg:mr-1 text-1.15rem leading-relaxed">{{ item.name }}</h2>
                        <span class="lg:hidden flex-grow" />
                        <Tag :name="item.channel.name" :color="{ background: item.channel.color }" :tooltip="item.channel.description" />
                        <IconMdiCancel v-if="item.visibility === Visibility.SOFT_DELETE" class="ml-1"></IconMdiCancel>
                        <span v-else-if="item.visibility !== Visibility.PUBLIC" class="ml-1 inline-flex items-center">
                          <span class="text-gray-600 dark:text-gray-300 text-sm">
                            {{ getVisibilityTitle(item.visibility) }}
                          </span>
                          <IconMdiEyeOff class="ml-1" />
                        </span>
                      </div>
                    </div>
                    <hr class="lg:hidden basis-full mt-1 border-gray-400 dark:border-gray-500" />
                    <div class="basis-6/15 lt-lg:(mt-2 basis-6/12)">
                      <div v-for="(v, p) in item.platformDependenciesFormatted" :key="p" class="basis-full">
                        <div class="inline-flex items-center">
                          <PlatformLogo :platform="p" :size="22" class="mr-1 flex-shrink-0" />
                          <span class="mr-3 text-0.95rem">{{ v }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="basis-3/15 lt-lg:(mt-2 basis-6/12) text-0.95rem leading-normal">
                      <div class="flex flex-wrap">
                        <span class="basis-full inline-flex items-center">
                          <IconMdiCalendar class="mr-1" />
                          {{ i18n.d(item.createdAt, "date") }}
                        </span>
                        <span class="basis-full inline-flex items-center">
                          <IconMdiDownload class="mr-1" />
                          {{ item.stats.totalDownloads.toLocaleString("en-US") }}
                        </span>
                      </div>
                    </div>
                  </div>
                </NuxtLink>
              </Card>
            </li>
          </template>
        </Pagination>
      </ul>
    </section>

    <section class="basis-full md:basis-4/15 flex-grow">
      <div class="flex flex-col flex-wrap space-y-4">
        <div v-if="hasPerms(NamedPermission.CREATE_VERSION)" class="basis-full flex-grow">
          <NuxtLink :to="route.path + '/new'">
            <Button size="large" class="w-full">{{ i18n.t("version.new.uploadNew") }}</Button>
          </NuxtLink>
        </div>

        <Card class="basis-6/12 md:basis-full flex-grow">
          <template #header>
            <div class="inline-flex w-full flex-cols space-between">
              <InputCheckbox v-model="filter.allChecked.channels" @change="checkAllChannels" />
              <h3 class="flex-grow">{{ i18n.t("version.channels") }}</h3>
              <Link v-if="hasPerms(NamedPermission.EDIT_CHANNELS)" :to="`/${project.owner.name}/${project.name}/channels`">
                <Button size="small" class="ml-2 text-sm"><IconMdiPencil /></Button>
              </Link>
            </div>
          </template>

          <ul>
            <li v-for="channel in channels" :key="channel.name" class="inline-flex w-full">
              <InputCheckbox v-model="filter.channels" :value="channel.name" @change="updateChannelCheckAll">
                <Tag :name="channel.name" :color="{ background: channel.color }" :tooltip="channel.description"></Tag>
              </InputCheckbox>
            </li>
          </ul>
        </Card>

        <Card class="basis-6/12 md:basis-full flex-grow">
          <template #header>
            <div class="inline-flex">
              <InputCheckbox v-model="filter.allChecked.platforms" class="flex-right" @change="checkAllPlatforms" />
              <h3>{{ i18n.t("version.platforms") }}</h3>
            </div>
          </template>

          <ul>
            <li v-for="platform in platforms" :key="platform.name" class="inline-flex w-full">
              <InputCheckbox v-model="filter.platforms" :value="platform.enumName" :label="platform.name" @change="updatePlatformCheckAll">
                <PlatformLogo :platform="platform.enumName" :size="24" class="mr-1" />
              </InputCheckbox>
            </li>
          </ul>
        </Card>
      </div>
    </section>
  </div>
</template>
