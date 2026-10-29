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

function leave() {
  const url = props.organization ? `organizations/org/${props.author}/members/leave` : `projects/project/${props.slug}/members/leave`;
  useInternalApi(url, "post")
    .then(() => router.go(0))
    .catch((err) => handleRequestError(err));
}
</script>

<template>
  <Modal :title="i18n.t('form.memberList.leaveModal.title', [name])" window-classes="w-150">
    <template #default>
      <p>{{ i18n.t("form.memberList.leaveModal.description", [name]) }}</p>
    </template>
    <template #activator="{ on }">
      <Button variant="ghost" tone="danger" size="sm" v-on="on"> {{ i18n.t("form.memberList.leave") }} </Button>
    </template>
    <template #footer="{ on }">
      <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
      <Button tone="danger" @click="leave()"> {{ i18n.t("form.memberList.leave") }} </Button>
    </template>
  </Modal>
</template>
