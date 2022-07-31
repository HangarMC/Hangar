<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/lib/components/design/Button.vue";
import Modal from "~/lib/components/modals/Modal.vue";
import { useContext } from "vite-ssr/vue";
import { useRouter } from "vue-router";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";

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
const ctx = useContext();
const router = useRouter();
const name = props.organization ? props.author : props.slug;

async function leave() {
  const url = props.organization ? `organizations/org/${props.author}/members/leave` : `projects/project/${props.author}/${props.slug}/members/leave`;
  useInternalApi(url, true, "post")
    .then(() => router.go(0))
    .catch((e) => handleRequestError(e, ctx, i18n));
}
</script>

<template>
  <Modal :title="i18n.t('form.memberList.leaveModal.title', [name])" window-classes="w-150">
    <template #default>
      <p>{{ i18n.t("form.memberList.leaveModal.description", [name]) }}</p>
      <Button class="mt-3" size="small" button-type="red" @click="leave()"> {{ i18n.t("form.memberList.leave") }} </Button>
    </template>
    <template #activator="{ on }">
      <Button class="text-base" size="small" button-type="red" v-on="on"> {{ i18n.t("form.memberList.leave") }} </Button>
    </template>
  </Modal>
</template>
