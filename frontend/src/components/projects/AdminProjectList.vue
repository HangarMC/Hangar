<script lang="ts" setup>
import type { ProjectApproval } from "hangar-internal";
import { useI18n } from "vue-i18n";
import Alert from "~/components/design/Alert.vue";
import Markdown from "~/components/Markdown.vue";
import Link from "~/components/design/Link.vue";
import VisibilityChangerModal from "~/components/modals/VisibilityChangerModal.vue";

const i18n = useI18n();
defineProps<{
  projects: ProjectApproval[];
}>();
</script>

<template>
  <ul v-if="projects.length">
    <template v-for="project in projects" :key="project.projectId">
      <hr />
      <li>
        <div class="flex lt-md:flex-col items-center">
          <div class="basis-full md:basis-3/12">
            {{ project.changeRequester }} Requested changes on
            <Link :to="`/${project.namespace.owner}/${project.namespace.slug}`" target="_blank">
              {{ `${project.namespace.owner}/${project.namespace.slug}` }}
            </Link>
          </div>
          <div class="basis-full md:basis-6/12 flex-grow">
            <Markdown :raw="project.comment" />
          </div>
          <div class="">
            <VisibilityChangerModal :prop-visibility="project.visibility" type="project" :post-url="`projects/visibility/${project.projectId}`" />
          </div>
        </div>
      </li>
    </template>
  </ul>
  <Alert v-else type="info">
    {{ i18n.t("projectApproval.noProjects") }}
  </Alert>
</template>
