<script lang="ts" setup>
const props = defineProps<{
  organization: string;
}>();

const i18n = useI18n();
const router = useRouter();

const comment = ref<string>("");
const loading = ref<boolean>(false);

async function deleteOrg() {
  if (loading.value) {
    return;
  }

  loading.value = true;
  try {
    await useInternalApi(`organizations/org/${props.organization}/delete`, "post", {
      content: comment.value,
    });
    await router.push("/");
  } catch (err) {
    handleRequestError(err);
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  comment.value = "";
}
</script>

<template>
  <Modal
    :title="i18n.t('organization.settings.deleteModal.title', [organization])"
    window-classes="w-full max-w-xl !rounded-xl border border-gray-200 dark:border-gray-800 shadow-lg !bg-white dark:!bg-charcoal-900"
    close-button-right
    @close="resetForm"
  >
    <template #default="{ on }">
      <p class="mb-4 text-sm leading-relaxed text-gray-600 dark:text-gray-400">
        {{ i18n.t("organization.settings.deleteModal.description", [organization]) }}
      </p>
      <div
        class="rounded-lg border border-transparent bg-gray-100 transition-colors duration-250 hover:border-gray-400 focus-within:border-primary-500 dark:bg-gray-800 dark:hover:border-gray-500 dark:focus-within:border-primary-500"
      >
        <textarea
          v-model.trim="comment"
          class="min-h-24 w-full resize-y bg-transparent px-3 py-2 outline-none"
          :placeholder="i18n.t('general.comment')"
          rows="3"
        />
      </div>
      <div class="mt-5 flex justify-end gap-2">
        <Button button-type="secondary" size="medium" :disabled="loading" @click="on.click">
          {{ i18n.t("general.close") }}
        </Button>
        <button
          class="inline-flex items-center justify-center rounded-md border border-red-600 bg-red-900/50 p-2 font-semibold text-white transition-all duration-250 disabled:cursor-wait disabled:opacity-60"
          type="button"
          :disabled="loading"
          @click="deleteOrg()"
        >
          {{ i18n.t("general.delete") }}
        </button>
      </div>
    </template>
    <template #activator="{ on }">
      <button
        class="inline-flex h-10.5 w-full items-center justify-center rounded-md border border-gray-800 px-3 font-semibold transition-all duration-250 hover:border-red-600 hover:bg-red-900/50"
        type="button"
        v-on="on"
      >
        <IconMdiDeleteAlert class="mr-1" />
        {{ i18n.t("general.delete") }}
      </button>
    </template>
  </Modal>
</template>
