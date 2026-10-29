<script lang="ts" setup>
const { t } = useI18n();

const props = withDefaults(defineProps<{ compact?: boolean }>(), {
  compact: false,
});

const socials = defineModel<Record<string, string>>({ required: true });

const linkTypes = [
  { value: "github", text: "GitHub", base: "github.com/" },
  { value: "twitter", text: "Twitter", base: "twitter.com/" },
  { value: "discord", text: "Discord" },
  { value: "website", text: "Website" },
];

const draft = reactive(Object.fromEntries(linkTypes.map((type) => [type.value, socials.value?.[type.value] ?? ""])));

// a link exists only while it has a value, so emptying a field removes it
// immediate so retired types stored on old profiles are dropped even if nothing is edited
watch(
  draft,
  () => {
    socials.value = Object.fromEntries(linkTypes.map((type) => [type.value, draft[type.value]?.trim() ?? ""]).filter(([, value]) => value));
  },
  { immediate: true }
);

function hint(type: (typeof linkTypes)[number]) {
  if (type.value === "discord") return t("auth.settings.profile.socialLinks.discordHint");
  if (type.value === "website") return t("auth.settings.profile.socialLinks.websiteHint");
  return type.base + (draft[type.value]?.trim() || t("auth.settings.profile.socialLinks.usernamePlaceholder"));
}
</script>

<template>
  <section :class="{ 'mt-6': !props.compact }">
    <h3 class="text-lg font-bold">{{ t("auth.settings.profile.socialLinks.title") }}</h3>
    <p class="mt-0.5 text-sm text-gray-secondary">{{ t("auth.settings.profile.socialLinks.description") }}</p>

    <div class="mt-3 flex flex-col gap-4">
      <div v-for="type in linkTypes" :key="type.value" class="flex items-start gap-3">
        <IconMdiGithub v-if="type.value === 'github'" class="mt-3 shrink-0 text-xl text-gray-secondary" />
        <IconMdiTwitter v-else-if="type.value === 'twitter'" class="mt-3 shrink-0 text-xl text-gray-secondary" />
        <IconMdiDiscord v-else-if="type.value === 'discord'" class="mt-3 shrink-0 text-xl text-gray-secondary" />
        <IconMdiWeb v-else class="mt-3 shrink-0 text-xl text-gray-secondary" />

        <div class="min-w-0 flex-1">
          <InputText v-model="draft[type.value]" :label="type.text" :rules="type.value === 'website' ? [validUrl()] : []" />
          <p class="mt-1 truncate text-xs text-gray-secondary">{{ hint(type) }}</p>
        </div>
      </div>
    </div>
  </section>
</template>
