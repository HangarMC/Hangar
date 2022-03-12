<script setup lang="ts">
import { PaginatedResult, Project } from "hangar-api";
import { PropType } from "vue";

const props = defineProps({
  projects: {
    type: Object as PropType<PaginatedResult<Project>>,
    required: true,
  },
});
</script>

<template>
  <div v-for="project in projects?.result" :key="project.name" bg="white" m="5px" p="5px" class="rounded-md flex">
    <div>
      <router-link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">{{ project.name }}</router-link>
      by
      <router-link :to="'/' + project.namespace.owner">{{ project.namespace.owner }}</router-link>
      <br />
      {{ project.description }}
    </div>
    <div class="flex-grow"></div>
    <div class="flex">
      Views: {{ project.stats.views }}<br />
      Downloads: {{ project.stats.downloads }}<br />
      Stars: {{ project.stats.stars }}
    </div>
  </div>
</template>
