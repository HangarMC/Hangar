<script lang="ts" setup>
import type { JsonNode } from "#shared/types/backend";

const props = defineProps<{
  socials: JsonNode;
  action: string;
  label?: string;
}>();

const newSocials = ref(props.socials);

const router = useRouter();
const i18n = useI18n();
const loading = ref(false);

async function save() {
  loading.value = true;
  try {
    await useInternalApi(props.action, "post", newSocials.value);
    router.go(0);
  } catch (err) {
    handleRequestError(err);
  }
  loading.value = false;
}
</script>

<template>
  <Modal :title="i18n.t('author.editSocials')" window-classes="w-200 text-lg">
    <SocialForm v-model="newSocials" compact />
    <template #activator="{ on }">
      <Button
        variant="ghost"
        tone="neutral"
        size="sm"
        :icon-only="!label"
        :title="label || i18n.t('general.edit')"
        :aria-label="label || i18n.t('general.edit')"
        v-on="on"
      >
        <IconMdiPencil />
        <span v-if="label">{{ label }}</span>
      </Button>
    </template>
    <template #footer="{ on }">
      <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
      <Button @click="save">{{ i18n.t("general.change") }}</Button>
    </template>
  </Modal>
</template>
