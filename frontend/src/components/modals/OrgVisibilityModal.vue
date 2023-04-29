<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { computed, ref } from "vue";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";

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

async function changeOrgVisibility(org: string) {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const hidden = internalVisibility.value[org];
  await useInternalApi(`organizations/${org}/userOrganizationsVisibility?hidden=${hidden}`, "post").catch((e) => handleRequestError(e));
  loading.value = false;
}
</script>

<!-- todo: save on save button, not immediately when ticking -->
<template>
  <Modal :title="i18n.t('author.editOrgVisibility')" window-classes="w-120">
    <p>{{ i18n.t("author.orgVisibilityModal") }}</p>

    <ul class="p-2">
      <li v-for="(hidden, org) in internalVisibility" :key="org">
        <InputCheckbox v-model="internalVisibility[org]" :label="org" :disabled="loading" @change="changeOrgVisibility(org)"></InputCheckbox>
      </li>
    </ul>
    <template #activator="{ on }">
      <Button class="text-sm" v-on="on"><IconMdiPencil /></Button>
    </template>
  </Modal>
</template>
