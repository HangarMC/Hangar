<script setup lang="ts">
import type { NamedPermission, PermissionGroup } from "#shared/types/backend";

const props = withDefaults(
  defineProps<{
    groups: PermissionGroup[];
    saving?: boolean;
    valid?: boolean;
    locked?: boolean;
    lockedHint?: string;
    saveLabel?: string;
    groupState: (group: PermissionGroup) => "all" | "some" | "none";
  }>(),
  {
    saving: false,
    valid: true,
    locked: false,
    lockedHint: undefined,
    saveLabel: undefined,
  }
);

const emit = defineEmits<{
  toggleGroup: [group: PermissionGroup];
  save: [];
  cancel: [];
}>();

const title = defineModel<string>("title", { required: true });
const permissions = defineModel<NamedPermission[]>("permissions", { required: true });

const i18n = useI18n();

const permLabel = (perm: string) => i18n.t("permission." + perm);
const groupLabel = (group: PermissionGroup) => i18n.t("permissionGroup." + group.name);

function toggle(perm: NamedPermission) {
  permissions.value = permissions.value.includes(perm) ? permissions.value.filter((p) => p !== perm) : [...permissions.value, perm];
}
</script>

<template>
  <div class="rounded-md border border-gray-300 p-3 dark:border-gray-700">
    <div class="max-w-xs">
      <InputText v-model="title" :label="i18n.t('form.memberList.title')" :maxlength="32" counter />
    </div>
    <p class="mt-1 text-xs text-gray-secondary">{{ i18n.t("form.memberList.titleHint") }}</p>

    <p v-if="props.locked" class="mt-3 text-sm text-gray-secondary">{{ props.lockedHint }}</p>

    <div v-else class="mt-3 flex flex-col gap-3">
      <div v-for="group in props.groups" :key="group.name">
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="flex items-center gap-2 text-sm font-semibold transition-colors hover:text-black dark:hover:text-white"
            :aria-pressed="props.groupState(group) === 'all'"
            @click="emit('toggleGroup', group)"
          >
            <span
              class="h-4 w-4 flex flex-shrink-0 items-center justify-center rounded-sm border transition-colors"
              :class="props.groupState(group) === 'none' ? 'border-gray-400 dark:border-gray-500' : 'mpe-box-on'"
            >
              <IconMdiCheckBold v-if="props.groupState(group) === 'all'" class="text-[10px]" />
              <IconMdiMinus v-else-if="props.groupState(group) === 'some'" class="text-[10px]" />
            </span>
            {{ groupLabel(group) }}
          </button>
        </div>
        <div class="mt-1 grid gap-x-4 gap-y-1 pl-6 sm:grid-cols-2 lg:grid-cols-3">
          <InputCheckbox
            v-for="perm in group.permissions"
            :key="perm"
            :model-value="permissions.includes(perm)"
            :label="permLabel(perm)"
            @update:model-value="toggle(perm)"
          />
        </div>
      </div>
    </div>

    <div class="mt-4 flex flex-wrap items-center gap-2">
      <Button :disabled="!props.valid || props.saving" :loading="props.saving" size="sm" @click="emit('save')">
        {{ props.saveLabel ?? i18n.t("general.save") }}
      </Button>
      <Button variant="ghost" tone="neutral" size="sm" :disabled="props.saving" @click="emit('cancel')">
        {{ i18n.t("general.cancel") }}
      </Button>
    </div>
  </div>
</template>

<style scoped>
.mpe-box-on {
  border-color: var(--gray-500);
  background-color: var(--gray-500);
  color: var(--gray-50);
}

.dark .mpe-box-on {
  border-color: var(--gray-400);
  background-color: var(--gray-400);
  color: var(--gray-900);
}
</style>
