<template>
  <footer class="relative flex items-end mt-10 background-default px-8 pb-2 text-light-10 min-h-10">
    <div class="footerContent w-screen">
      <div class="footerBar flex items-center justify-around mt-4 max-w-1200px m-auto text-sm">
        <p>© 2022 <a href="https://papermc.io/">PaperMC</a></p>
        <div class="footerBarLinks flex flex-col md:flex-row">
          <router-link :to="{ name: 'guidelines' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0"
            >Resource Guidelines
          </router-link>
          <router-link :to="{ name: 'api' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0">API </router-link>
          <a href="https://github.com/HangarMC" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0">GitHub Organization </a>
          <router-link :to="{ name: 'tos' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0"
            >Terms of Service
          </router-link>
        </div>
      </div>
    </div>
  </footer>
</template>
