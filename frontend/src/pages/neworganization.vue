<script lang="ts" setup>
definePageMeta({
  loginRequired: true,
});

const route = useRoute();
const router = useRouter();
const i18n = useI18n();
const v = useVuelidate();

const currentUser = useAuthStore().user;

const name = ref<string>("");

const canCreate = computed<boolean>(() => !v.value.$invalid && !v.value.$pending);

useHead(useSeo(i18n.t("organization.new.title"), null, route, null));

async function create() {
  try {
    await useInternalApi("organizations/create", "post", {
      name: name.value,
    });
    await router.push("/" + name.value);
  } catch (e: any) {
    handleRequestError(e);
  }
}
</script>

<template>
  <Card>
    <template #header>{{ i18n.t("organization.new.title") }}</template>
    <p class="mb-3">{{ i18n.t("organization.new.text") }}</p>

    <div v-if="currentUser && currentUser.headerData.organizationCount < useBackendData.validations.maxOrgCount" class="mt-2">
      <InputText
        v-model="name"
        class="max-w-100 pr-2"
        :label="i18n.t('organization.new.name')"
        counter
        :maxlength="useBackendData.validations.org.max"
        :rules="[
          required(),
          minLength()(useBackendData.validations.org.min),
          maxLength()(useBackendData.validations.org.max),
          pattern()(useBackendData.validations.org.regex),
          validOrgName(),
        ]"
      />

      <Button size="medium" :disabled="!canCreate" @click="create">
        {{ i18n.t("form.memberList.create") }}
      </Button>
    </div>

    <Alert v-else type="danger">
      {{ i18n.t("organization.new.error.tooManyOrgs", [useBackendData.validations.maxOrgCount]) }}
    </Alert>
  </Card>
</template>
