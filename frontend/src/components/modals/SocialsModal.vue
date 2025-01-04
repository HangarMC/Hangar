<script lang="ts" setup>
import type { JsonNode } from "~/types/backend";

const props = defineProps<{
  socials: JsonNode;
  action: string;
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
    <SocialForm v-model="newSocials" />
    <Button class="mt-3" @click="save">{{ i18n.t("general.change") }}</Button>
    <template #activator="{ on }">
      <Button size="small" class="ml-2 inline-flex text-lg" v-on="on">
        <IconMdiPencil />
      </Button>
    </template>
  </Modal>
</template>
