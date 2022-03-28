<script setup lang="ts">
import { User } from "hangar-api";
import ProjectList from "~/components/projects/ProjectList.vue";
import Card from "~/components/design/Card.vue";
import { useI18n } from "vue-i18n";
import { avatarUrl } from "~/composables/useUrlHelper";
import UserAvatar from "~/components/UserAvatar.vue";
import Link from "~/components/design/Link.vue";
import MemberList from "~/components/projects/MemberList.vue";
import { useContext } from "vite-ssr/vue";
import { useOrgVisibility, useUserData } from "~/composables/useApiHelper";
import { useBackendDataStore } from "~/store/backendData";
import { useAuthStore } from "~/store/auth";
import { useRoute } from "vue-router";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";
import { Organization } from "hangar-internal";
import UserHeader from "~/components/UserHeader.vue";

const props = defineProps<{
  user: User;
  organization: Organization;
}>();
const i18n = useI18n();
const ctx = useContext();
const route = useRoute();

const { starred, watching, projects, organizations } = (await useUserData(props.user.name)).value || {};
let organizationVisibility = null;
if (props.user.name === useAuthStore().user?.name) {
  organizationVisibility = await useOrgVisibility(props.user.name);
}
const orgRoles = useBackendDataStore().orgRoles;

useHead(useSeo(props.user.name, props.user.tagline, route, avatarUrl(props.user.name)));
</script>

<template>
  <UserHeader :user="user" :organization="organization" />
  <div class="flex gap-4">
    <div class="flex-basis-full md:flex-basis-8/12 flex-grow">
      <ProjectList :projects="projects"></ProjectList>
    </div>
    <div class="flex-basis-full md:flex-basis-4/12 flex-grow">
      <template v-if="!user.isOrganization">
        <Card class="mb-4">
          <template #header>
            {{ i18n.t("author.orgs") }}
            <!-- todo org visibility modal -->
          </template>

          <ul>
            <li v-for="(orgRole, orgName) in organizations" :key="orgName">
              <router-link :to="'/' + orgName" class="flex">
                <UserAvatar :username="orgName" :avatar-url="avatarUrl(orgName)" size="xs" />
                &nbsp;
                {{ orgName }}
                &nbsp;
                {{ orgRole.role.title }}
                &nbsp;
                <span v-if="organizationVisibility && organizationVisibility[orgName]"> Hidden </span>
              </router-link>
            </li>
          </ul>

          <span v-if="!organizations || Object.keys(organizations).length === 0">
            {{ i18n.t("author.noOrgs", [props.user.name]) }}
          </span>
        </Card>

        <Card class="mb-4">
          <template #header>{{ i18n.t("author.stars") }}</template>

          <ul>
            <li v-for="star in starred.result" :key="star.name">
              <Link :to="'/' + star.namespace.owner + '/' + star.namespace.slug">
                {{ star.namespace.owner }}/<strong>{{ star.name }}</strong>
              </Link>
            </li>

            <span v-if="!starred || starred.result.length === 0">
              {{ i18n.t("author.noStarred", [props.user.name]) }}
            </span>
          </ul>
        </Card>

        <Card>
          <template #header>{{ i18n.t("author.watching") }}</template>

          <ul>
            <li v-for="watch in watching.result" :key="watch.name">
              <Link :to="'/' + watch.namespace.owner + '/' + watch.namespace.slug"
                >{{ watch.namespace.owner }}/<strong>{{ watch.name }}</strong></Link
              >
            </li>

            <span v-if="!watching || watching.result.length === 0">
              {{ i18n.t("author.noStarred", [props.user.name]) }}
            </span>
          </ul>
        </Card>
      </template>
      <MemberList v-else :model-value="organization.members" :roles="orgRoles" :org="true" />
    </div>
  </div>
</template>
