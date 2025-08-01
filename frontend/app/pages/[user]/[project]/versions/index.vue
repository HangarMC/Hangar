<script lang="ts" setup>
import type { HangarProject, Platform, Version } from "#shared/types/backend";
import { ChannelFlag, NamedPermission, Visibility } from "#shared/types/backend";

const i18n = useI18n();
const router = useRouter();
const route = useRoute("user-project-versions");
const globalData = useGlobalData();

const toArray = <T,>(input: unknown): T => (Array.isArray(input) ? input : input ? [input] : []) as T;
const filter = reactive({
  channels: toArray<string[]>(route.query.channel),
  platforms: toArray<Platform[]>(route.query.platform),
  allChecked: {
    channels: true,
    platforms: true,
  },
});

const props = defineProps<{
  project?: HangarProject;
}>();

const pageChangeScrollAnchor = useTemplateRef("pageChangeScrollAnchor");
const page = ref(route.query.page ? Number(route.query.page) : 0);
const requestParams = computed(() => {
  const limit = 7;
  return {
    limit,
    offset: page.value * limit,
    channel: filter.channels,
    platform: filter.platforms,
  };
});

const { channels, channelPromise } = useProjectChannels(() => route.params.project);
if (!route.query.channel) {
  await channelPromise;
  filter.channels.push(...channels.value!.filter((c) => !c.flags.includes(ChannelFlag.HIDE_BY_DEFAULT)).map((c) => c.name));
}
if (!route.query.platform && globalData.value) {
  filter.platforms.push(...globalData.value.platforms.map((p) => p.enumName));
}
const { versions, versionsStatus } = useProjectVersions(
  () => ({
    project: route.params.project,
    data: { ...requestParams.value, includeHiddenChannels: filter.channels?.length > 0 },
  }),
  router
);

useSeo(
  computed(() => ({
    title: "Versions | " + props.project?.name,
    route,
    description: `Download ${versions.value?.pagination?.count} ${props.project?.name} versions. ${props.project?.stats?.downloads} total downloads. Last updated on ${lastUpdated(new Date(versions.value?.result?.[0]?.createdAt || 0), i18n)}`,
    image: props.project?.avatarUrl,
  }))
);

function checkAllChannels() {
  filter.channels = filter.allChecked.channels ? channels.value?.map((c) => c.name) || [] : [];
}

function checkAllPlatforms() {
  filter.platforms = filter.allChecked.platforms ? globalData.value!.platforms.map((c) => c.enumName) : [];
}

function updateChannelCheckAll() {
  filter.allChecked.channels = filter.channels.length === (channels.value?.length || 0);
}

function updatePlatformCheckAll() {
  filter.allChecked.platforms = filter.platforms.length === globalData.value!.platforms.length;
}

function getBorderClasses(version: Version): string {
  if (version.visibility === Visibility.SoftDelete) {
    return "!border-red-500 border-1px";
  }
  return version.visibility === Visibility.Public ? "!border-gray-300 !dark:border-gray-700 border-1px" : "";
}

function getVisibilityTitle(visibility: Visibility) {
  const value = useBackendData.visibilities.find((v) => v.name === visibility);
  return value ? i18n.t(value.title) : undefined;
}
</script>

<template>
  <div ref="pageChangeScrollAnchor" class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-11/15 flex-grow">
      <ul>
        <template v-if="versionsStatus === 'loading'">
          <Skeleton class="mb-2 h-[90px]" delay />
          <Skeleton class="mb-2 h-[90px]" delay />
        </template>
        <Alert v-else-if="!versions?.result?.length" type="info"> {{ i18n.t("version.page.noVersions") }} </Alert>
        <Pagination
          v-else
          :items="versions.result"
          :server-pagination="versions.pagination"
          :reset-anchor="pageChangeScrollAnchor"
          @update:page="(p) => (page = p)"
        >
          <template #default="{ item }">
            <li class="mb-2">
              <Card :class="getBorderClasses(item)" class="pb-1">
                <NuxtLink :to="`/${project?.namespace?.owner}/${project?.namespace?.slug}/versions/${item.name}`">
                  <div class="flex lt-lg:flex-wrap">
                    <div class="basis-full lg:(basis-6/15 pb-4) truncate">
                      <div class="flex flex-wrap items-center">
                        <h3 class="lg:basis-full lt-lg:mr-1 text-1.15rem leading-relaxed">{{ item.name }}</h3>
                        <span class="lg:hidden flex-grow" />
                        <Tag :name="item.channel.name" :color="{ background: item.channel.color }" :tooltip="item.channel.description" />
                        <IconMdiCancel v-if="item.visibility === Visibility.SoftDelete" class="ml-1" />
                        <span v-else-if="item.visibility !== Visibility.Public" class="ml-1 inline-flex items-center">
                          <span class="text-gray-600 dark:text-gray-300 text-sm">
                            {{ getVisibilityTitle(item.visibility) }}
                          </span>
                          <IconMdiEyeOff class="ml-1" />
                        </span>
                      </div>
                    </div>
                    <hr class="lg:hidden basis-full mt-1 border-gray-400 dark:border-gray-500" />
                    <div class="basis-6/15 lt-lg:(mt-2 basis-6/12)">
                      <div v-for="(v, p) in item?.platformDependenciesFormatted" :key="p" class="basis-full">
                        <div class="inline-flex items-center">
                          <PlatformLogo :platform="p as unknown as Platform" :size="22" class="mr-1 flex-shrink-0" />
                          <span class="mr-3 text-0.95rem">{{ v.join(", ") }}</span>
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
        <div v-if="hasPerms(NamedPermission.CreateVersion)" class="basis-full flex-grow">
          <NuxtLink :to="route.path + '/new'">
            <Button size="large" class="w-full">{{ i18n.t("version.new.uploadNew") }}</Button>
          </NuxtLink>
        </div>

        <Card class="basis-6/12 md:basis-full flex-grow">
          <template #header>
            <div class="inline-flex w-full flex-cols space-between">
              <InputCheckbox v-model="filter.allChecked.channels" @change="checkAllChannels" />
              <h2 class="flex-grow">{{ i18n.t("version.channels") }}</h2>
              <Link v-if="project && hasPerms(NamedPermission.EditChannels)" :to="`/${project.namespace.owner}/${project.name}/channels`">
                <Button size="small" class="ml-2 text-sm"><IconMdiPencil /></Button>
              </Link>
            </div>
          </template>

          <ul>
            <li v-for="channel in channels" :key="channel.name" class="inline-flex w-full">
              <InputCheckbox v-model="filter.channels" :value="channel.name" @change="updateChannelCheckAll">
                <Tag :name="channel.name" :color="{ background: channel.color }" :tooltip="channel.description" />
              </InputCheckbox>
            </li>
          </ul>
        </Card>

        <Card class="basis-6/12 md:basis-full flex-grow">
          <template #header>
            <div class="inline-flex">
              <InputCheckbox v-model="filter.allChecked.platforms" class="flex-right" @change="checkAllPlatforms" />
              <h2>{{ i18n.t("version.platforms") }}</h2>
            </div>
          </template>

          <ul>
            <li v-for="platform in globalData?.platforms" :key="platform.name" class="inline-flex w-full">
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
