<script setup lang="ts">
import { Menu, MenuButton, MenuItem, MenuItems } from "@headlessui/vue";

const { t } = useI18n();


</script>

<template>
  <div class="flex flex-col items-center pt-10">
      <h2 class="text-3xl font-bold uppercase">Search your favorite plugins</h2>
      <div class="flex flex-row mt-6 big-box-shadow">
          <input class="rounded-md  p-3 w-4/5 max-w-800px focus-visible:(border-white) text-black" type="text" placeholder="Search in 1 projects, proudly made by the community...">
          <div class="sort rounded-md">
              <Menu>
                  <MenuButton class="bg-white text-black rounded-md p-2">Sort by</MenuButton>
                  <transition
                      enter-active-class="transition duration-100 ease-out"
                      enter-from-class="transform scale-95 opacity-0"
                      enter-to-class="transform scale-100 opacity-100"
                      leave-active-class="transition duration-75 ease-out"
                      leave-from-class="transform scale-100 opacity-100"
                      leave-to-class="transform scale-95 opacity-0"
                  >
                      <MenuItems>
                          <MenuItem v-slot="{ active }">
                              <a :class='{ "bg-blue-500": active }' href="/account-settings">
                                  Account settings
                              </a>
                          </MenuItem>
                          <MenuItem v-slot="{ active }">
                              <a :class='{ "bg-blue-500": active }' href="/account-settings">
                                  Documentation
                              </a>
                          </MenuItem>
                          <MenuItem disabled>
                              <span class="opacity-75">Invite a friend (coming soon!)</span>
                          </MenuItem>
                      </MenuItems>
                  </transition>
              </Menu>
          </div>
      </div>

  </div>
  <div class="projectsAndSidebar p-4 mt-5 w-screen max-w-1200px flex justify-around m-auto flex-col md:flex-row gap-y-6 md:gap-y-0 md:gap-x-6">
      <div class="projects md:(w-2/3 min-w-2/3 max-w-2/3) min-h-800px bg-gray-200 rounded-md">

      </div>
      <div class="sidebar bg-gradient-to-r from-[#004ee9] to-[#367aff] rounded-md min-w-300px min-h-800px p-4 text-white">

          <div class="versions">
              <h3 class="font-bold">Minecraft versions</h3>
              <div>
                  <input id="1.18.1" type="checkbox" name="1.18.1">
                  <label for="1.18.1">1.18.1</label>
              </div>
          </div>
          <div class="categories">
              <h3 class="font-bold">Categories</h3>
          </div>
          <div class="platforms">
              <h3 class="font-bold">Platforms</h3>
          </div>
          <div class="licenses">
              <h3 class="font-bold">Licenses</h3>
          </div>
      </div>
  </div>
</template>

<route lang="yaml">
meta:
  layout: default
</route>

<style lang="css" scoped>

.big-box-shadow {
    box-shadow: 0 0 10px 0 #004ee99e;
}


</style>
