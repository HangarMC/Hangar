<script lang="ts" setup>
import type { User } from "#shared/types/backend";

const props = defineProps<{
  user: User;
}>();

const i18n = useI18n();
const v = useVuelidate();
const comment = ref<string>("");

async function confirm(close: () => void) {
  try {
    await useInternalApi(`admin/yeet/${props.user.name}`, "post", {
      content: comment.value,
    });
    close();
    useNotificationStore().success("Deleted user");
  } catch (err) {
    handleRequestError(err);
  }
}
</script>

<template>
  <Modal title="Delete user">
    <template #default>
      <InputTextarea v-model="comment" :rules="[required()]" label="Reason" />
    </template>
    <template #activator="{ on }">
      <Tooltip>
        <template #content> Delete user </template>
        <Button variant="outline" tone="danger" size="sm" v-on="on">
          <IconMdiDelete />
          {{ i18n.t("author.tooltips.deleteUser") }}
        </Button>
      </Tooltip>
    </template>
    <template #footer="{ on }">
      <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
      <Button tone="danger" :disabled="v.$invalid" @click="confirm(on.click)">{{ i18n.t("general.confirm") }}</Button>
    </template>
  </Modal>
</template>
