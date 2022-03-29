<script lang="ts" setup>
import { Organization } from "hangar-internal";
import { User } from "hangar-api";
import UserAvatar from "~/components/UserAvatar.vue";
import { avatarUrl, forumUserUrl } from "~/composables/useUrlHelper";
import { useI18n } from "vue-i18n";
import Card from "~/components/design/Card.vue";

const props = defineProps<{
  user: User;
  organization: Organization;
}>();

const i18n = useI18n();
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
          <span v-else-if="canEditCurrentUser">{{ $t("author.addTagline") }}</span>
          <!-- todo tag line change modal -->
          <!-- todo user log modal -->
        </div>
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
