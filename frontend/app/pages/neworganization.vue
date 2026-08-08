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
    <template #header>
      <div class="flex items-center gap-2">
        <IconMdiAccountGroup class="flex-shrink-0 color-primary" />
        {{ i18n.t("organization.new.title") }}
      </div>
    </template>

    <p class="text-gray-secondary">{{ i18n.t("organization.new.text") }}</p>

    <template v-if="currentUser && canCreateMore">
      <div class="mt-4">
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
        <p class="mt-1 text-sm text-gray-secondary">
          {{ i18n.t("organization.new.nameHint", [useBackendData.validations.org.min, useBackendData.validations.org.max]) }}
        </p>
      </div>

      <div class="mt-5 flex flex-wrap items-center justify-between gap-2 border-t border-gray-300 pt-4 dark:border-gray-700">
        <span class="text-sm text-gray-secondary tabular-nums">
          {{ i18n.t("organization.new.slotsUsed", [orgCount, maxOrgs]) }}
        </span>
        <Button :disabled="!canCreate" :loading="loading" @click="create">
          <IconMdiPlus />
          {{ i18n.t("organization.new.create") }}
        </Button>
      </div>
    </template>

    <Alert v-else type="danger" class="mt-4">
      {{ i18n.t("organization.new.error.tooManyOrgs", [maxOrgs]) }}
    </Alert>
  </Card>
</template>
