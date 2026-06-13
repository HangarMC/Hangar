<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    author: string;
    organization?: boolean;
    slug?: string;
  }>(),
  {
    organization: false,
    slug: undefined,
  }
);

const i18n = useI18n();
const router = useRouter();
const name = props.organization ? props.author : props.slug;
const loading = ref(false);

async function leave(close: () => void) {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const url = props.organization ? `organizations/org/${props.author}/members/leave` : `projects/project/${props.slug}/members/leave`;
  try {
    await useInternalApi(url, "post");
    close();
    router.go(0);
  } catch (err) {
    handleRequestError(err);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <Modal
    :title="i18n.t('form.memberList.leaveModal.title', [name])"
    window-classes="w-full max-w-xl !rounded-xl border border-gray-200 dark:border-gray-800 shadow-lg !bg-white dark:!bg-charcoal-900"
    close-button-right
  >
    <template #default="{ on }">
      <p class="text-sm leading-relaxed text-gray-600 dark:text-gray-400">
        {{ i18n.t("form.memberList.leaveModal.description", [name]) }}
      </p>
      <div class="mt-5 flex justify-end gap-2">
        <Button button-type="secondary" size="medium" :disabled="loading" @click="on.click">
          {{ i18n.t("general.close") }}
        </Button>
        <Button size="medium" button-type="red" :loading="loading" @click="leave(on.click)">
          {{ i18n.t("form.memberList.leave") }}
        </Button>
      </div>
    </template>
    <template #activator="{ on }">
      <button
        class="inline-flex h-8 items-center justify-center rounded-md border border-gray-200 px-2 text-sm font-semibold transition-all duration-250 hover:border-red-600 hover:bg-red-900/50 dark:border-gray-700"
        type="button"
        v-on="on"
      >
        {{ i18n.t("form.memberList.leave") }}
      </button>
    </template>
  </Modal>
</template>
