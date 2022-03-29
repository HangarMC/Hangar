<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { JoinableMember } from "hangar-internal";
import Card from "~/components/design/Card.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import Link from "~/components/design/Link.vue";
import DropdownButton from "~/components/design/DropdownButton.vue";
import DropdownItem from "~/components/design/DropdownItem.vue";
import { avatarUrl } from "~/composables/useUrlHelper";
import Button from "~/components/design/Button.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";

const props = defineProps<{
  modelValue: JoinableMember[];
  forceEdit?: boolean;
  disableSaveButton?: boolean;
  class?: string;
}>();
const emit = defineEmits<{
  (e: "update:modelValue", value: JoinableMember[]): void;
}>();
const value = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});
const i18n = useI18n();

const editing = ref(props.forceEdit);
const dirty = ref<boolean>(false);
</script>

<template>
  <Card :class="props.class">
    <template #header>
      <div class="inline-flex w-full flex-cols space-between">
        <span class="flex-grow">{{ i18n.t("project.members") }}</span>
        <Button v-if="!editing && hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)" class="mr-1 inline-flex" size="medium" @click="editing = true"
          ><IconMdiPencil class="text-xs" :title="i18n.t('general.edit')"
        /></Button>
        <Button v-else-if="!dirty" class="mr-1 inline-flex" @click="editing = false"><IconMdiClose /></Button>
        <Button v-if="editing && !disableSaveButton" :disabled="!dirty" class="inline-flex items-center" @click="save"
          ><IconMdiCheck /> <span class="text-sm">{{ i18n.t("general.save") }}</span></Button
        >
      </div>
    </template>

    <template #default>
      <div
        v-for="member in modelValue"
        :key="member.user.name"
        class="p-2 w-full border border-neutral-100 dark:border-neutral-800 rounded inline-flex flex-row space-x-4"
      >
        <UserAvatar :username="member.user.name" :avatar-url="avatarUrl(member.user.name)" size="sm" />
        <div class="flex-grow">
          <p class="font-semibold">
            <Link :to="'/' + member.user.name">{{ member.user.name }}</Link>
          </p>
          <p>{{ member.role.role.title }}</p>
        </div>
        <DropdownButton v-if="editing" name="">
          <!-- todo change role -->
          <DropdownItem @click="alert('OK!')">Change role</DropdownItem>
        </DropdownButton>
      </div>
    </template>
  </Card>
</template>
