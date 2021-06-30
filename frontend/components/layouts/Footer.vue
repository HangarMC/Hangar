<template>
    <v-footer dark padless>
        <v-container>
            <v-row align="center" justify="center">
                <v-col cols="12" class="d-flex justify-center">
                    By using this site you're accepting our <a href="https://papermc.io/community-guidelines"> Terms of Service</a>
                </v-col>
            </v-row>

            <v-row align="center" justify="center">
                <v-col cols="12" sm="2" class="d-flex justify-center">
                    <div>
                        <h5 class="d-flex justify-center pb-2">Hangar API</h5>
                        <v-btn color="primary" to="/api"> API </v-btn>
                    </div>
                </v-col>

                <v-col cols="12" sm="2" class="d-flex justify-center">
                    <div>
                        <h5 class="d-flex justify-center pb-2">Help Needed?</h5>
                        <v-btn color="primary" href="https://papermc.io/forums"> Forum </v-btn>
                    </div>
                </v-col>

                <v-col cols="12" sm="4" class="d-flex justify-center">
                    <a href="https://papermc.io">
                        <img src="/icon.png" alt="Paper Logo" />
                    </a>
                </v-col>

                <v-col cols="12" sm="2" class="d-flex justify-center">
                    <div>
                        <h5 class="d-flex justify-center pb-2">Found an Issue?</h5>
                        <v-btn color="primary" href="https://github.com/PaperMC/Hangar/issues" target="_blank" rel="noopener"> Issues </v-btn>
                    </div>
                </v-col>

                <v-col cols="12" sm="2" class="d-flex justify-center">
                    <div>
                        <h5 class="d-flex justify-center pb-2">Our Sponsors</h5>
                        <v-btn color="primary" href="https://papermc.io/sponsors"> Sponsors </v-btn>
                    </div>
                </v-col>
            </v-row>

            <v-row align="center" justify="center">
                <v-col cols="12">
                    <LanguageModal />
                </v-col>
                <v-col cols="12">
                    <v-switch v-model="$vuetify.theme.dark">Theme</v-switch>
                </v-col>
                <v-col cols="12" class="d-flex justify-center">
                    Copyright © <a href="https://papermc.io">PaperMC</a> 2016 - {{ new Date().getFullYear() }}
                </v-col>
            </v-row>
        </v-container>
    </v-footer>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import LanguageModal from '~/components/modals/LanguageModal.vue';
@Component({
    components: { LanguageModal },
})
export default class Footer extends Vue {}
</script>

<style lang="scss" scoped>
img {
    height: 60px;
    width: 60px;

    @media (min-width: 768px) {
        height: 74px;
        width: 74px;

        &:hover {
            animation-name: bounce;
            animation-duration: 0.5s;
            animation-fill-mode: both;
            animation-iteration-count: infinite;
        }

        @keyframes bounce {
            0%,
            100% {
                transform: rotateZ(0);
            }
            33% {
                transform: rotateZ(-5deg);
            }
            50% {
                transform: rotateZ(0);
            }
            76% {
                transform: rotateZ(5deg);
            }
        }
    }
}

a {
    margin: 0 3px;
}
</style>
