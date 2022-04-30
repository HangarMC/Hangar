<script lang="ts" setup>
import { ProjectApproval } from "hangar-internal";
import Alert from "~/components/design/Alert.vue";
import { useI18n } from "vue-i18n";
import Markdown from "~/components/Markdown.vue";
import Link from "~/components/design/Link.vue";
import VisibilityChangerModal from "~/components/modals/VisibilityChangerModal.vue";

const i18n = useI18n();
const props = defineProps<{
  projects: ProjectApproval[];
}>();
</script>

<template>
  <ul v-if="projects.length">
    <template v-for="project in projects" :key="project.projectId">
      <hr class="mb-3" />
      <li>
        <div class="flex">
          <div class="flex-grow">
            {{ i18n.t("projectApproval.description", [project.changeRequester, `${project.namespace.owner}/${project.namespace.slug}`]) }}
            <Link :to="`/${project.namespace.owner}/${project.namespace.slug}`" target="_blank">
              <IconMdiOpenInNew />
            </Link>
          </div>
          <div class="flex-shrink">
            <VisibilityChangerModal :prop-visibility="project.visibility" small-btn type="project" :post-url="`projects/visibility/${project.projectId}`" />
          </div>
          <div class="basis-full">
            <Markdown :raw="project.comment" class="mb-3" />
          </div>
        </div>
      </li>
    </template>
  </ul>
  <Alert type="danger" v-else>
    {{ i18n.t("projectApproval.noProjects") }}
  </Alert>
</template>
