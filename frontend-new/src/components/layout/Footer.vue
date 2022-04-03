<template>
  <footer class="relative flex items-end mt-10 bg-[#ecf2fb] dark:bg-[#212121] px-8 pb-2 text-light-10 min-h-10">
    <div class="footerContent w-screen">
      <div class="footerBar flex items-center justify-around mt-4 max-w-1200px m-auto text-sm">
        <p>Copyright © <a href="https://papermc.io/">PaperMC</a> 2016 - 2022</p>
        <div class="footerBarLinks flex">
          <router-link :to="{ name: 'staff' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0">About Hangar </router-link>
          <router-link :to="{ name: 'staff' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0">Imprint </router-link>
          <router-link :to="{ name: 'staff' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0"
            >Privacy Policy
          </router-link>
        </div>
      </div>
    </div>
  </footer>
</template>
