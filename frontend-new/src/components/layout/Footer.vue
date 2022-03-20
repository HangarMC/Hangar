<template>
  <footer class="relative flex items-end mt-10 bg-gradient-to-r from-[#004ee9] to-[#367aff] px-8 pt-15 pb-2 text-background-light-10 min-h-70">
    <div class="footerContent w-screen">
      <div class="flex justify-center flex-col justify-center gap-y-6 max-w-1200px m-auto" md="gap-x-6 gap-y-0 flex-row">
        <div md="w-1/3 min-w-1/3 max-w-1/3">
          <p class="text-l font-bold text-center mb-2">About Hangar</p>
          <p class="text-sm">
            Hangar is an NFT plugin repository, hosted in the blockchain. Every plugin is unique and owned just by you. Can you use it on your server? No! You
            only own the receipt.
          </p>
        </div>
        <div md="w-1/3 min-w-1/3 max-w-1/3">
          <p class="text-l font-bold text-center mb-2">Links</p>
          <div class="flex flex-col items-center gap-y-3">
            <router-link
              :to="{ name: 'staff' }"
              class="flex items-center justify-center rounded-md px-4 py-2 background-body w-fit text-sm"
              light="text-black"
              hover="text-primary-100"
            >
              Open Crypto Wallet
            </router-link>
            <router-link
              :to="{ name: 'staff' }"
              class="flex items-center justify-center rounded-md px-4 py-2 background-body w-fit text-sm"
              light="text-black"
              hover="text-primary-100"
            >
              Exchange Plugin NFTs
            </router-link>
          </div>
        </div>
        <div md="w-1/3 min-w-1/3 max-w-1/3">
          <p class="text-l font-bold text-center mb-2">More from Paper</p>
        </div>
      </div>
      <div class="footerBar flex items-center justify-around mt-4 max-w-1200px m-auto text-sm">
        <p>Copyright © <a href="https://papermc.io/">PaperMC</a> 2016 - 2022</p>
        <div class="footerBarLinks flex">
          <router-link :to="{ name: 'staff' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-100 bg-primary-50">About Hangar </router-link>
          <router-link :to="{ name: 'staff' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-100 bg-primary-50">Imprint </router-link>
          <router-link :to="{ name: 'staff' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-100 bg-primary-50"
            >Privacy Policy
          </router-link>
        </div>
      </div>
    </div>
    <div class="footerShape absolute z-1 left-0 right-0 top-0 overflow-hidden pointer-events-none" style="transform: scaleY(-1) scaleX(-1)">
      <svg
        class="fill-background-light-10 h-240px min-w-full"
        dark="fill-background-dark-80"
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 1200 218"
        preserveAspectRatio="none"
      >
        <path d="M0 218h1200v-61.3l-40 0,961 V 0,480 C 268.5,456.5 537,433 857,433 C 1177,433 1548.5,456.5 1920,480 C 1920,480 1920,961 1920,961 Z"></path>
      </svg>
    </div>
  </footer>
</template>
