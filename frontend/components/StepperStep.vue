<template>
    <div>
        <v-stepper-step :complete="currentStep > thisStep" :step="thisStep">
            <small v-if="optional">{{ $t('project.new.step' + thisStep + '.optional') }}</small>
            {{ $t('project.new.step' + thisStep + '.title') }}
        </v-stepper-step>
        <v-stepper-content :step="thisStep">
            <slot />
            <template v-if="buttons">
                <v-btn color="primary" @click="$emit('continue')"> {{ $t('project.new.step' + thisStep + '.continue') }} </v-btn>
                <v-btn text @click="$emit('back')"> {{ $t('project.new.step' + thisStep + '.back') }} </v-btn>
            </template>
        </v-stepper-content>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Prop } from 'vue-property-decorator';

@Component
export default class StepperStep extends Vue {
    @Prop()
    currentStep: Number;

    @Prop()
    thisStep: Number;

    @Prop({ default: false })
    optional: Boolean;

    @Prop({ default: true })
    buttons: Boolean;
}
</script>

<style lang="scss" scoped></style>
