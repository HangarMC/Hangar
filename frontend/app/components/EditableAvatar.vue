<script lang="ts" setup>
withDefaults(
  defineProps<{
    username: string;
    avatarUrl: string;
    action: string;
    size?: "xs" | "sm" | "md" | "lg" | "xl";
    compact?: boolean;
  }>(),
  {
    size: "lg",
    compact: false,
  }
);

const { t } = useI18n();
</script>

<template>
  <AvatarChangeModal :avatar="avatarUrl" :action="action">
    <template #activator="{ on }">
      <button
        type="button"
        class="group relative block overflow-hidden rounded-lg focus-visible:(outline-2 outline-primary-500)"
        :aria-label="t('organization.settings.changeAvatar')"
        :title="t('organization.settings.changeAvatar')"
        v-on="on"
      >
        <UserAvatar :username="username" :avatar-url="avatarUrl" :size="size" disable-link />
        <span
          class="absolute inset-x-0 bottom-0 flex items-center justify-center gap-1 rounded-b-lg bg-black/65 py-1 text-xs text-white transition-colors group-hover:bg-black/80"
        >
          <IconMdiCameraOutline />
          <span v-if="!compact">{{ t("organization.settings.changeAvatar") }}</span>
        </span>
      </button>
    </template>
  </AvatarChangeModal>
</template>
