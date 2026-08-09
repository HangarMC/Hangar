<script lang="ts" setup>
import { cloneDeep } from "lodash-es";
import { useVuelidate } from "@vuelidate/core";
import type { Tab } from "#shared/types/components/design/Tabs";
import IconMdiTune from "~icons/mdi/tune";
import IconMdiLinkVariant from "~icons/mdi/link-variant";
import IconMdiImageMultiple from "~icons/mdi/image-multiple";
import IconMdiAccountGroup from "~icons/mdi/account-group";
import IconMdiTagMultiple from "~icons/mdi/tag-multiple";
import IconMdiShieldAlert from "~icons/mdi/shield-alert";
import InputText from "~/components/ui/InputText.vue";
import { NamedPermission, Tag, Visibility } from "#shared/types/backend";
import type { HangarProject, HangarUser, PaginatedResultUser, ProjectSettings, Category } from "#shared/types/backend";

definePageMeta({
  projectPermsRequired: ["EditSubjectSettings"],
});

const route = useRoute("user-project-settings-slug");
const router = useRouter();
const i18n = useI18n();
const v = useVuelidate();
const notificationStore = useNotificationStore();
const props = defineProps<{
  project?: HangarProject;
  user?: HangarUser;
}>();

const selectedTab = ref(route.params.slug?.[0] || "general");
const tabs = ref<Tab<string>[]>([
  { value: "general", header: i18n.t("project.settings.tabs.general"), icon: IconMdiTune },
  { value: "links", header: i18n.t("project.settings.tabs.links"), icon: IconMdiLinkVariant },
  { value: "members", header: i18n.t("project.settings.tabs.members"), icon: IconMdiAccountGroup },
]);

if (hasPerms(NamedPermission.EditChannels)) {
  tabs.value.push({ value: "channels", header: i18n.t("project.settings.tabs.channels"), icon: IconMdiTagMultiple });
}
if (hasPerms(NamedPermission.IsSubjectOwner) || hasPerms(NamedPermission.DeleteProject) || hasPerms(NamedPermission.HardDeleteProject)) {
  tabs.value.push({ value: "management", header: i18n.t("project.settings.tabs.management"), icon: IconMdiShieldAlert });
}

tabs.value.push({ value: "banners", header: i18n.t("project.settings.tabs.banners"), icon: IconMdiImageMultiple, separated: true });

const isFormTab = computed(() => selectedTab.value === "general" || selectedTab.value === "links");

const form = reactive({
  settings: undefined,
  description: undefined,
  category: undefined,
} as { settings?: ProjectSettings; description?: string; category?: Category });

watch(
  () => props.project,
  (val) => {
    form.settings = cloneDeep(val?.settings);
    form.description = val?.description;
    form.category = val?.category;

    if (form.settings && !form.settings?.license?.type) {
      form.settings.license.type = "Unspecified";
    }
    if (form.settings && !form.settings?.links) {
      form.settings.links = [];
    }
  },
  { immediate: true }
);

const imgSrc = ref(props.project?.avatarUrl);
const hasCustomIcon = computed(() => imgSrc.value?.includes("/project/"));

watch(
  () => props.project?.avatarUrl,
  (avatarUrl) => {
    if (avatarUrl) imgSrc.value = avatarUrl;
  },
  { immediate: true }
);

const newName = ref<string | null | undefined>("");
const newNameField = useTemplateRef("newNameField");
const loading = reactive({
  save: false,
  rename: false,
  transfer: false,
});

function toggleTag(tag: Tag) {
  const tags = form.settings?.tags;
  if (!tags) return;
  const index = tags.indexOf(tag);
  if (index === -1) {
    tags.push(tag);
  } else {
    tags.splice(index, 1);
  }
}

const isCustomLicense = computed(() => form.settings?.license?.type === "Other");
const isUnspecifiedLicense = computed(() => form.settings?.license?.type === "Unspecified");

watch(route, (val) => (selectedTab.value = val.params.slug?.[0] || "general"), { deep: true });
watch(selectedTab, (val) => router.replace("/" + route.params.user + "/" + route.params.project + "/settings/" + val));

