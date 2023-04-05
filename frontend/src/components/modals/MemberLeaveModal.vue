<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
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
const router = useRouter();
const name = props.organization ? props.author : props.slug;

function leave() {
  const url = props.organization ? `organizations/org/${props.author}/members/leave` : `projects/project/${props.author}/${props.slug}/members/leave`;
  useInternalApi(url, "post")
    .then(() => router.go(0))
    .catch((e) => handleRequestError(e));
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
