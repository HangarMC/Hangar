<script lang="ts" setup>
import Link from "~/components/design/Link.vue";
import { useI18n } from "vue-i18n";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission, Platform } from "~/types/enums";
import Card from "~/components/design/Card.vue";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import Tag from "~/components/Tag.vue";
import Button from "~/components/design/Button.vue";
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
import Alert from "~/components/design/Alert.vue";
import Pagination from "~/components/Pagination.vue";

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
      <ul>
        <Alert v-if="!versions.result || versions.result.length === 0" type="info"> {{ i18n.t("version.page.noVersions") }} </Alert>
        <Pagination v-else :items="versions.result">
          <template #default="{ item }">
            <li class="mb-4">
              <Card>
                <router-link :to="`/${project.namespace.owner}/${project.namespace.slug}/versions/${item.name}`">
                  <div class="flex flex-wrap">
                    <div class="basis-3/12 <sm:basis-4/12">
                      <div class="flex flex-wrap">
                        <span class="basis-full text-xl">{{ item.name }}</span>
                        <span class="basis-full"><Tag :tag="getChannelTag(item)" /></span>
                      </div>
                    </div>
                    <div class="basis-4/12 <sm:1/12">
                      <Tag v-for="(tag, index) in getNonChannelTags(item)" :key="index" :tag="tag" class="w-full" />
                    </div>
                    <div class="basis-2/12 <sm:hidden">
                      <div class="flex flex-wrap">
                        <span class="basis-full inline-flex items-center">
                          <IconMdiAccountArrowRight class="mr-1" />
                          {{ item.author }}
                        </span>
                        <span class="basis-full inline-flex items-center">
                          <IconMdiFile class="mr-1" />
                          <template v-if="item.fileInfo.sizeBytes">
                            {{ filesize(item.fileInfo.sizeBytes) }}
                          </template>
                          <template v-else> (external) </template>
                        </span>
                      </div>
                    </div>
                    <div class="basis-3/12 <sm:basis-4/12">
                      <div class="flex flex-wrap">
                        <span class="basis-full inline-flex items-center">
                          <IconMdiCalendar class="mr-1" />
                          {{ i18n.d(item.createdAt, "date") }}
                        </span>
                        <span class="basis-full inline-flex items-center">
                          <IconMdiDownload class="mr-1" />
                          {{ item.stats.downloads }}
                        </span>
                      </div>
                    </div>
                  </div>
                </router-link>
              </Card>
            </li>
          </template>
        </Pagination>
      </ul>
    </section>

    <section class="basis-full md:basis-3/12 flex-grow">
      <div class="flex flex-col flex-wrap space-y-4">
        <div v-if="hasPerms(NamedPermission.CREATE_VERSION)" class="basis-full flex-grow">
          <router-link :to="route.path + '/new'">
            <Button size="large" class="w-full">{{ i18n.t("version.new.uploadNew") }}</Button>
          </router-link>
        </div>

        <Card class="basis-6/12 md:basis-full flex-grow">
          <template #header>
            <div class="inline-flex w-full flex-cols space-between">
              <InputCheckbox v-model="filter.allChecked.channels" @change="checkAllChannels" />
              <span class="flex-grow">{{ i18n.t("version.channels") }}</span>
              <Link v-if="hasPerms(NamedPermission.EDIT_TAGS)" :to="`/${project.owner.name}/${project.name}/channels`">
                <Button size="small" class="ml-2 text-sm"> <IconMdiPencil /> </Button
              ></Link>
            </div>
          </template>

          <ul>
            <li v-for="channel in channels" :key="channel.name" class="inline-flex w-full">
              <InputCheckbox v-model="filter.channels" :value="channel.name" @change="updateChannelCheckAll" />
              <Tag :name="channel.name" :color="{ background: channel.color }"></Tag>
            </li>
          </ul>
        </Card>

        <Card class="basis-6/12 md:basis-full flex-grow">
          <template #header>
            <div class="inline-flex">
              <InputCheckbox v-model="filter.allChecked.platforms" class="flex-right" @change="checkAllPlatforms" />
              {{ i18n.t("version.platforms") }}
            </div>
          </template>

          <ul>
            <li v-for="platform in platforms" :key="platform.name" class="inline-flex w-full">
              <InputCheckbox v-model="filter.platforms" :value="platform.enumName" @change="updatePlatformCheckAll" />
              <Tag :name="platform.name" :color="platform.tagColor" />
            </li>
          </ul>
        </Card>
      </div>
    </section>
  </div>
</template>