const search = ref<string>("");
const result = ref<string[]>([]);
async function doSearch(val: unknown) {
  result.value = [];
  const users = await useApi<PaginatedResultUser>("users", "get", {
    query: val,
    limit: 25,
    offset: 0,
  });
  result.value = users.result.map((u) => u.name);
}

async function save() {
  if (!(await v.value.$validate())) return;
  loading.save = true;
  try {
    if (selectedTab.value === "links") {
      await useInternalApi(`projects/project/${route.params.project}/links`, "post", {
        links: form.settings?.links ?? [],
      });
    } else {
      if (form.settings && !isCustomLicense.value) {
        form.settings.license.name = undefined as unknown as string;
      }
      if (form.settings && isUnspecifiedLicense.value) {
        form.settings.license.url = undefined;
      }

      await useInternalApi(`projects/project/${route.params.project}/settings`, "post", {
        ...form,
      });
    }
    await router.go(0);
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.save = false;
}

async function transfer() {
  loading.transfer = true;
  try {
    await useInternalApi<string>(`projects/project/${route.params.project}/transfer`, "post", {
      content: search.value,
    });
    notificationStore.success(i18n.t("project.settings.success.transferRequest", [search.value]));
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.transfer = false;
}

async function rename() {
  loading.rename = true;
  try {
    const newSlug = await useInternalApi<string>(`projects/project/${route.params.project}/rename`, "post", {
      content: newName.value,
    });
    await notificationStore.success(i18n.t("project.settings.success.rename", [newName.value]));
    await router.push("/" + route.params.user + "/" + newSlug);
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.rename = false;
}

async function softDelete(comment: string) {
  try {
    await useInternalApi(`projects/project/${props.project?.id}/manage/delete`, "post", {
      content: comment,
    });
    await notificationStore.success(i18n.t("project.settings.success.softDelete"));
    if (hasPerms(NamedPermission.HardDeleteProject)) {
      router.go(0);
    } else {
      await router.push("/");
    }
  } catch (err: any) {
    handleRequestError(err);
  }
}

async function hardDelete(comment: string) {
  try {
    await useInternalApi(`projects/project/${props.project?.id}/manage/hardDelete`, "post", {
      content: comment,
    });
    await notificationStore.success(i18n.t("project.settings.success.hardDelete"));
    await router.push("/");
  } catch (err: any) {
    handleRequestError(err);
  }
}

const shieldIoStyle = ref("flat");
const mcBannersStyle = ref("DARK_GUNMETAL");
const bannerUrls = computed(() => ({
  author: `https://api.mcbanners.com/banner/author/hangar/${props.project?.namespace?.slug}/banner.png?background__template=${mcBannersStyle.value}`,
  resource: `https://api.mcbanners.com/banner/resource/hangar/${props.project?.namespace?.slug}/banner.png?background__template=${mcBannersStyle.value}`,
  downloads: `https://img.shields.io/hangar/dt/${props.project?.namespace?.slug}?link=https%3A%2F%2Fhangar.papermc.io%2F${props.project?.namespace?.owner}%2F${props.project?.namespace?.slug}&style=${shieldIoStyle.value}`,
  stars: `https://img.shields.io/hangar/stars/${props.project?.namespace?.slug}?link=https%3A%2F%2Fhangar.papermc.io%2F${props.project?.namespace?.owner}%2F${props.project?.namespace?.slug}&style=${shieldIoStyle.value}`,
  views: `https://img.shields.io/hangar/views/${props.project?.namespace?.slug}?link=https%3A%2F%2Fhangar.papermc.io%2F${props.project?.namespace?.owner}%2F${props.project?.namespace?.slug}&style=${shieldIoStyle.value}`,
}));

const mcBanners = computed(() => [
  { label: i18n.t("project.settings.banners.author"), url: bannerUrls.value.author },
  { label: i18n.t("project.settings.banners.resource"), url: bannerUrls.value.resource },
]);
const shieldBanners = computed(() => [
  { label: i18n.t("project.settings.banners.downloads"), url: bannerUrls.value.downloads },
  { label: i18n.t("project.settings.banners.stars"), url: bannerUrls.value.stars },
  { label: i18n.t("project.settings.banners.views"), url: bannerUrls.value.views },
]);

function copyToClipboard(event: any, url: string, type: string = "url") {
  const clipboardData = event.clipboardData || event.originalEvent?.clipboardData || navigator.clipboard;
  if (type === "markdown") {
    url = `[![${props.project?.name}](${url})](https://hangar.papermc.io/${props.project?.namespace.owner}/${props.project?.namespace.slug})`;
  }
  clipboardData.writeText(url);
  notificationStore.success(i18n.t("project.settings.banners.copied"));
}

useSeo(
  computed(() => ({
    title: i18n.t("project.settings.title") + " | " + props.project?.name,
    route,
    description: props.project?.description,
    image: props.project?.avatarUrl,
  }))
);
</script>

<template>
  <Card>
    <Tabs v-model="selectedTab" :tabs="tabs" highlight-selected divided>
      <template #general>
        <ProjectSettingsSection title="project.settings.description" description="project.settings.descriptionSub">
          <InputText
            v-model="form.description"
            counter
            :maxlength="useBackendData.validations?.project?.desc?.max || 120"
            :rules="[required(), maxLength()(useBackendData.validations?.project?.desc?.max || 120)]"
          />
        </ProjectSettingsSection>
        <ProjectSettingsSection title="project.settings.icon" description="project.settings.iconSub">
          <EditableAvatar
            :username="project?.namespace?.owner"
            :monogram-name="project?.name"
            :img-src="imgSrc"
            :action="`projects/project/${route.params.project}/saveIcon`"
            :reset-action="hasCustomIcon ? `projects/project/${route.params.project}/resetIcon` : undefined"
            field="projectIcon"
            :label="i18n.t('project.settings.changeIcon')"
            size="xl"
          />
        </ProjectSettingsSection>
        <ProjectSettingsSection title="project.settings.category" description="project.settings.categorySub">
          <InputDropdown v-model="form.category" :values="useCategoryOptions" :rules="[required()]" i18n-text-values />
        </ProjectSettingsSection>
        <ProjectSettingsSection title="project.settings.tags.title" description="project.settings.tagsSub">
          <div v-if="form.settings" class="flex flex-wrap gap-2">
            <Tooltip v-for="tag in Object.values(Tag)" :key="tag">
              <template #content>{{ i18n.t("project.settings.tags." + tag + ".description") }}</template>
              <button
                type="button"
                class="inline-flex items-center gap-2 rounded-md border px-3 py-1.5 text-sm font-semibold transition-colors"
                :class="
                  form.settings.tags.includes(tag)
                    ? 'border-primary-500 background-card color-primary'
                    : 'border-gray-300 hover:background-card dark:border-gray-700'
                "
                :aria-pressed="form.settings.tags.includes(tag)"
                @click="toggleTag(tag)"
              >
                <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
                <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
                <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" />
                {{ i18n.t("project.settings.tags." + tag + ".title") }}
              </button>
            </Tooltip>
          </div>
        </ProjectSettingsSection>
        <ProjectSettingsSection title="project.settings.keywords" description="project.settings.keywordsSub">
          <InputTag
            v-if="form.settings"
            v-model="form.settings.keywords"
            counter
            :maxlength="useBackendData.validations?.project?.keywords?.max || 5"
            :tag-maxlength="useBackendData.validations?.project?.keywordName?.max || 16"
            :label="i18n.t('project.new.step3.keywords')"
            :rules="[maxLength()(useBackendData.validations?.project?.keywords?.max || 5), noDuplicated()(() => form.settings?.keywords)]"
          />
        </ProjectSettingsSection>
        <ProjectSettingsSection title="project.settings.license" description="project.settings.licenseSub">
          <div class="flex flex-wrap items-end gap-2">
            <div class="flex-shrink-0">
              <InputDropdown
                v-if="form.settings"
                v-model="form.settings.license.type"
                :values="useLicenseOptions"
                :label="i18n.t('project.settings.licenseType')"
              />
            </div>
            <div v-if="isCustomLicense" class="min-w-60 flex-1">
              <InputText
                v-if="form.settings"
                v-model.trim="form.settings.license.name"
                :label="i18n.t('project.settings.licenseCustom')"
                :rules="[
                  requiredIf()(isCustomLicense),
                  maxLength()(useBackendData.validations.project.license.max!),
                  pattern()(useBackendData.validations.project.license.regex!),
                ]"
              />
            </div>
            <div v-if="!isUnspecifiedLicense" class="min-w-60 flex-1">
              <InputText v-if="form.settings" v-model.trim="form.settings.license.url" :label="i18n.t('project.settings.licenseUrl')" :rules="[validUrl()]" />
            </div>
          </div>
        </ProjectSettingsSection>
      </template>
      <template #links>
        <ProjectSettingsSection title="project.settings.links.title" description="project.settings.links.sub">
          <ProjectLinksForm v-if="form.settings" v-model="form.settings.links" />
        </ProjectSettingsSection>
      </template>
      <template #management>
        <ProjectSettingsSection v-if="hasPerms(NamedPermission.IsSubjectOwner)" title="project.settings.rename" description="project.settings.renameSub">
          <div class="flex items-center">
            <InputText ref="newNameField" v-model.trim="newName" :label="i18n.t('project.settings.newName')" :rules="[validProjectName()()]" />
            <Button :disabled="!newName || newNameField?.validation?.$invalid" :loading="loading.rename" class="ml-2" @click="rename">
              <IconMdiRenameBox />
              {{ i18n.t("project.settings.rename") }}
            </Button>
          </div>
        </ProjectSettingsSection>
        <ProjectSettingsSection v-if="hasPerms(NamedPermission.IsSubjectOwner)" title="project.settings.transfer" description="project.settings.transferSub">
          <div class="flex items-center">
            <InputAutocomplete id="membersearch" v-model="search" :values="result" :label="i18n.t('project.settings.transferTo')" @search="doSearch" />
            <Button :disabled="search.length === 0" :loading="loading.transfer" class="ml-2" @click="transfer">
              <IconMdiRenameBox />
              {{ i18n.t("project.settings.transfer") }}
            </Button>
          </div>
        </ProjectSettingsSection>
        <div
          v-if="(hasPerms(NamedPermission.DeleteProject) && project?.visibility !== Visibility.SoftDelete) || hasPerms(NamedPermission.HardDeleteProject)"
          class="mt-4 overflow-hidden rounded-md border border-red-500/50"
        >
          <h2 class="border-b border-red-500/50 bg-red-500/10 px-4 py-2 font-semibold text-red-700 dark:text-red-300">
            {{ i18n.t("project.settings.dangerZone") }}
          </h2>
          <div
            v-if="hasPerms(NamedPermission.DeleteProject) && project?.visibility !== Visibility.SoftDelete"
            class="flex flex-wrap items-center gap-3 px-4 py-3"
          >
            <div class="min-w-0 flex-1">
              <div class="font-semibold">{{ i18n.t("project.settings.delete") }}</div>
              <p class="text-sm text-gray-secondary">{{ i18n.t("project.settings.deleteSub") }}</p>
            </div>
            <TextAreaModal
              :title="i18n.t('project.settings.delete')"
              :label="i18n.t('general.comment')"
              :submit="softDelete"
              :submit-label="i18n.t('project.settings.delete')"
              submit-tone="danger"
              require-input
            >
              <template #activator="{ on }">
                <Button tone="danger" class="flex-shrink-0" v-on="on">{{ i18n.t("project.settings.delete") }}</Button>
              </template>
            </TextAreaModal>
          </div>
          <div v-if="hasPerms(NamedPermission.HardDeleteProject)" class="flex flex-wrap items-center gap-3 border-t border-red-500/50 px-4 py-3">
            <div class="min-w-0 flex-1">
              <div class="font-semibold">{{ i18n.t("project.settings.hardDelete") }}</div>
              <p class="text-sm text-gray-secondary">{{ i18n.t("project.settings.hardDeleteSub") }}</p>
            </div>
            <TextAreaModal
              :title="i18n.t('project.settings.hardDelete')"
              :label="i18n.t('general.comment')"
              :submit="hardDelete"
              :submit-label="i18n.t('project.settings.hardDelete')"
              submit-tone="danger"
              require-input
            >
              <template #activator="{ on }">
                <Button tone="danger" class="flex-shrink-0" v-on="on">{{ i18n.t("project.settings.hardDelete") }}</Button>
              </template>
            </TextAreaModal>
          </div>
        </div>
      </template>
      <!--<template #donation>
          <Alert type="info" class="my-4">Coming Soon!</Alert>
          <ProjectSettingsSection title="project.settings.donation.enable">
            <InputCheckbox v-model="form.settings.donation.enable" :label="i18n.t('project.settings.donation.enableSub')" disabled />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.donation.subject" description="project.settings.donation.subjectSub">
            <InputText
              v-model="form.settings.donation.subject"
              :label="i18n.t('project.settings.donation.subjectLabel')"
              :rules="[requiredIf()(form.settings.donation.enable)]"
              disabled
            />
          </ProjectSettingsSection>
        </template>-->
      <template #banners>
        <ProjectSettingsSection title="project.settings.banners.mcbanners" description="project.settings.banners.mcbannersSub">
          <InputDropdown
            v-model="mcBannersStyle"
            :values="[
              'BLUE_RADIAL',
              'BURNING_ORANGE',
              'MANGO',
              'MOONLIGHT_PURPLE',
              'ORANGE_RADIAL',
              'VELVET',
              'YELLOW',
              'MALACHITE_GREEN',
              'DARK_GUNMETAL',
              'PURPLE_TAUPE',
              'LIGHT_MODE',
            ]"
            :label="i18n.t('project.settings.banners.style')"
          />
          <div class="mt-3 flex flex-col gap-3">
            <div v-for="banner in mcBanners" :key="banner.label" class="rounded-md border border-gray-300 p-3 dark:border-gray-700">
              <div class="mb-2 font-semibold">{{ banner.label }}</div>
              <img :src="banner.url" alt="" class="max-w-full rounded" />
              <div class="mt-2 flex flex-wrap gap-2">
                <Button variant="outline" tone="neutral" size="sm" @click="copyToClipboard($event, banner.url, 'markdown')">
                  <IconMdiContentCopy />
                  {{ i18n.t("project.settings.banners.markdown") }}
                </Button>
                <Button variant="outline" tone="neutral" size="sm" @click="copyToClipboard($event, banner.url)">
                  <IconMdiContentCopy />
                  {{ i18n.t("project.settings.banners.url") }}
                </Button>
              </div>
            </div>
          </div>
        </ProjectSettingsSection>
        <ProjectSettingsSection title="project.settings.banners.shields" description="project.settings.banners.shieldsSub">
          <InputDropdown
            v-model="shieldIoStyle"
            :values="['flat', 'flat-square', 'plastic', 'for-the-badge', 'social']"
            :label="i18n.t('project.settings.banners.style')"
          />
          <div class="mt-3 flex flex-col gap-3">
            <div v-for="banner in shieldBanners" :key="banner.label" class="rounded-md border border-gray-300 p-3 dark:border-gray-700">
              <div class="mb-2 font-semibold">{{ banner.label }}</div>
              <img :src="banner.url" alt="" class="max-w-full rounded" />
              <div class="mt-2 flex flex-wrap gap-2">
                <Button variant="outline" tone="neutral" size="sm" @click="copyToClipboard($event, banner.url, 'markdown')">
                  <IconMdiContentCopy />
                  {{ i18n.t("project.settings.banners.markdown") }}
                </Button>
                <Button variant="outline" tone="neutral" size="sm" @click="copyToClipboard($event, banner.url)">
                  <IconMdiContentCopy />
                  {{ i18n.t("project.settings.banners.url") }}
                </Button>
              </div>
            </div>
          </div>
        </ProjectSettingsSection>
      </template>
      <template #members>
        <ProjectSettingsSection class="max-w-3xl">
          <MemberList
            bare
            title="project.settings.tabs.members"
            description="project.settings.membersSub"
            :members="project?.members || []"
            :author="project?.namespace?.owner"
            :slug="project?.name"
          />
        </ProjectSettingsSection>
      </template>
      <template #channels>
        <ProjectSettingsSection title="channel.manage.title" description="project.settings.channelsSub">
          <ChannelManager :project="project" />
        </ProjectSettingsSection>
      </template>
    </Tabs>

    <div v-if="isFormTab" class="mt-6 flex justify-end border-t border-gray-300 pt-4 dark:border-gray-700">
      <Button :disabled="v.$error" :loading="loading.save" @click="save">
        <IconMdiCheck />
        {{ i18n.t("project.settings.save") }}
      </Button>
    </div>
  </Card>
</template>
