<script lang="ts" setup>
const { t } = useI18n();
const notification = useNotificationStore();

const props = withDefaults(defineProps<{ compact?: boolean }>(), {
  compact: false,
});

const socials = defineModel<Record<string, string>>({ required: true });

const linkType = ref<string>();
const addingLink = ref(false);
const editingType = ref<string>();
const linkTypes = [
  { value: "discord", text: "Discord" },
  { value: "github", text: "GitHub" },
  { value: "twitter", text: "Twitter" },
  { value: "youtube", text: "YouTube" },
  { value: "website", text: "Website" },
];

const availableLinkTypes = computed(() => linkTypes.filter((type) => !Object.hasOwn(socials.value, type.value)));

function addLink() {
  if (!linkType.value) {
    return notification.error(t("auth.settings.profile.socialLinks.selectType"));
  }
  if (Object.hasOwn(socials.value, linkType.value)) {
    return notification.error(t("auth.settings.profile.socialLinks.alreadyAdded"));
  }
  socials.value[linkType.value] = "";
  editingType.value = linkType.value;
  linkType.value = undefined;
  addingLink.value = false;
}

function removeLink(type: string) {
  delete socials.value[type];
  if (editingType.value === type) {
    editingType.value = undefined;
  }
}

function toggleEditing(type: string) {
  editingType.value = editingType.value === type ? undefined : type;
}

function linkName(type: string) {
  return linkTypes.find((linkType) => linkType.value === type)?.text ?? type;
}
</script>

<template>
  <section :class="{ 'mt-6': !props.compact }">
    <div class="flex items-start gap-3">
      <div>
        <h3 class="text-lg font-bold">{{ t("auth.settings.profile.socialLinks.title") }}</h3>
        <p class="mt-1 text-sm text-gray-secondary">{{ t("auth.settings.profile.socialLinks.description") }}</p>
      </div>
      <Button variant="outline" tone="neutral" class="ml-auto shrink-0" :disabled="availableLinkTypes.length === 0" @click.prevent="addingLink = !addingLink">
        <IconMdiPlus v-if="!addingLink" />
        {{ addingLink ? t("general.close") : t("auth.settings.profile.socialLinks.add") }}
      </Button>
    </div>

    <div v-if="addingLink" class="mt-3 flex flex-wrap items-end gap-2 rounded-md border border-gray-300 p-3 dark:border-gray-700">
      <InputDropdown v-model="linkType" :values="availableLinkTypes" :label="t('project.settings.links.typeField')" button-size="md" />
      <Button :disabled="!linkType" @click.prevent="addLink">{{ t("general.add") }}</Button>
    </div>

    <div class="mt-2">
      <div v-for="(_, type) in socials" :key="type" class="flex items-start gap-3 border-t border-gray-300 py-3 first:border-t-0 dark:border-gray-700">
        <IconMdiDiscord v-if="type === 'discord'" class="mt-0.5 shrink-0 text-xl text-gray-secondary" />
        <IconMdiGithub v-else-if="type === 'github'" class="mt-0.5 shrink-0 text-xl text-gray-secondary" />
        <IconMdiTwitter v-else-if="type === 'twitter'" class="mt-0.5 shrink-0 text-xl text-gray-secondary" />
        <IconMdiYoutube v-else-if="type === 'youtube'" class="mt-0.5 shrink-0 text-xl text-gray-secondary" />
        <IconMdiWeb v-else class="mt-0.5 shrink-0 text-xl text-gray-secondary" />

        <div class="min-w-0 flex-grow">
          <div class="font-semibold">{{ linkName(type) }}</div>
          <div v-show="editingType !== type" class="truncate text-sm text-gray-secondary">
            {{ socials[type] || t("auth.settings.profile.socialLinks.noValue") }}
          </div>
          <div v-show="editingType === type" class="mt-2">
            <InputText v-if="type === 'website'" v-model="socials[type]" label="URL" :rules="[required(), validUrl()]" />
            <InputText v-else v-model="socials[type]" :label="t('auth.settings.account.username')" :rules="[required()]" />
          </div>
        </div>

        <div class="flex shrink-0 gap-1 self-start">
          <Button
            :variant="editingType === type ? 'outline' : 'ghost'"
            :tone="editingType === type ? 'primary' : 'neutral'"
            size="sm"
            icon-only
            :title="t('auth.settings.profile.socialLinks.edit', [linkName(type)])"
            :aria-label="t('auth.settings.profile.socialLinks.edit', [linkName(type)])"
            @click.prevent="toggleEditing(type)"
          >
            <IconMdiCheck v-if="editingType === type" />
            <IconMdiPencil v-else />
          </Button>
          <Button
            variant="ghost"
            tone="danger"
            size="sm"
            icon-only
            :title="t('auth.settings.profile.socialLinks.remove', [linkName(type)])"
            :aria-label="t('auth.settings.profile.socialLinks.remove', [linkName(type)])"
            @click.prevent="removeLink(type)"
          >
            <IconMdiDelete />
          </Button>
        </div>
      </div>
    </div>

    <p v-if="Object.keys(socials).length === 0 && !addingLink" class="mt-4 text-sm text-gray-secondary">
      {{ t("auth.settings.profile.socialLinks.empty") }}
    </p>
  </section>
</template>
