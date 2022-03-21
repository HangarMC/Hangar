<script setup lang="ts">
import { computed, PropType } from "vue";
import { Project } from "hangar-api";
import UserAvatar from "~/components/UserAvatar.vue";
import Button from "~/components/design/Button.vue";
import Card from "~/components/design/Card.vue";
import { projectIconUrl } from "~/composables/useUrlHelper";

const props = defineProps({
  project: {
    type: Object as PropType<Project>,
    required: true,
  },
});

const imageUrl = computed(() => {
  return projectIconUrl(props.project.namespace.owner, props.project.name);
});
</script>

<template>
  <Card>
    <div class="flex">
      <UserAvatar :username="project.namespace.owner" :to="'/' + project.namespace.owner + '/' + project.name" :img-src="imageUrl"></UserAvatar>
      <div class="flex-grow mx-4">
        <p class="text-2xl">
          <router-link :to="'/' + project.namespace.owner">{{ project.namespace.owner }}</router-link>
          <span class="text-gray-500 dark:text-gray-400"> / </span>
          <span class="text-medium">{{ project.name }}</span>
        </p>
        <p>{{ project.description }}</p>
      </div>
      <div>
        <Button>Download latest</Button>
      </div>
    </div>
  </Card>
</template>
