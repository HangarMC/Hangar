<template>
    <div>
        {{ testArr.join(', ') }}
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarComponent } from '~/components/mixins';

@Component
export default class Testing extends HangarComponent {
    testArr!: number[];

    created() {
        console.log('created');
    }

    async asyncData() {
        console.log('async start');
        await new Promise<void>((resolve) => {
            setTimeout(() => {
                resolve();
            }, 300);
        });
        console.log('async end');
        return Promise.resolve({ testArr: [5, 4, 3, 2, 1], t: 't' });
    }
}
</script>

<style lang="scss" scoped></style>
