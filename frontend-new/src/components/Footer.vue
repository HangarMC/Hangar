<script setup lang="ts"></script>

<template>
    <footer class="relative flex items-end mt-10 bg-gradient-to-r from-[#004ee9] to-[#367aff] px-8 pt-12 pb-2 text-[#f8faff] min-h-70">
        <div class="footerContent w-screen">
            <div class="flex justify-center">
                Footer

            </div>
            <div class="footerBar flex items-center	justify-around mt-4">
                <p>Copyright © <a href="https://papermc.io/">PaperMC</a> 2016 - 2022</p>
                <div class="footerBarLinks flex">
                    <router-link
                        :to="{ name: 'staff' }"
                        class="flex items-center rounded-md px-6 py-2"
                        hover="text-primary100 bg-primary50"
                    >About Hangar
                    </router-link>
                    <router-link
                        :to="{ name: 'staff' }"
                        class="flex items-center rounded-md px-6 py-2"
                        hover="text-primary100 bg-primary50"
                    >Imprint
                    </router-link>
                    <router-link
                        :to="{ name: 'staff' }"
                        class="flex items-center rounded-md px-6 py-2"
                        hover="text-primary100 bg-primary50"
                    >Privacy Policy
                    </router-link>
                </div>
            </div>
        </div>
        <div class="footerShape absolute z-1 left-0 right-0 top-0 overflow-hidden" style="transform: scaleY(-1) scaleX(-1);">
         <!--- <svg class="fill-current" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 218" preserveAspectRatio="none"><path d="M0 218h1200v-31.3l-40 4.4c-40 4.8-120 13.1-200 0-80-13.6-160-48.6-240-66.7-80-17.8-160-17.8-240-8.8-80 8.6-160 26.9-240 8.8-80-17.7-160-71.1-200-97.7L0 0v218z"></path></svg>
     -->       <svg class="fill-backgroundLight10 dark:fill-backgroundDark80 h-180px min-w-full" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 218" preserveAspectRatio="none"><path d="M0 218h1200v-71.3l-40 0,961 V 0,480 C 268.5,456.5 537,433 857,433 C 1177,433 1548.5,456.5 1920,480 C 1920,480 1920,961 1920,961 Z"></path></svg>
        </div>
    </footer>
</template>
