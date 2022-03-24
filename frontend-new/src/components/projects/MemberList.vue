<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { HangarProject } from "hangar-internal";
import Card from "~/components/design/Card.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import Link from "~/components/design/Link.vue";
import DropdownButton from "~/components/design/DropdownButton.vue";
import DropdownItem from "~/components/design/DropdownItem.vue";

const props = defineProps<{
  project: HangarProject;
  forceEdit?: boolean;
}>();
const i18n = useI18n();

const editing = ref(props.forceEdit);
</script>

<template>
  <Card>
    <template #header>{{ i18n.t("project.members") }}</template>
    <template #default>
      <div
        v-for="member in project.members"
        :key="member.user.name"
        class="p-2 w-full border border-neutral-100 dark:border-neutral-800 rounded inline-flex flex-row space-x-4"
      >
        <UserAvatar :username="member.user.name" size="sm" />
        <div class="flex-grow">
          <p class="font-semibold">
            <Link :to="'/' + member.user.name">{{ member.user.name }}</Link>
          </p>
          <p>{{ member.role.role.title }}</p>
        </div>
        <DropdownButton v-if="editing">
          <DropdownItem @click="alert('OK!')">Change role</DropdownItem>
        </DropdownButton>
      </div>
    </template>
  </Card>
</template>
