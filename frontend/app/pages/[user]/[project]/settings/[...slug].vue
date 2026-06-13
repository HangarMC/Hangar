<script lang="ts" setup>
import { cloneDeep } from "lodash-es";
import { useVuelidate } from "@vuelidate/core";
import type { Tab } from "#shared/types/components/design/Tabs";
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
const tabs = ref([
  { value: "general", header: i18n.t("project.settings.tabs.general") },
  { value: "links", header: i18n.t("project.settings.tabs.links") },
  { value: "banners", header: i18n.t("project.settings.tabs.banners") },
  // { value: "donation", header: i18n.t("project.settings.tabs.donation") },
] satisfies Tab<string>[]);

if (hasPerms(NamedPermission.IsSubjectOwner) || hasPerms(NamedPermission.DeleteProject) || hasPerms(NamedPermission.HardDeleteProject)) {
  tabs.value.push({ value: "management", header: i18n.t("project.settings.tabs.management") });
}

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

const hasCustomIcon = computed(() => props.project?.avatarUrl?.includes("project"));

const newName = ref<string | null | undefined>("");
const newNameField = useTemplateRef("newNameField");
const loading = reactive({
  save: false,
  resetIcon: false,
  rename: false,
  transfer: false,
});

const isCustomLicense = computed(() => form.settings?.license?.type === "Other");
const isUnspecifiedLicense = computed(() => form.settings?.license?.type === "Unspecified");
const selectedCategory = computed(() => useCategoryOptions.value.find((option) => option.value === form.category));

function selectCategory(value: string) {
  form.category = value as Category;
}

function selectLicense(value: string) {
  if (form.settings) form.settings.license.type = value;
}

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
    if (form.settings && !isCustomLicense.value) {
      form.settings.license.name = undefined as unknown as string;
    }
    if (form.settings && isUnspecifiedLicense.value) {
      form.settings.license.url = undefined;
    }

    await useInternalApi(`projects/project/${route.params.project}/settings`, "post", {
      ...form,
    });
    notificationStore.success("Saved!");
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

