<script lang="ts" setup>
import { NamedPermission } from "#shared/types/backend";
import type { User } from "#shared/types/backend";

const props = defineProps<{
  user: User;
}>();

const i18n = useI18n();
const router = useRouter();
const v = useVuelidate();

const comment = ref<string>("");
const toggleProjectDeletion = ref<boolean>(false);

async function confirm(close: () => void) {
  try {
    await useInternalApi(`admin/lock-user/${props.user.name}?locked=${!props.user.locked}&toggleProjectDeletion=${toggleProjectDeletion.value}`, "post", {
      content: comment.value,
    });
    close();
    router.go(0);
    useNotificationStore().success(
      i18n.t(`author.lock.success${props.user.locked ? "Unlock" : "Lock"}${props.user.isOrganization ? "Org" : ""}`, [props.user.name])
    );
  } catch (err) {
    handleRequestError(err);
  }
}
</script>

<template>
  <Modal :title="i18n.t(`author.lock.confirm${user.locked ? 'Unlock' : 'Lock'}${user.isOrganization ? 'Org' : ''}`, [user.name])">
    <template #default>
      <InputTextarea v-model="comment" :rules="[required()]" :label="i18n.t(`author.lock.reason${user.locked ? 'Unlock' : 'Lock'}`)" />
      <InputCheckbox
        v-if="hasPerms(NamedPermission.DeleteProject)"
        v-model="toggleProjectDeletion"
        :label="i18n.t(`author.lock.${user.locked ? 'reinstateProjects' : 'deleteProjects'}${user.isOrganization ? 'Org' : ''}`)"
      />
    </template>
    <template #activator="{ on }">
      <Tooltip>
        <template #content>
          {{ i18n.t(`author.tooltips.${user.locked ? "unlock" : "lock"}${user.isOrganization ? "Org" : ""}`) }}
        </template>
        <Button variant="outline" tone="neutral" size="sm" v-on="on">
          <IconMdiLockOpenOutline v-if="user.locked" />
          <IconMdiLockOutline v-else />
          {{ i18n.t(`author.tooltips.${user.locked ? "unlock" : "lock"}${user.isOrganization ? "Org" : ""}`) }}
        </Button>
      </Tooltip>
    </template>
    <template #footer="{ on }">
      <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
      <Button :disabled="v.$invalid" @click="confirm(on.click)">{{ i18n.t("general.confirm") }}</Button>
    </template>
  </Modal>
</template>
