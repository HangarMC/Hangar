<script lang="ts" setup>
import type { Ref } from "vue";
import type { User } from "hangar-api";

const route = useRoute("user");
const user = ref();
const organization = ref();
const blocking = computed(() => route.name === "user");
if (blocking.value) {
  const u = await useUser(route.params.user);
  await cb(u);
} else {
  useUser(route.params.user).then(cb);
}

async function cb(u: Ref<User | null>) {
  if (!u || !u.value) {
    throw useErrorRedirect(route, 404, "Not found");
  } else if (route.params.user !== u.value?.name) {
    const newPath = route.fullPath.replace(route.params.user, u.value!.name);
    console.debug("Redirect to " + newPath + " from (" + route.fullPath + ")");
    await navigateTo(newPath);
    useDummyError();
  } else if (u.value?.isOrganization) {
    organization.value = (await useOrganization(route.params.user)).value;
  }
  user.value = u.value;
}
</script>

<template>
  <div>
    <router-view v-if="!blocking || user" v-slot="{ Component }">
      <Suspense>
        <div>
          <component :is="Component" :user="user" :organization="organization" />
        </div>
      </Suspense>
    </router-view>
  </div>
</template>
