<template>
    <v-menu v-model="open" :close-on-click="false" offset-x :close-on-content-click="false">
        <template #activator="props">
            <slot name="activator" v-bind="props" />
        </template>
        <v-card max-width="300" color="info" light>
            <v-card-title>{{ $t(promptInfo.titleKey) }}</v-card-title>
            <v-card-text>{{ $t(promptInfo.messageKey) }}</v-card-text>
            <v-card-actions>
                <v-btn small :loading="loading" @click.stop="markAsRead">
                    <v-icon color="success" left> mdi-check-circle </v-icon>
                    {{ $t('prompts.confirm') }}
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-menu>
</template>

<script lang="ts">
import { Component, Prop } from 'nuxt-property-decorator';
import { IPrompt } from 'hangar-internal';
import { PropType } from 'vue';
import { RootState } from '~/store';
import { Prompt as PromptEnum } from '~/types/enums';
import { HangarComponent } from '~/components/mixins';

@Component
export default class Prompt extends HangarComponent {
    open = false;
    loading = false;

    @Prop({ type: String as PropType<PromptEnum>, required: true })
    prompt!: PromptEnum;

    created() {
        this.open = !this.currentUser!.readPrompts.includes(this.promptInfo.ordinal);
    }

    get promptInfo(): IPrompt {
        return (this.$store.state as RootState).prompts.get(this.prompt)!;
    }

    markAsRead() {
        this.loading = true;
        this.$api
            .requestInternal(`read-prompt/${this.promptInfo.ordinal}`, true, 'post')
            .then(() => {
                this.open = false;
                this.$auth.refreshUser();
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading = false;
            });
    }
}
</script>

<style lang="scss" scoped></style>
