<template>
    <div :id="id" class="ct-chart"></div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { IChartistBase, IChartistData, IChartOptions } from 'chartist';
import * as Chartist from 'chartist';
import { Prop } from 'vue-property-decorator';
import { PropType } from 'vue';

require('chartist-plugin-legend');

@Component
export default class Chart extends Vue {
    @Prop({ required: true })
    id!: string;

    @Prop({ required: true })
    barType!: PropType<'pie' | 'bar' | 'line' | 'candle'>;

    @Prop({ required: true })
    data!: IChartistData;

    @Prop()
    options!: IChartOptions;

    chart!: IChartistBase<IChartOptions>;

    mounted() {
        const type = (this.barType as unknown) as string;
        if (type === 'pie') {
            this.chart = new Chartist.Pie('#' + this.id, this.data, this.options);
        } else if (type === 'bar') {
            this.chart = new Chartist.Bar('#' + this.id, this.data, this.options);
        } else if (type === 'line') {
            this.chart = new Chartist.Line('#' + this.id, this.data, this.options);
        } else if (type === 'candle') {
            this.chart = new Chartist.Candle('#' + this.id, this.data, this.options);
        } else {
            console.log('unknown bar type ', type);
        }
    }
}
</script>

<style lang="scss">
@import 'node_modules/chartist/dist/scss/chartist.scss';

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
