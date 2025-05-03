<script lang="ts" setup>
const i18n = useI18n();
const notification = useNotificationStore();

if (!import.meta.env.DEV) {
  await navigateTo("/");
}

const form = reactive({
  username: "",
  email: "",
  password: "",
  admin: false,
});

async function createUser() {
  try {
    await useInternalApi("onboarding/createUser", "POST", form);
    notification.success("Done");
  } catch (err: any) {
    notification.fromError(i18n, err);
  }
}

const usersCount = ref("100");
const projectsPerUser = ref("5");
async function createFakeData() {
  try {
    await useInternalApi("onboarding/generateFakeData", "GET", {
      users: usersCount.value,
      projectsPerUser: projectsPerUser.value,
    });
    notification.success("Done");
  } catch (err: any) {
    notification.fromError(i18n, err);
  }
}

async function createE2EData() {
  try {
    await useInternalApi("onboarding/generateE2EData", "GET", {}, { timeout: 20_000_000 });
    notification.success("Done");
  } catch (err: any) {
    notification.fromError(i18n, err);
  }
}
</script>

<template>
  <div>
    <DevOnly>
      <h1 class="text-2xl font-bold mb-4">Onboarding</h1>

      <div class="grid cols-2 gap-4">
        <Card>
          <template #header> Create new verified user </template>
          <form class="grid gap-2" @submit="createUser">
            <InputText v-model="form.username" label="Username" />
            <InputText v-model="form.email" type="email" label="E-Mail" />
            <InputPassword v-model="form.password" label="Password" />
            <InputCheckbox v-model="form.admin" label="Admin?" />
            <Button type="submit" @click.prevent="createUser">Create</Button>
          </form>
        </Card>
        <Card>
          <template #header> Create Fake Data (requires login)</template>
          <form class="grid gap-2" @submit="createFakeData">
            <InputText v-model="usersCount" type="number" label="Users" />
            <InputText v-model="projectsPerUser" type="number" label="Projects per User" />
            <Button class="mt-2" type="submit" @click.prevent="createFakeData">Create</Button>
          </form>
        </Card>
        <Card>
          <template #header> Create E2E Data (requires login)</template>
          <Button @click="createE2EData">Create</Button>
        </Card>
      </div>
    </DevOnly>
  </div>
</template>
