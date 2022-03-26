<script setup lang="ts">
import { PaginatedResult, Project } from "hangar-api";
import { PropType } from "vue";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { projectIconUrl } from "~/composables/useUrlHelper";

const props = defineProps({
  projects: {
    type: Object as PropType<PaginatedResult<Project>>,
    required: true,
  },
});
</script>

<template>
  <Card v-for="project in projects?.result" :key="project.name" class="flex mb-2 space-x-4">
    <div>
      <UserAvatar
        :username="project.namespace.owner"
        :to="'/' + project.namespace.owner + '/' + project.name"
        :img-src="projectIconUrl(project.namespace.owner, project.name)"
        size="md"
      />
    </div>
    <div>
      <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">{{ project.name }}</Link>
      by
      <Link :to="'/' + project.namespace.owner">{{ project.namespace.owner }}</Link>
      <br />
      {{ project.description }}
    </div>
    <div class="flex-grow"></div>
    <div class="flex">
      Views: {{ project.stats.views }}<br />
      Downloads: {{ project.stats.downloads }}<br />
      Stars: {{ project.stats.stars }}
    </div>
  </Card>
</template>
