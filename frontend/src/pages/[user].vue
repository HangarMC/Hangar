<script lang="ts" setup>
import { useDataLoader } from "~/composables/useDataLoader";

const { data: user } = useDataLoader("user");
const { data: org } = useDataLoader("organization");

definePageMeta({
  dataLoader_user: true,
  dataLoader_organization: true,
});
</script>

<template>
  <div>
    <router-view v-slot="{ Component }">
      <Suspense>
        <div>
          <component :is="Component" :user="user" :organization="org" />
        </div>
      </Suspense>
    </router-view>
  </div>
</template>
