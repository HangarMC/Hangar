<template>
    <v-dialog v-model="dialog" max-width="700">
        <template #activator="{ attrs, on }">
            <v-btn v-bind="attrs" v-on="on">
                {{ $t('lang.button') }}
            </v-btn>
        </template>
        <v-card>
            <v-card-title>{{ $t('lang.title') }}</v-card-title>
            <v-card-text>
                <v-list dense>
                    <v-subheader>{{ $t('lang.available') }}</v-subheader>
                    <v-list-item-group v-model="lang" @change="changeLang">
                        <v-list-item v-for="locale in availableLocales" :key="locale.code" :value="locale.code">
                            <v-list-item-icon>
                                {{ locale.icon }}
                            </v-list-item-icon>
                            <v-list-item-content>
                                {{ locale.name }}
                            </v-list-item-content>
                        </v-list-item>
                    </v-list-item-group>
                </v-list>
                <v-alert>
                    <!-- todo idk how we wanna handle this, we need to load the lang from hangar auth, maybe a check box to also set on hangar auth -->
                    {{ $t('lang.hangarAuth') }}
                </v-alert>
            </v-card-text>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarFormModal } from '~/components/mixins';

@Component
export default class LanguageModal extends HangarFormModal {
    lang: string = 'en';

    mounted() {
        this.lang = this.$i18n.locale || this.$i18n.defaultLocale || 'en';
    }

    get availableLocales() {
        return this.$i18n.locales;
    }

    changeLang() {
        const shouldReload = this.$i18n.locale === 'dum' || this.lang === 'dum';
        console.log('setLocale ' + this.lang);
        this.$i18n.setLocale(this.lang);
        if (shouldReload) {
            location.reload();
        }
    }
}
</script>

<style lang="scss" scoped></style>
