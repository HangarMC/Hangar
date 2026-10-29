<script lang="ts" setup>
definePageMeta({
  loginRequired: true,
});

const route = useRoute("neworganization");
const router = useRouter();
const i18n = useI18n();
const v = useVuelidate();

const currentUser = useAuthStore().user;

const name = ref<string>("");
const loading = ref(false);

const maxOrgs = computed(() => useBackendData.validations.maxOrgCount);
const orgCount = computed(() => currentUser?.headerData.organizationCount ?? 0);
const canCreateMore = computed(() => orgCount.value < maxOrgs.value);
const canCreate = computed<boolean>(() => !v.value.$invalid && !v.value.$pending && !loading.value);

useSeo(computed(() => ({ title: i18n.t("organization.new.title"), route })));

async function create() {
  if (!canCreate.value) return;
  loading.value = true;
  try {
    await useInternalApi("organizations/create", "post", {
      name: name.value,
    });
    await router.push("/" + name.value);
  } catch (err: any) {
    handleRequestError(err);
    loading.value = false;
  }
}
</script>

<template>
  <Card>
    <h2 class="text-2xl font-bold">{{ i18n.t("organization.new.title") }}</h2>

    <div class="pt-2 flex flex-col gap-6">
      <p class="text-gray-secondary">{{ i18n.t("organization.new.text") }}</p>

      <FormSection
        v-if="currentUser && canCreateMore"
        :title="i18n.t('organization.new.name')"
        :description="i18n.t('organization.new.nameHint', [useBackendData.validations.org.min, useBackendData.validations.org.max])"
      >
        <template #icon><IconMdiAccountMultiplePlusOutline /></template>
        <InputText
          v-model.trim="name"
          :label="i18n.t('organization.new.name')"
          name="name"
          counter
          :maxlength="useBackendData.validations.org.max"
          :rules="[
            required(),
            minLength()(useBackendData.validations.org.min!),
            maxLength()(useBackendData.validations.org.max!),
            pattern()(useBackendData.validations.org.regex!),
            validOrgName(),
          ]"
          @keyup.enter="create"
        />
      </FormSection>

      <Alert v-else type="danger">
        {{ i18n.t("organization.new.error.tooManyOrgs", [maxOrgs]) }}
      </Alert>
    </div>

    <div v-if="currentUser && canCreateMore" class="mt-5 flex flex-wrap items-center justify-between gap-2 border-t border-gray-300 pt-4 dark:border-gray-700">
      <span class="text-sm text-gray-secondary tabular-nums">
        {{ i18n.t("organization.new.slotsUsed", [orgCount, maxOrgs]) }}
      </span>
      <Button :disabled="!canCreate" :loading="loading" @click="create">
        <IconMdiPlus />
        {{ i18n.t("organization.new.create") }}
      </Button>
    </div>
  </Card>
</template>
