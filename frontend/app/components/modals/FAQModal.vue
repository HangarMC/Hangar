<script lang="ts" setup>
import { onClickOutside } from '@vueuse/core';

const i18n = useI18n();

const showModal = ref(false);
const modalContainer = ref<HTMLElement>();

onClickOutside(modalContainer, () => {
  closeModal();
});

function openModal() {
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

</script>

<template>
  <div>
    <!-- FAQ Button -->
    <Button
      button-type="primary"
      size="large"
      @click="openModal"
    >
      <span>{{ i18n.t('hangar.faq.title') }}</span>
    </Button>

    <!-- Modal Overlay -->
    <Transition name="fade">
      <div
        v-if="showModal"
        class="fixed inset-0 z-50 flex items-center justify-center bg-[#000000] bg-opacity-70"
        @click.self="closeModal"
      >
        <div
          ref="modalContainer"
          class="w-full max-w-2xl max-h-[600px] mx-4 bg-white dark:bg-gray-900 rounded-xl shadow-lg border border-gray-200 dark:border-gray-800 overflow-hidden flex flex-col"
        >
          <!-- Header -->
          <div class="relative dark:border-gray-700 flex items-center justify-between bg-charcoal-900 px-6 py-4 border-b border-gray-700">
            <h2 class="text-xl font-bold">{{ i18n.t('hangar.faq.title') }}</h2>
            <Button
              button-type="transparent"
              class="py-2"
              @click="closeModal"
            >
              <IconMdiClose class="text-gray-500 hover:text-white" />
            </Button>
          </div>

          <!-- FAQ Content -->
          <div class="flex-1 overflow-y-auto bg-charcoal-900 px-6 py-4">
            <div class="space-y-4">
              <div
                class="p-4 rounded-lg bg-gray-800 border border-gray-700"
              >
                <h3 class="font-semibold text-lg mb-2">What is Hangar?</h3>
                <p class="text-gray-400">
                  Hangar is the best place to download plugins. Created by the <Link href="https://papermc.io/team">PaperMC Team</Link>, we took
                  great care that you can find the newest and best plugins.
                </p>
              </div>

              <div
                class="p-4 rounded-lg bg-gray-800 border border-gray-700"
              >
                <h3 class="font-semibold text-lg mb-2">How do I download plugins from Hangar?</h3>
                <p class="text-gray-400">
                  To download plugins, simply use the search on this page to find the plugin you are looking for and download the plugin from the resource page.
                  The main download button will always provide the latest release version.
                </p>
              </div>

              <div
                class="p-4 rounded-lg bg-gray-800 border border-gray-700"
              >
                <h3 class="font-semibold text-lg mb-2">Can I automate uploading plugins to Hangar?</h3>
                <p class="text-gray-400">
                  Yes! Simply use the <Link href="https://github.com/HangarMC/hangar-publish-plugin">Hangar publish plugin for Gradle</Link>.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
