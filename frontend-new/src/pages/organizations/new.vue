<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import Card from "~/components/design/Card.vue";
import { useAuthStore } from "~/store/auth";
import { useBackendDataStore } from "~/store/backendData";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { computed, ref, watch } from "vue";
import InputText from "~/components/ui/InputText.vue";
import MemberList from "~/components/projects/MemberList.vue";
import Button from "~/components/design/Button.vue";
import Alert from "~/components/design/Alert.vue";
import { JoinableMember } from "hangar-internal";

const route = useRoute();
const router = useRouter();
const i18n = useI18n();
const ctx = useContext();
const backendData = useBackendDataStore();

const currentUser = useAuthStore().user;

const name = ref<string>("");
const members = ref<JoinableMember[]>([]);
const nameErrorMessages = ref<string[]>([]);

/*const canCreate = computed<boolean>(
  () =>
    this.validForm &&
    !this.validateLoading &&
    (this.$refs.memberList.isEdited || (this.$refs.memberList.editedMembers.length === 0 && this.$refs.memberList.editingMembers.length === 0))
);*/

useHead(useSeo(i18n.t("organization.new.title"), null, route, null));

watch(name, async (val) => {
  if (!val) return;
  nameErrorMessages.value = [];
  try {
    await useInternalApi("organizations/validate", false, "get", { name: val });
  } catch (e) {
    nameErrorMessages.value.push(i18n.t("organization.new.error.duplicateName"));
  }
});

async function create() {
  try {
    await useInternalApi("organizations/create", true, "post", {
      name: name.value,
      members: members.value,
    });
    await router.push("/" + name.value);
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
}
</script>

<template>
  <Card>
    <template #header>{{ i18n.t("organization.new.title") }}</template>
    <p>{{ i18n.t("organization.new.text") }}</p>

    <template v-if="currentUser.headerData.organizationCount < backendData.validations.maxOrgCount">
      <InputText v-model="name" class="mt-2" :label="i18n.t('organization.new.name')" :error-messages="nameErrorMessages" />

      <MemberList v-model="members" organization disable-saving class="mt-2"></MemberList>

      <Button class="mt-2" @click="create">
        <IconMdiCheck class="float-left" />
        {{ i18n.t("form.memberList.create") }}
      </Button>
    </template>

    <Alert v-else>
      {{ i18n.t("organization.new.error.tooManyOrgs", [backendData.validations.maxOrgCount]) }}
    </Alert>
  </Card>
</template>

<route lang="yaml">
meta:
  requireLoggedIn: true
</route>
