<script lang="ts" setup>
import Link from "~/components/design/Link.vue";
import { useI18n } from "vue-i18n";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission, Platform } from "~/types/enums";
import Card from "~/components/design/Card.vue";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import Tag from "~/components/Tag.vue";
import { PaginatedResult, Tag as ApiTag, Version } from "hangar-api";
import { computed, reactive, watch } from "vue";
import { useBackendDataStore } from "~/store/backendData";
import { useProjectChannels, useProjectVersions } from "~/composables/useApiHelper";
import { useContext } from "vite-ssr/vue";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useRoute } from "vue-router";
import { useApi } from "~/composables/useApi";
import { HangarProject } from "hangar-internal";
import filesize from "filesize";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";

const i18n = useI18n();
const ctx = useContext();
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
const options = reactive({ page: 1, itemsPerPage: 10 });
const platforms = computed(() => [...(useBackendDataStore().platforms?.values() || [])]);
const requestOptions = computed(() => {
  return {
    limit: options.itemsPerPage,
    offset: (options.page - 1) * options.itemsPerPage,
    channel: filter.channels,
    platform: filter.platforms,
  };
});

const channels = await useProjectChannels(route.params.user as string, route.params.project as string).catch((e) => handleRequestError(e, ctx, i18n));
const versions = await useProjectVersions(route.params.user as string, route.params.project as string).catch((e) => handleRequestError(e, ctx, i18n));

if (channels) {
  filter.channels.push(...(channels.value?.map((c) => c.name) || []));
  filter.platforms.push(...platforms.value.map((p) => p.enumName));
}

useHead(
  useSeo("Versions | " + props.project.name, props.project.description, route, projectIconUrl(props.project.namespace.owner, props.project.namespace.slug))
);

watch(
  filter,
  async () => {
    if (import.meta.env.SSR) return;
    if (!versions) return;
    if (filter.channels.length === 0 || filter.platforms.length === 0) {
      versions.value.pagination.count = 0;
      versions.value.result = [];
      return;
    }
    const newVersions = await useApi<PaginatedResult<Version>>(
      `projects/${route.params.user}/${route.params.project}/versions`,
      false,
      "get",
      requestOptions.value
    ).catch((e) => handleRequestError(e, ctx, i18n));
    if (newVersions) {
      versions.value = newVersions;
    }
  },
  { deep: true }
);

function checkAllChannels() {
  if (!channels) return;
  filter.channels = filter.allChecked.channels ? channels.value?.map((c) => c.name) || [] : [];
}

function checkAllPlatforms() {
  filter.platforms = filter.allChecked.platforms ? platforms.value.map((c) => c.enumName) : [];
}

function updateChannelCheckAll() {
  if (!channels) return;
  filter.allChecked.channels = filter.channels.length === (channels.value?.length || 0);
}

function updatePlatformCheckAll() {
  filter.allChecked.platforms = filter.platforms.length === platforms.value.length;
}

function getChannelTag(version: Version): ApiTag {
  const channelTag = version.tags.find((t) => t.name === "Channel");
  if (typeof channelTag === "undefined") {
    throw new TypeError("Version missing a channel tag");
  }
  return channelTag;
}

function getNonChannelTags(version: Version): ApiTag[] {
  return version.tags.filter((t) => t.name !== "Channel");
}
</script>

<template>
  <div class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-9/12 flex-grow">
      <!-- todo pagination -->
      <ul>
        <li v-for="version in versions.result" :key="version.name" class="rounded bg-gray-200 p-2 mb-4">
          <router-link :to="`/${project.namespace.owner}/${project.namespace.slug}/versions/${version.name}`">
            <div class="flex flex-wrap">
              <div class="basis-4/12 md:basis-3/12 lg:basis-2/12">
                <div class="flex flex-wrap">
                  <span class="basis-full">{{ version.name }}</span>
                  <span class="basis-full"><Tag :tag="getChannelTag(version)" /></span>
                </div>
              </div>
              <div class="basis-4/12 md:basis-3/12 lg:basis-4/12">
                <Tag v-for="(tag, index) in getNonChannelTags(version)" :key="index" :tag="tag" />
              </div>
              <div class="basis-2/12 md:basis-4/12 lg:basis-3/12">
                <div class="flex flex-wrap">
                  <span class="basis-full">
                    <IconMdiCalendar />
                    {{ i18n.d(version.createdAt, "date") }}
                  </span>
                  <span class="basis-full">
                    <IconMdiFile />
                    <template v-if="version.fileInfo.sizeBytes">
                      {{ filesize(version.fileInfo.sizeBytes) }}
                    </template>
                    <template v-else> (external) </template>
                  </span>
                </div>
              </div>
              <div class="basis-2/12 md:basis-2/12 lg:basis-3/12">
                <div class="flex flex-wrap">
                  <span class="basis-full">
                    <IconMdiAccountArrowRight />
                    <Link :to="'/' + version.author">{{ version.author }}</Link>
                  </span>
                  <span class="basis-full">
                    <IconMdiDownload />
                    {{ version.stats.downloads }}
                  </span>
                </div>
              </div>
            </div>
          </router-link>
        </li>
      </ul>
    </section>

    <section class="basis-full md:basis-3/12 flex-grow">
      <div class="flex flex-wrap space-y-4">
        <div v-if="hasPerms(NamedPermission.CREATE_VERSION)" class="basis-full flex-grow">
          <Link to="versions/new">{{ i18n.t("version.new.uploadNew") }}</Link>
        </div>

        <Card class="basis-6/12 md:basis-full flex-grow">
          <template #header>
            {{ i18n.t("version.channels") }}
            <!-- todo channel editing -->
            <InputCheckbox v-model="filter.allChecked.channels" class="flex-right" @change="checkAllChannels" />
          </template>

          <ul>
            <li v-for="channel in channels" :key="channel.name">
              <InputCheckbox v-model="filter.channels" :value="channel.name" @change="updateChannelCheckAll" />
              <Tag :name="channel.name" :color="{ background: channel.color }"></Tag>
            </li>
          </ul>
        </Card>

        <Card class="basis-6/12 md:basis-full flex-grow">
          <template #header>
            {{ i18n.t("version.platforms") }}
            <InputCheckbox v-model="filter.allChecked.platforms" class="flex-right" @change="checkAllPlatforms" />
          </template>

          <ul>
            <li v-for="platform in platforms" :key="platform.name">
              <InputCheckbox v-model="filter.platforms" :value="platform.enumName" @change="updatePlatformCheckAll" />
              <Tag :name="platform.name" :color="platform.tagColor" />
            </li>
          </ul>
        </Card>
      </div>
    </section>
  </div>
</template>
