<script setup lang="ts">
import Header from "~/components/layout/Header.vue";
import Footer from "~/components/layout/Footer.vue";
import Container from "~/components/design/Container.vue";
import Notifications from "~/components/Notifications.vue";
</script>

<template>
  <main>
    <Header />
    <Container class="min-h-[80vh]">
      <Suspense>
        <router-view v-slot="{ Component }" v-bind="$attrs">
          <transition name="slide">
            <!-- dummy diff to make the transition work on pages where template root has multiple elements -->
            <div id="#page">
              <component :is="Component" />
            </div>
          </transition>
        </router-view>
      </Suspense>
    </Container>
    <Notifications />
    <Footer />
  </main>
</template>
