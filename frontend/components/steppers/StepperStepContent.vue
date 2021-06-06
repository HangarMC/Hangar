<template>
    <v-stepper-content :step="step">
        <slot />
        <v-btn v-if="!hideButtons" :disabled="!allowContinue" color="primary" class="mt-3" @click="$emit('continue')">
            {{ $t('project.new.step' + step + '.continue') }}
        </v-btn>
        <v-btn v-if="!hideButtons" text color="warning" class="mt-3" @click="$emit('back')">
            {{ $t('project.new.step' + step + '.back') }}
        </v-btn>
    </v-stepper-content>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';

@Component
export default class StepperStepContent extends Vue {
    @Prop({ type: Number, required: true })
    step!: number;

    @Prop({ type: Boolean, default: false })
    hideButtons!: boolean;

    @Prop({ type: Boolean, default: true })
    allowContinue!: boolean;
}
</script>
