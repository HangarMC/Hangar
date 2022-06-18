<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Markdown from "~/components/Markdown.vue";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { useInternalApi } from "~/composables/useApi";
import { computed, ref } from "vue";
import { useContext } from "vite-ssr/vue";
import { handleRequestError } from "~/composables/useErrorHandling";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";

const props = defineProps<{
  modelValue: { [key: string]: boolean };
}>();

const i18n = useI18n();
const ctx = useContext();

const emit = defineEmits<{
  (e: "update:modelValue", value: { [key: string]: boolean }): void;
}>();

const loading = ref(false);

const internalVisibility = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});

async function changeOrgVisibility(org: string) {
  loading.value = true;
  await useInternalApi<{ [key: string]: boolean }>(`organizations/${org}/userOrganizationsVisibility`, true, "POST", internalVisibility.value[org] as any, {
    "Content-Type": "application/json",
  }).catch((e) => handleRequestError(e, ctx, i18n));
  loading.value = false;
}
</script>

<template>
  <Modal :title="i18n.t('author.editOrgVisibility')">
    <template #default="{ on }">
      <p>{{ i18n.t("author.orgVisibilityModal") }}</p>

      <ul>
        <li v-for="(hidden, org) in internalVisibility" :key="org">
          <InputCheckbox v-model="internalVisibility[org]" :label="org" :disabled="loading" @change="changeOrgVisibility(org)"></InputCheckbox>
        </li>
      </ul>

      <Button button-type="secondary" class="mt-2" v-on="on">{{ i18n.t("general.close") }}</Button>
    </template>
    <template #activator="{ on }">
      <Button class="text-sm" v-on="on"><IconMdiPencil /></Button>
    </template>
  </Modal>
</template>
