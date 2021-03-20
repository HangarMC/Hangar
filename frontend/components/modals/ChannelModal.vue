<template>
    <v-dialog v-model="dialog" max-width="500" persistent @input="reset">
        <template #activator="{ on, attrs }">
            <slot name="activator" :on="on" :attrs="attrs" />
        </template>
        <v-card>
            <v-card-title>{{ edit ? $t('channel.modal.titleEdit') : $t('channel.modal.titleNew') }}</v-card-title>
            <v-card-text>
                <v-form ref="modalForm" v-model="validForm">
                    <v-text-field
                        v-model.trim="form.name"
                        :label="$t('channel.modal.name')"
                        :rules="[
                            $util.$vc.require($t('channel.modal.name')),
                            $util.$vc.regex($t('channel.modal.name'), validations.project.channels.regex),
                            $util.$vc.maxLength(validations.project.channels.max),
                        ]"
                    />
                    <v-card-subtitle class="pa-0 text-center">{{ $t('channel.modal.color') }}</v-card-subtitle>
                    <v-item-group v-model="form.color" mandatory>
                        <v-container>
                            <v-row v-for="(arr, arrIndex) in swatches" :key="arrIndex" justify="center">
                                <v-col v-for="(color, n) in arr" :key="n" class="flex-grow-0 flex-shrink-1 pa-2 px-1">
                                    <v-item v-slot="{ active, toggle }" :value="color">
                                        <v-card
                                            :color="color"
                                            class="d-flex align-center"
                                            :dark="$util.isDark(color)"
                                            :light="$util.isLight(color)"
                                            height="20"
                                            width="40"
                                            @click="toggle"
                                        >
                                            <v-fade-transition>
                                                <v-icon v-show="active" small class="ma-auto"> mdi-checkbox-marked-circle</v-icon>
                                            </v-fade-transition>
                                        </v-card>
                                    </v-item>
                                </v-col>
                            </v-row>
                        </v-container>
                    </v-item-group>
                    <v-checkbox v-model="form.nonReviewed" :label="$t('channel.modal.reviewQueue')" />
                </v-form>
            </v-card-text>
            <v-card-actions class="justify-end">
                <v-btn color="error" text @click="dialog = false">{{ $t('general.close') }}</v-btn>
                <v-btn color="success" :disabled="!isValid" @click="create">{{ edit ? $t('general.save') : $t('general.create') }}</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, Emit, Prop, State } from 'nuxt-property-decorator';
import { Color, ProjectChannel } from 'hangar-internal';
import { HangarFormModal } from '../mixins';
import { RootState } from '~/store';

@Component
export default class ChannelModal extends HangarFormModal {
    @Prop({ default: false })
    edit!: Boolean;

    @Prop()
    channel!: ProjectChannel | undefined;

    colors: Color[] = [];
    form: ProjectChannel = {
        name: '',
        color: '',
        nonReviewed: false,
        versionCount: 0,
    };

    mounted() {
        this.reset();
    }

    reset() {
        if (this.channel) {
            this.form = { ...this.channel };
        }
    }

    async fetch() {
        this.colors = await this.$api.requestInternal<Color[]>('data/channelColors', false).catch<any>(this.$util.handleRequestError);
    }

    get swatches(): string[][] {
        const swatches: string[][] = [];
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

    @Emit()
    create() {
        // TODO check channel name against existing channels
        this.dialog = false;
        return this.form;
    }

    @State((state: RootState) => state.validations)
    validations!: RootState['validations'];
}
</script>

<style lang="scss" scoped></style>
