<script lang="ts" setup>
import { User } from "hangar-api";
import Card from "~/components/design/Card.vue";
import { useI18n } from "vue-i18n";
import ProjectInfo from "~/components/projects/ProjectInfo.vue";
import { HangarProject } from "hangar-internal";
import UserAvatar from "~/components/UserAvatar.vue";
import Link from "~/components/design/Link.vue";

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();
const i18n = useI18n();
</script>

<template>
  <div class="flex flex-col <lg:space-y-4 lg:flex-row lg:space-x-4">
    <section class="flex-grow">
      <Card>
        Project readme
        <br />
        {{ project }}
      </Card>
    </section>
    <section class="min-w-[320px] space-y-4">
      <ProjectInfo :project="project"></ProjectInfo>
      <Card>
        <template #header>{{ i18n.t("project.promotedVersions") }}</template>
        <template #default>Promoted versions go here</template>
      </Card>
      <Card>
        <template #header>{{ i18n.t("page.plural") }}</template>
        <template #default>Page navigation goes here</template>
      </Card>
      <Card>
        <template #header>{{ i18n.t("project.members") }}</template>
        <template #default>
          <div v-for="member in project.members" :key="member.user.name" class="p-2 w-full border border-neutral-100 rounded inline-flex flex-row space-x-4">
            <UserAvatar :username="member.user.name" size="sm" />
            <div>
              <p class="font-semibold">
                <Link :to="'/' + member.user.name">{{ member.user.name }}</Link>
              </p>
              <p>{{ member.role.role.title }}</p>
            </div>
          </div>
        </template>
      </Card>
    </section>
  </div>
</template>
