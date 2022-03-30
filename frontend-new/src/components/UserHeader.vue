<script lang="ts" setup>
import { Organization } from "hangar-internal";
import { User } from "hangar-api";
import UserAvatar from "~/components/UserAvatar.vue";
import { avatarUrl, forumUserUrl } from "~/composables/useUrlHelper";
import { useI18n } from "vue-i18n";
import Card from "~/components/design/Card.vue";
import TaglineModal from "~/components/modals/TaglineModal.vue";
import { computed, FunctionalComponent } from "vue";
import { NamedPermission } from "~/types/enums";
import { hasPerms } from "~/composables/usePerm";
import { useAuthStore } from "~/store/auth";
import Button from "~/components/design/Button.vue";
import Tooltip from "~/components/design/Tooltip.vue";
import IconMdiWrench from "~icons/mdi/wrench";
import IconMdiCog from "~icons/mdi/cog";
import IconMdiKey from "~icons/mdi/key";
import IconMdiCalendar from "~icons/mdi/calendar";
import Link from "~/components/design/Link.vue";

const props = defineProps<{
  user: User;
  organization: Organization;
}>();

const i18n = useI18n();
const authStore = useAuthStore();

const isCurrentUser = computed(() => {
  return authStore.user && authStore.user.name === props.user.name;
});

const canEditCurrentUser = computed(() => {
  return hasPerms(NamedPermission.EDIT_ALL_USER_SETTINGS) || isCurrentUser || hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS);
});

interface UserButton {
  icon: FunctionalComponent;
  action?: () => void;
  attr: {
    to?: string;
    href?: string;
  };
  name: string;
}

const buttons = computed<UserButton[]>(() => {
  const list = [] as UserButton[];
  if (!props.user.isOrganization) {
    if (isCurrentUser.value) {
      list.push({ icon: IconMdiCog, attr: { href: `${import.meta.env.HANGAR_AUTH_HOST}/account/settings` }, name: "settings" });
    }
    if (isCurrentUser.value || hasPerms(NamedPermission.EDIT_ALL_USER_SETTINGS)) {
      list.push({ icon: IconMdiKey, attr: { to: "/" + props.user.name + "/settings/api-keys" }, name: "apiKeys" });
    }
  }
  if ((hasPerms(NamedPermission.MOD_NOTES_AND_FLAGS) || hasPerms(NamedPermission.REVIEWER)) && !props.user.isOrganization) {
    list.push({ icon: IconMdiCalendar, attr: { to: `/admin/activities/${props.user.name}` }, name: "activity" });
  }
  if (hasPerms(NamedPermission.EDIT_ALL_USER_SETTINGS)) {
    list.push({ icon: IconMdiWrench, attr: { to: "/admin/user/" + props.user.name }, name: "admin" });
  }

  return list;
});
</script>

<template>
  <Card accent>
    <div class="flex mb-4">
      <div>
        <UserAvatar :username="user.name" :avatar-url="avatarUrl(user.name)" />
        <!-- todo org avatar changing -->
      </div>

      <div class="ml-2">
        <h1 class="text-2xl text-strong inline-flex items-center">
          {{ user.name }}
          <a v-if="!user.isOrganization" class="inline-flex mx-1" :href="forumUserUrl(user.name)" :title="i18n.t('author.viewOnForums')"
            ><IconMdiOpenInNew
          /></a>
        </h1>

        <div>
          <span v-if="user.tagline">{{ user.tagline }}</span>
          <span v-else-if="canEditCurrentUser">{{ i18n.t("author.addTagline") }}</span>
          <TaglineModal :tagline="user.tagline" :action="`${user.isOrganization ? 'organizations/org' : 'users'}/${user.name}/settings/tagline`" />
        </div>

        <Tooltip v-for="btn in buttons" :key="btn.name" :content="i18n.t(`author.tooltips.${btn.name}`)">
          <Link v-bind="btn.attr">
            <Button size="small" class="mr-1 mt-1 inline-flex"><component :is="btn.icon" /></Button>
          </Link>
        </Tooltip>

        <!-- todo lock user modal -->
      </div>

      <div class="flex-grow" />
      <div class="flex flex-col space-y-1 items-end">
        <span>{{ i18n.t("author.memberSince", [i18n.d(user.joinDate, "date")]) }}</span>
        <span>{{ i18n.t("author.numProjects", [user.projectCount], user.projectCount) }}</span>
        <span v-for="role in user.roles" :key="role.roleId" :style="{ backgroundColor: role.color }" class="rounded p-1 w-max items-end">{{ role.title }}</span>
      </div>
    </div>
  </Card>
  <hr class="border-gray-400 dark:border-gray-500 my-4 mb-4" />
</template>
