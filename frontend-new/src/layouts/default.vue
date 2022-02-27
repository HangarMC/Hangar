<template>
    <main style="min-height: 60vh;">
        <Header/>
        <div style="min-height: 60vh;">
            <router-view v-bind="$attrs"/>
        </div>
        <Footer/>
    </main>
</template>
