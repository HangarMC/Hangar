<script lang="ts" setup>
import { Project, User } from "hangar-api";
import Card from "~/components/design/Card.vue";
import { useI18n } from "vue-i18n";
import ProjectInfo from "~/components/projects/ProjectInfo.vue";
import { HangarProject } from "hangar-internal";

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
        <template #header>Promoted versions</template>
        <template #default>Promoted versions go here</template>
      </Card>
    </section>
  </div>
</template>
