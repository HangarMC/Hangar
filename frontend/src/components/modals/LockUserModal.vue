<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { AxiosError } from "axios";
import { User } from "hangar-api";
import { ref } from "vue";
import { useRouter } from "vue-router";
import Button from "~/lib/components/design/Button.vue";
import Modal from "~/lib/components/modals/Modal.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import Tooltip from "~/lib/components/design/Tooltip.vue";
import InputTextarea from "~/lib/components/ui/InputTextarea.vue";
import { useNotificationStore } from "~/lib/store/notification";

const props = defineProps<{
  user: User;
}>();

const i18n = useI18n();
const router = useRouter();

const comment = ref<string>("");

async function confirm(close: () => void) {
  try {
    await useInternalApi(`admin/lock-user/${props.user.name}/${!props.user.locked}`, "post", {
      content: comment.value,
    });
    close();
    router.go(0);
    useNotificationStore().success(i18n.t(`author.lock.success${props.user.locked ? "Unlock" : "Lock"}`, [props.user.name]));
  } catch (e) {
    handleRequestError(e as AxiosError);
  }
}
</script>

<template>
  <Modal :title="i18n.t(`author.lock.confirm${user.locked ? 'Unlock' : 'Lock'}`, [user.name])">
    <template #default="{ on }">
      <InputTextarea v-model="comment" />

      <Button button-type="primary" class="mt-3" @click="confirm(on.click)">{{ i18n.t("general.confirm") }}</Button>
    </template>
    <template #activator="{ on }">
      <Tooltip>
        <template #content>
          {{ i18n.t(`author.tooltips.${user.locked ? "unlock" : "lock"}`) }}
        </template>
        <Button size="small" class="mr-1 inline-flex" v-on="on">
          <IconMdiLockOpenOutline v-if="user.locked" />
          <IconMdiLockOutline v-else />
        </Button>
      </Tooltip>
    </template>
  </Modal>
</template>
