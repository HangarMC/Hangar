<template>
    <div :id="id" class="ct-chart" />
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'nuxt-property-decorator';
import * as Chartist from 'chartist';
import { IChartistBase, IChartistData, IChartOptions } from 'chartist';
import { PropType } from 'vue';

require('chartist-plugin-legend');

@Component
export default class Chart extends Vue {
    @Prop({ required: true })
    id!: string;

    @Prop({ required: true, type: String as PropType<'Pie' | 'Bar' | 'Line' | 'Candle'> })
    barType!: 'Pie' | 'Bar' | 'Line' | 'Candle';

    @Prop({ required: true })
    data!: IChartistData;

    @Prop()
    options!: IChartOptions;

    chart: IChartistBase<IChartOptions> | null = null;

    mounted() {
        this.draw();
    }

    draw() {
        if (this.chart) {
            this.chart.update(this.data, this.options);
        } else {
            this.chart = new Chartist[this.barType]('#' + this.id, this.data, this.options);
        }
    }

    @Watch('data', { deep: true })
    onDataChange() {
        this.draw();
    }
}
</script>

<style lang="scss">
@import '~chartist/dist/scss/chartist';

.ct-label {
    color: rgba(255, 255, 255, 0.7);
}

.ct-grid {
    stroke: rgba(255, 255, 255, 0.2);
}

// ct legend plugin stuff
.ct-legend {
    position: relative;
    z-index: 10;
    list-style: none;
    text-align: center;

    li {
        position: relative;
        padding-right: 10px;
        padding-left: 23px;
        margin-bottom: 3px;
        cursor: pointer;
        display: inline-block;
    }

    li:before {
        width: 12px;
        height: 12px;
        position: absolute;
        left: 0;
        content: '';
        border: 3px solid transparent;
        border-radius: 2px;
    }

    li.inactive:before {
        background: transparent;
    }

    &.ct-legend-inside {
        position: absolute;
        top: 0;
        right: 0;
    }

    @for $i from 0 to length($ct-series-colors) {
        .ct-series-#{$i}:before {
            background-color: nth($ct-series-colors, $i + 1);
            border-color: nth($ct-series-colors, $i + 1);
        }
    }
}
</style>
