<template>
    <v-dialog v-model="dialog" max-width="500" persistent>
        <template #activator="{ on, attrs }">
            <slot name="activator" :on="on" :attrs="attrs" />
        </template>
        <v-card>
            <v-card-title>{{ $t('channel.new.title') }}</v-card-title>
            <v-card-text>
                <v-form v-model="validForm">
                    <v-text-field v-model.trim="form.name" :label="$t('channel.new.name')" :rules="[$util.$vc.require($t('channel.new.name'))]" />
                    <v-card-subtitle class="pa-0 text-center">{{ $t('channel.new.color') }}</v-card-subtitle>
                    <v-item-group v-model="form.color" mandatory>
                        <v-container>
                            <v-row v-for="(arr, arrIndex) in swatches" :key="arrIndex" justify="center">
                                <v-col v-for="(color, n) in arr" :key="n" class="flex-grow-0 flex-shrink-1 pa-2 px-1">
                                    <v-item v-slot="{ active, toggle }" :value="color">
                                        <v-card
                                            :color="color"
                                            class="d-flex align-center"
                                            :dark="isDark(color)"
                                            :light="isLight(color)"
                                            height="20"
                                            width="40"
                                            @click="toggle"
                                        >
                                            <v-fade-transition>
                                                <v-icon v-show="active" small class="ma-auto"> mdi-checkbox-marked-circle </v-icon>
                                            </v-fade-transition>
                                        </v-card>
                                    </v-item>
                                </v-col>
                            </v-row>
                        </v-container>
                    </v-item-group>
                    <v-checkbox v-model="form.nonReviewed" :label="$t('channel.new.reviewQueue')" />
                </v-form>
            </v-card-text>
            <v-card-actions class="justify-end">
                <v-btn color="error" text @click="dialog = false">{{ $t('general.close') }}</v-btn>
                <v-btn color="success" :disabled="!isValid" @click="createChannel">{{ $t('general.create') }}</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Color, ProjectChannel } from 'hangar-internal';
import { contrastRatio, HexToRGBA, parseHex } from 'vuetify/es5/util/colorUtils';
import { HangarFormModal } from '../mixins';

@Component
export default class NewChannelModal extends HangarFormModal {
    colors: Color[] = [];
    form: ProjectChannel = {
        name: null as null | string,
        color: null as null | string,
        nonReviewed: false,
        temp: true,
    };

    async fetch() {
        this.colors = await this.$api.requestInternal<Color[]>('data/channelColors', false).catch<any>(this.$util.handleRequestError);
    }

    get swatches(): Color[Color[]] {
        const swatches = [];
        const columns = Math.floor(Math.sqrt(this.colors.length));
        const rows = Math.ceil(Math.sqrt(this.colors.length));
        for (let i = 0; i < rows; i++) {
            swatches[i] = [];
            for (let j = 0; j < columns; j++) {
                if (this.colors[i * columns + j]) {
                    swatches[i][j] = this.colors[i * columns + j].hex;
                }
            }
        }
        return swatches;
    }

    get isValid() {
        return this.validForm && !!this.form.color;
    }

    white = HexToRGBA(parseHex('#ffffff'));
    black = HexToRGBA(parseHex('#000000'));

    // TODO util, this is probably super useful elsewhere
    isDark(hex: string): boolean {
        const rgba = HexToRGBA(parseHex(hex));
        return contrastRatio(rgba, this.white) > 2;
    }

    isLight(hex: string): boolean {
        const rgba = HexToRGBA(parseHex(hex));
        return contrastRatio(rgba, this.black) > 2;
    }

    createChannel() {
        this.$emit('create', this.form);
        this.dialog = false;
    }
}
</script>

<style lang="scss" scoped></style>
