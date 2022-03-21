<script setup lang="ts">
import { PropType } from "vue";
import { Project, User } from "hangar-api";
import UserAvatar from "~/components/UserAvatar.vue";
import Button from "~/components/design/Button.vue";

defineProps({
  user: {
    type: Object as PropType<User>,
    required: true,
  },
  project: {
    type: Object as PropType<Project>,
    required: true,
  },
});
</script>

<template>
  <section class="background-header rounded-lg p-4">
    <div class="flex">
      <UserAvatar :username="project.namespace.owner"></UserAvatar>
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
  </section>
</template>
