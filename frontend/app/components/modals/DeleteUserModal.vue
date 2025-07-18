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
    <template #default="{ on }">
      <InputTextarea v-model="comment" :rules="[required()]" label="Reason" />
      <Button button-type="red" class="mt-3" :disabled="v.$invalid" @click="confirm(on.click)">{{ i18n.t("general.confirm") }}</Button>
    </template>
    <template #activator="{ on }">
      <Tooltip>
        <template #content> Delete user </template>
        <Button button-type="red" size="small" class="mr-1 inline-flex" v-on="on">
          <IconMdiDelete />
        </Button>
      </Tooltip>
    </template>
  </Modal>
</template>
