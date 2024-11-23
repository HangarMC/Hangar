<script lang="ts" setup>
const props = defineProps<{
  modelValue: { [key: string]: boolean };
}>();

const i18n = useI18n();

const emit = defineEmits<{
  (e: "update:modelValue", value: { [key: string]: boolean }): void;
}>();

const loading = ref(false);

const internalVisibility = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});

const orgs = computed(() => Object.keys(internalVisibility.value));

async function changeOrgVisibility(org: string) {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const hidden = internalVisibility.value[org];
  await useInternalApi(`organizations/${org}/userOrganizationsVisibility?hidden=${hidden}`, "post").catch((err) => handleRequestError(err));
  loading.value = false;
}
</script>

<!-- todo: save on save button, not immediately when ticking -->
<template>
  <Modal :title="i18n.t('author.editOrgVisibility')" window-classes="w-120">
    <p>{{ i18n.t("author.orgVisibilityModal") }}</p>

    <ul class="p-2">
      <li v-for="org in orgs" :key="org">
        <InputCheckbox v-model="internalVisibility[org]" :label="org" :disabled="loading" @change="changeOrgVisibility(org)" />
      </li>
    </ul>
    <template #activator="{ on }">
      <Button class="text-sm" v-on="on"><IconMdiPencil /></Button>
    </template>
  </Modal>
</template>
