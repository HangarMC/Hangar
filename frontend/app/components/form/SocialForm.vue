<script lang="ts" setup>
const { t } = useI18n();
const notification = useNotificationStore();

const socials = defineModel<Record<string, string>>({ required: true });

defineSlots<{
  actions?: () => unknown;
}>();

const linkType = ref<string>("discord");
const linkTypes = [
  { value: "discord", text: "Discord" },
  { value: "github", text: "GitHub" },
  { value: "twitter", text: "Twitter" },
  { value: "youtube", text: "YouTube" },
  { value: "website", text: "Website" },
];

const selectedLinkType = computed(() => linkTypes.find((type) => type.value === linkType.value));

function addLink() {
  if (!linkType.value) {
    return notification.error("You have to select a type");
  }
  if (Object.keys(socials.value).includes(linkType.value)) {
    return notification.error("You already have a link of that type added");
  }
  socials.value[linkType.value] = "";
}

function removeLink(type: string) {
  delete socials.value[type];
}
</script>

<template>
  <div class="space-y-2">
    <div v-for="(_, type) in socials" :key="type" class="flex items-center gap-2 rounded-lg border border-gray-200 !bg-transparent p-2 dark:!border-gray-800">
      <span class="inline-flex h-9 w-9 flex-shrink-0 items-center justify-center text-lg text-gray">
        <IconMdiDiscord v-if="type === 'discord'" />
        <IconMdiGithub v-else-if="type === 'github'" />
        <IconMdiTwitter v-else-if="type === 'twitter'" />
        <IconMdiYoutube v-else-if="type === 'youtube'" />
        <IconMdiWeb v-else />
      </span>
      <div class="relative flex h-10.5 min-w-0 flex-grow rounded-md transition-all duration-200">
        <input
          v-model="socials[type]"
          class="min-w-0 flex-grow truncate rounded-lg border border-transparent bg-gray-100 px-3 py-2 outline-none transition-colors hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-600"
          :placeholder="type === 'website' ? 'https://example.com' : t('auth.settings.account.username')"
          type="text"
        />
      </div>
      <button
        class="inline-flex h-10.5 w-10.5 flex-shrink-0 items-center justify-center rounded-lg border border-transparent transition-all duration-250 hover:border-red-600 hover:bg-red-900/50"
        :title="`Remove ${linkTypes.find((e) => e.value === type)?.text}`"
        @click.prevent="removeLink(type)"
      >
        <IconMdiBin />
      </button>
    </div>

    <p v-if="Object.keys(socials).length === 0" class="rounded-lg border border-dashed p-4 text-center text-sm text-gray dark:border-gray-700">
      No social links added yet.
    </p>
  </div>

  <div class="mt-3 flex flex-wrap items-center gap-2">
    <DropdownButton button-size="medium" button-type="transparent" match-width spread-arrow>
      <template #button-label>
        <div class="flex w-36 items-center justify-start gap-2">
          <IconMdiLinkVariant />
          <span class="truncate">{{ selectedLinkType?.text || "Link type" }}</span>
        </div>
      </template>
      <template #default="{ close }">
        <DropdownItem
          v-for="type in linkTypes"
          :key="type.value"
          :style="
            linkType === type.value
              ? {
                  backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                  borderColor: 'var(--primary-500)',
                }
              : {}
          "
          @click="
            linkType = type.value;
            close();
          "
        >
          {{ type.text }}
        </DropdownItem>
      </template>
    </DropdownButton>
    <Button button-type="secondary" size="medium" @click.prevent="addLink">
      <IconMdiPlus class="mr-1" />
      Add link
    </Button>
    <div class="ml-auto">
      <slot name="actions" />
    </div>
  </div>
</template>