async function resetIcon() {
  loading.resetIcon = true;
  try {
    const response = await useInternalApi<string | null>(`projects/project/${route.params.project}/resetIcon`, "post");
    await (response
      ? notificationStore.success(i18n.t("project.settings.success.resetIconWarn", [response]))
      : notificationStore.success(i18n.t("project.settings.success.resetIcon")));
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.resetIcon = false;
}

const shieldIoStyle = ref("flat");
const mcBannersStyle = ref("DARK_GUNMETAL");
const mcBannerErrors = reactive({
  author: false,
  resource: false,
});
const bannerUrls = computed(() => ({
  author: `https://api.mcbanners.com/banner/author/hangar/${props.project?.namespace?.owner}/banner.png?background__template=${mcBannersStyle.value}`,
  resource: `https://api.mcbanners.com/banner/resource/hangar/${props.project?.namespace?.slug}/banner.png?background__template=${mcBannersStyle.value}`,
  downloads: `https://img.shields.io/hangar/dt/${props.project?.namespace?.slug}?link=https%3A%2F%2Fhangar.papermc.io%2F${props.project?.namespace?.owner}%2F${props.project?.namespace?.slug}&style=${shieldIoStyle.value}`,
  stars: `https://img.shields.io/hangar/stars/${props.project?.namespace?.slug}?link=https%3A%2F%2Fhangar.papermc.io%2F${props.project?.namespace?.owner}%2F${props.project?.namespace?.slug}&style=${shieldIoStyle.value}`,
  views: `https://img.shields.io/hangar/views/${props.project?.namespace?.slug}?link=https%3A%2F%2Fhangar.papermc.io%2F${props.project?.namespace?.owner}%2F${props.project?.namespace?.slug}&style=${shieldIoStyle.value}`,
}));

watch(mcBannersStyle, () => {
  mcBannerErrors.author = false;
  mcBannerErrors.resource = false;
});

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
  <div>
    <section class="min-w-0">
      <!-- setting icons -->
      <Tabs v-model="selectedTab" :tabs="tabs" hide-navigation>
        <template #general>
          <div class="grid grid-cols-1 items-start gap-4 xl:grid-cols-2">
            <Card>
              <div>
                <h2 class="text-xl font-bold">{{ i18n.t("project.settings.category") }}</h2>
                <p class="mt-1 text-sm text-gray">{{ i18n.t("project.settings.categorySub") }}</p>
                <div class="mt-3">
                  <DropdownButton button-size="medium" button-type="transparent" button-class="!h-10.5 !py-2" match-width spread-arrow>
                    <template #button-label>
                      <span class="w-full truncate text-left">{{ selectedCategory ? i18n.t(selectedCategory.text) : form.category }}</span>
                    </template>
                    <template #default="{ close }">
                      <DropdownItem
                        v-for="category in useCategoryOptions"
                        :key="category.value"
                        :style="
                          form.category === category.value
                            ? {
                                backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                                borderColor: 'var(--primary-500)',
                              }
                            : {}
                        "
                        @click="
                          selectCategory(category.value);
                          close();
                        "
                      >
                        {{ i18n.t(category.text) }}
                      </DropdownItem>
                    </template>
                  </DropdownButton>
                </div>
              </div>

              <div class="mt-6">
                <div class="flex items-start justify-between gap-3">
                  <h2 class="text-xl font-bold">{{ i18n.t("project.settings.description") }}</h2>
                  <span class="text-xs text-gray"> {{ form.description?.length || 0 }}/{{ useBackendData.validations?.project?.desc?.max || 120 }} </span>
                </div>
                <p class="mt-1 text-sm text-gray">{{ i18n.t("project.settings.descriptionSub") }}</p>
                <div class="mt-3">
                  <InputText
                    v-model="form.description"
                    :maxlength="useBackendData.validations?.project?.desc?.max || 120"
                    :rules="[required(), maxLength()(useBackendData.validations?.project?.desc?.max || 120)]"
                  />
                </div>
              </div>

              <div class="mt-6">
                <div class="flex items-start justify-between gap-3">
                  <h2 class="text-xl font-bold">{{ i18n.t("project.settings.keywords") }}</h2>
                  <span class="text-xs text-gray">
                    {{ form.settings?.keywords.length || 0 }}/{{ useBackendData.validations?.project?.keywords?.max || 5 }}
                  </span>
                </div>
                <p class="mt-1 text-sm text-gray">{{ i18n.t("project.settings.keywordsSub") }}</p>
                <div class="mt-3">
                  <InputTag
                    v-if="form.settings"
                    v-model="form.settings.keywords"
                    :maxlength="useBackendData.validations?.project?.keywords?.max || 5"
                    :tag-maxlength="useBackendData.validations?.project?.keywordName?.max || 16"
                    :label="i18n.t('project.new.step3.keywords')"
                    :rules="[maxLength()(useBackendData.validations?.project?.keywords?.max || 5), noDuplicated()(() => form.settings?.keywords)]"
                  />
                </div>
              </div>

              <div class="mt-6">
                <h2 class="text-xl font-bold">{{ i18n.t("project.settings.tags.title") }}</h2>
                <p class="mt-1 text-sm text-gray">{{ i18n.t("project.settings.tagsSub") }}</p>
                <div v-if="form.settings" class="mt-3 grid gap-2 sm:grid-cols-3">
                  <button
                    v-for="tag in Object.values(Tag)"
                    :key="tag"
                    type="button"
                    class="flex h-11 items-center rounded-lg border px-3 text-sm transition-all duration-200 hover:border-gray-600 hover:bg-gray-800/60"
                    :class="form.settings.tags.includes(tag) ? 'color-primary' : 'border-gray-200 dark:border-gray-800'"
                    :style="
                      form.settings.tags.includes(tag)
                        ? {
                            backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                            borderColor: 'var(--primary-500)',
                          }
                        : {}
                    "
                    @click="
                      form.settings.tags = form.settings.tags.includes(tag)
                        ? form.settings.tags.filter((currentTag) => currentTag !== tag)
                        : [...form.settings.tags, tag]
                    "
                  >
                    <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
                    <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
                    <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" />
                    <span class="ml-1">{{ i18n.t("project.settings.tags." + tag + ".title") }}</span>
                    <Tooltip>
                      <template #content> {{ i18n.t("project.settings.tags." + tag + ".description") }} </template>
                      <IconMdiHelpCircleOutline class="ml-1 text-gray-500 dark:text-gray-400 text-sm" />
                    </Tooltip>
                    <IconMdiCheck v-if="form.settings.tags.includes(tag)" class="ml-auto" />
                  </button>
                </div>
              </div>

              <div class="mt-6 flex justify-end">
                <Button :disabled="v.$error" :loading="loading.save" @click="save">
                  <IconMdiCheck class="mr-1" />
                  {{ i18n.t("general.save") }}
                </Button>
              </div>
            </Card>

            <Card>
              <div>
                <h2 class="text-xl font-bold">{{ i18n.t("project.settings.license") }}</h2>
                <p class="mt-1 text-sm text-gray">{{ i18n.t("project.settings.licenseSub") }}</p>
                <div class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-[minmax(10rem,1fr)_minmax(0,2fr)]">
                  <div>
                    <DropdownButton v-if="form.settings" button-size="medium" button-type="transparent" button-class="!h-10.5 !py-2" match-width spread-arrow>
                      <template #button-label>
                        <span class="w-full truncate text-left">{{ form.settings.license.type }}</span>
                      </template>
                      <template #default="{ close }">
                        <DropdownItem
                          v-for="license in useLicenseOptions"
                          :key="license.value"
                          :style="
                            form.settings?.license.type === license.value
                              ? {
                                  backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                                  borderColor: 'var(--primary-500)',
                                }
                              : {}
                          "
                          @click="
                            selectLicense(license.value);
                            close();
                          "
                        >
                          {{ license.text }}
                        </DropdownItem>
                      </template>
                    </DropdownButton>
                  </div>
                  <div v-if="isCustomLicense">
                    <InputText
                      v-if="form.settings"
                      v-model.trim="form.settings.license.name"
                      :placeholder="i18n.t('project.settings.licenseCustom')"
                      :rules="[
                        requiredIf()(isCustomLicense),
                        maxLength()(useBackendData.validations.project.license.max!),
                        pattern()(useBackendData.validations.project.license.regex!),
                      ]"
                    />
                  </div>
                  <div v-if="!isUnspecifiedLicense" :class="{ 'sm:col-start-2': isCustomLicense }">
                    <InputText
                      v-if="form.settings"
                      v-model.trim="form.settings.license.url"
                      :placeholder="i18n.t('project.settings.licenseUrl')"
                      :rules="[validUrl()]"
                    />
                  </div>
                </div>
              </div>

              <div class="mt-6">
                <div class="flex flex-col items-start gap-4 sm:flex-row">
                  <img class="h-24 w-24 flex-shrink-0 self-start rounded-lg shadow-lg" :src="project?.avatarUrl" :alt="project?.name" width="96" height="96" />
                  <div class="min-w-0 flex-grow">
                    <h2 class="text-xl font-bold">{{ i18n.t("project.settings.icon") }}</h2>
                    <p class="mt-1 text-sm text-gray">{{ i18n.t("project.settings.iconSub") }}</p>
                    <div class="mt-3 flex items-center gap-2">
                      <AvatarChangeModal
                        :avatar="project?.avatarUrl || ''"
                        :action="`projects/project/${route.params.project}/saveIcon`"
                        :title="i18n.t('project.settings.icon')"
                        field-name="projectIcon"
                      >
                        <template #activator="{ on }">
                          <Button size="small" @click.prevent="on.click">{{ i18n.t("project.settings.iconUpload") }}</Button>
                        </template>
                      </AvatarChangeModal>
                      <button
                        class="inline-flex h-7.5 w-7.5 flex-shrink-0 items-center justify-center rounded-md border border-transparent transition-all duration-250 hover:border-red-600 hover:bg-red-900/50 disabled:cursor-not-allowed disabled:opacity-50"
                        :title="i18n.t('project.settings.iconReset')"
                        :disabled="!hasCustomIcon || loading.resetIcon"
                        @click.prevent="resetIcon"
                      >
                        <IconMdiBin />
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <div class="mt-6 flex justify-end">
                <Button :disabled="v.$error" :loading="loading.save" @click="save">
                  <IconMdiCheck class="mr-1" />
                  {{ i18n.t("general.save") }}
                </Button>
              </div>
            </Card>
          </div>
        </template>
        <template #links>
          <Card>
            <ProjectLinksForm v-if="form.settings" v-model="form.settings.links" />
            <div v-if="form.settings?.links.length" class="mt-4 flex justify-end">
              <Button :disabled="v.$error" :loading="loading.save" @click="save">
                <IconMdiCheck class="mr-1" />
                {{ i18n.t("general.save") }}
              </Button>
            </div>
          </Card>
        </template>
        <template #management>
          <div class="grid gap-3 sm:grid-cols-2 items-start">
            <Card>
              <div v-if="hasPerms(NamedPermission.IsSubjectOwner)" class="mb-4">
                <h3 class="text-lg font-semibold">Rename</h3>
                <p class="mt-1 text-sm text-gray">Changing your project's name can have undesired consequences. </p>
                <div class="mt-3 flex flex-col gap-2 sm:flex-row sm:items-stretch">
                  <InputText ref="newNameField" v-model.trim="newName" label="New Name" :rules="[validProjectName()()]" class="w-full [&>label]:h-10.5" />
                  <Button
                    size="small"
                    class="!h-10.5 h-full leading-none flex-shrink-0 !px-3 text-sm"
                    :disabled="!newName || newNameField?.validation?.$invalid"
                    :loading="loading.rename"
                    @click="rename"
                  >
                    <IconMdiRenameBox class="mr-1" />
                    Rename
                  </Button>
                </div>
              </div>

              <div v-if="hasPerms(NamedPermission.IsSubjectOwner)" class="mb-4">
                <h3 class="text-lg font-semibold">Transfer</h3>
                <p class="mt-1 text-sm text-gray">Transferring your project name can have undesired consequences.</p>
                <div class="mt-3 flex flex-col gap-2 sm:flex-row sm:items-stretch">
                  <InputAutocomplete id="membersearch" v-model="search" :values="result" label="User or organization" @search="doSearch" />
                  <Button
                    size="small"
                    class="!h-10.5 h-full leading-none flex-shrink-0 !px-3 text-sm"
                    :disabled="search.length === 0"
                    :loading="loading.transfer"
                    @click="transfer"
                  >
                    <IconMdiRenameBox class="mr-1" />
                    Transfer
                  </Button>
                </div>
              </div>

              <template v-if="hasPerms(NamedPermission.DeleteProject) && project?.visibility !== Visibility.SoftDelete">
                <h3 class="text-lg font-semibold">Delete</h3>
                <p class="mt-1 text-sm text-gray">Once you delete a project, it cannot be recovered.</p>
                <div class="mt-3">
                  <TextAreaModal
                    title="Delete project"
                    label="Reason for deletion"
                    description="This will delete the project and make it unavailable. "
                    confirmation-text="Confirm"
                    submit-label="Delete"
                    :submit="softDelete"
                    require-input
                    destructive
                  >
                    <template #activator="{ on }">
                      <button
                        type="button"
                        class="inline-flex h-10.5 items-center justify-center rounded-md border border-red-600 bg-red-900/50 px-3 text-sm font-semibold text-white transition-all duration-250"
                        v-on="on"
                      >
                        Delete
                      </button>
                    </template>
                  </TextAreaModal>
                </div>
              </template>

              <div v-if="hasPerms(NamedPermission.HardDeleteProject)" class="mb-0">
                <h3 class="text-lg font-semibold">Hard Delete</h3>
                <p class="mt-1 text-sm text-gray">This will permanently remove the project. This action is irreversible.</p>
                <div class="mt-3">
                  <TextAreaModal
                    title="Permanently delete project"
                    label="Reason for permanent deletion"
                    description="This permanently removes the project and all associated data. This action is irreversible."
                    confirmation-text="Confirm"
                    submit-label="Delete forever"
                    :submit="hardDelete"
                    require-input
                    destructive
                  >
                    <template #activator="{ on }">
                      <button
                        type="button"
                        class="inline-flex h-10.5 items-center justify-center rounded-md border border-red-600 bg-red-900/50 px-3 text-sm font-semibold text-white transition-all duration-250"
                        v-on="on"
                      >
                        Hard Delete
                      </button>
                    </template>
                  </TextAreaModal>
                </div>
              </div>
            </Card>

            <div>
              <MemberList :members="project?.members || []" :author="project?.namespace?.owner" :slug="project?.name" class="mb-4 h-max overflow-visible" />
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
          <div class="grid gap-3 sm:grid-cols-2 items-start">
            <ProjectSettingsSection title="project.settings.banners.mcbanners" description="project.settings.banners.mcbannersSub">
              <div class="mb-2">
                <DropdownButton button-size="medium" button-type="transparent" button-class="!h-10.5 !py-2" match-width spread-arrow>
                  <template #button-label>
                    <span class="w-full truncate text-left">{{ mcBannersStyle }}</span>
                  </template>
                  <template #default="{ close }">
                    <DropdownItem
                      v-for="style in [
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
                      :key="style"
                      @click="
                        mcBannersStyle = style;
                        close();
                      "
                    >
                      {{ style }}
                    </DropdownItem>
                  </template>
                </DropdownButton>
              </div>
              <div class="grid gap-3 sm:grid-cols-2">
                <div>
                  <div class="mb-2 text-sm font-semibold">{{ i18n.t("project.settings.banners.author") }}</div>
                  <div class="inline-block max-w-full overflow-hidden rounded-lg border border-gray-300 bg-gray-100 dark:border-gray-700 dark:bg-charcoal-600">
                    <img
                      v-if="!mcBannerErrors.author"
                      :src="bannerUrls.author"
                      alt=""
                      class="block w-auto h-auto max-w-full"
                      @error="mcBannerErrors.author = true"
                    />
                    <div v-else class="flex h-20 w-full items-center justify-center px-4 text-center text-sm text-gray">MCBanners image was not found.</div>
                  </div>
                  <div class="mt-3 flex gap-2">
                    <Button size="small" class="!h-9 text-sm" @click="copyToClipboard($event, bannerUrls.author, 'markdown')">
                      {{ i18n.t("project.settings.banners.markdown") }}
                    </Button>
                    <Button size="small" class="!h-9 text-sm" button-type="secondary" @click="copyToClipboard($event, bannerUrls.author)">
                      {{ i18n.t("project.settings.banners.url") }}
                    </Button>
                  </div>
                </div>
                <div>
                  <div class="mb-2 text-sm font-semibold">{{ i18n.t("project.settings.banners.resource") }}</div>
                  <div class="inline-block max-w-full overflow-hidden rounded-lg border border-gray-300 bg-gray-100 dark:border-gray-700 dark:bg-charcoal-600">
                    <img
                      v-if="!mcBannerErrors.resource"
                      :src="bannerUrls.resource"
                      alt=""
                      class="block w-auto h-auto max-w-full"
                      @error="mcBannerErrors.resource = true"
                    />
                    <div v-else class="flex h-20 w-full items-center justify-center px-4 text-center text-sm text-gray">MCBanners image was not found.</div>
                  </div>
                  <div class="mt-3 flex gap-2">
                    <Button size="small" class="!h-9 text-sm" @click="copyToClipboard($event, bannerUrls.resource, 'markdown')">
                      {{ i18n.t("project.settings.banners.markdown") }}
                    </Button>
                    <Button size="small" class="!h-9 text-sm" button-type="secondary" @click="copyToClipboard($event, bannerUrls.resource)">
                      {{ i18n.t("project.settings.banners.url") }}
                    </Button>
                  </div>
                </div>
              </div>
            </ProjectSettingsSection>
            <ProjectSettingsSection title="project.settings.banners.shields" description="project.settings.banners.shieldsSub">
              <div class="mb-2">
                <DropdownButton button-size="medium" button-type="transparent" button-class="!h-10.5 !py-2" match-width spread-arrow>
                  <template #button-label>
                    <span class="w-full truncate text-left">{{ shieldIoStyle }}</span>
                  </template>
                  <template #default="{ close }">
                    <DropdownItem
                      v-for="style in ['flat', 'flat-square', 'plastic', 'for-the-badge', 'social']"
                      :key="style"
                      @click="
                        shieldIoStyle = style;
                        close();
                      "
                    >
                      {{ style }}
                    </DropdownItem>
                  </template>
                </DropdownButton>
              </div>
              <div class="grid gap-3 sm:grid-cols-3 items-start">
                <div
                  v-for="badge in [
                    { label: i18n.t('project.settings.banners.downloads'), url: bannerUrls.downloads },
                    { label: i18n.t('project.settings.banners.stars'), url: bannerUrls.stars },
                    { label: i18n.t('project.settings.banners.views'), url: bannerUrls.views },
                  ]"
                  :key="badge.label"
                  class="flex flex-col items-start gap-1"
                >
                  <div class="text-sm font-semibold">{{ badge.label }}</div>
                  <img :src="badge.url" alt="" class="h-6 w-auto" />
                  <div class="flex gap-2 mt-1">
                    <Button size="small" class="!h-8 text-sm" @click="copyToClipboard($event, badge.url, 'markdown')">
                      {{ i18n.t("project.settings.banners.markdown") }}
                    </Button>
                    <Button size="small" class="!h-8 text-sm" button-type="secondary" @click="copyToClipboard($event, badge.url)">
                      {{ i18n.t("project.settings.banners.url") }}
                    </Button>
                  </div>
                </div>
              </div>
            </ProjectSettingsSection>
          </div>
        </template>
      </Tabs>
    </section>
  </div>
</template>
