<script lang="ts" setup>
import { useDataLoader } from "~/composables/useDataLoader";
import type { User } from "~/types/backend";

const { data: user } = useDataLoader<User>("user");

definePageMeta({
  dataLoader_user: true,
});
</script>

<template>
  <div>
    <router-view v-slot="{ Component }">
      <Suspense>
        <div>
          <!-- todo fix org -->
          <component :is="Component" :user="user" :organization="undefined" />
        </div>
      </Suspense>
    </router-view>
  </div>
</template>
