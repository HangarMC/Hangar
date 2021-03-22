<template>
    <v-card>
        <v-card-title>
            {{ $t('stats.title') }}
        </v-card-title>
        <v-card-text>
            <v-menu ref="menu" v-model="dateMenu" :close-on-content-click="false" transition="scale-transition" offset-y min-width="auto">
                <template #activator="{ on, attrs }">
                    <v-text-field v-model="dateRangeText" prepend-icon="mdi-calendar" readonly v-bind="attrs" v-on="on"></v-text-field>
                </template>
                <v-date-picker ref="picker" v-model="dates" range></v-date-picker>
            </v-menu>
            <h2>{{ $t('stats.plugins') }}</h2>
            <Chart id="stats" :data="pluginData" :options="options" bar-type="line"></Chart>
            <h2>{{ $t('stats.downloads') }}</h2>
            <Chart id="downloads" :data="downloadData" :options="options" bar-type="line"></Chart>
            <h2>{{ $t('stats.flags') }}</h2>
            <Chart id="flags" :data="flagData" :options="options" bar-type="line"></Chart>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import * as Chartist from 'chartist';
import { IChartistData, ILineChartOptions } from 'chartist';
import Chart from '~/components/chart/Chart.vue';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

// TODO implement AdminStatsPage
@Component({
    components: { Chart },
})
@GlobalPermission(NamedPermission.VIEW_STATS)
export default class AdminStatsPage extends Vue {
    dates: Array<Date> | null;
    dateMenu = false;

    constructor() {
        super();
        this.dates = null;
        // TODO init properly
        // this.dates = [];
        // this.dates.push(new Date());
        // this.dates.push(new Date());
        // this.dates[0].setDate(this.dates[0].getMonth() - 1);
    }

    get dateRangeText() {
        return this.dates && this.dates.length > 1 ? this.$util.prettyDate(this.dates[0]) + ' ~ ' + this.$util.prettyDate(this.dates[1]) : null;
    }

    options: ILineChartOptions = {
        axisX: {
            type: Chartist.FixedScaleAxis,
            divisor: 5,
            labelInterpolationFnc: (value: string | Date) => {
                return this.$util.prettyDate(value);
            },
        },
        plugins: [Chartist.plugins.legend()],
    };

    pluginData: IChartistData = {
        series: [
            {
                name: this.$t('stats.reviews') as string,
                data: [
                    { x: new Date(143134652600), y: 53 },
                    { x: new Date(143234652600), y: 40 },
                    { x: new Date(143340052600), y: 45 },
                    { x: new Date(143366652600), y: 40 },
                    { x: new Date(143410652600), y: 20 },
                    { x: new Date(143508652600), y: 32 },
                    { x: new Date(143569652600), y: 18 },
                    { x: new Date(143579652600), y: 11 },
                ],
            },
            {
                name: this.$t('stats.uploads') as string,
                data: [
                    { x: new Date(143134652600), y: 53 },
                    { x: new Date(143234652600), y: 35 },
                    { x: new Date(143334652600), y: 30 },
                    { x: new Date(143384652600), y: 30 },
                    { x: new Date(143568652600), y: 10 },
                ],
            },
        ],
    };

    downloadData: IChartistData = {
        series: [
            {
                name: this.$t('stats.totalDownloads') as string,
                data: [
                    { x: new Date(143134652600), y: 53 },
                    { x: new Date(143234652600), y: 40 },
                    { x: new Date(143340052600), y: 45 },
                    { x: new Date(143366652600), y: 40 },
                    { x: new Date(143410652600), y: 20 },
                    { x: new Date(143508652600), y: 32 },
                    { x: new Date(143569652600), y: 18 },
                    { x: new Date(143579652600), y: 11 },
                ],
            },
            {
                name: this.$t('stats.unsafeDownloads') as string,
                data: [
                    { x: new Date(143134652600), y: 53 },
                    { x: new Date(143234652600), y: 35 },
                    { x: new Date(143334652600), y: 30 },
                    { x: new Date(143384652600), y: 30 },
                    { x: new Date(143568652600), y: 10 },
                ],
            },
        ],
    };

    flagData: IChartistData = {
        series: [
            {
                name: this.$t('stats.openedFlags') as string,
                data: [
                    { x: new Date(143134652600), y: 53 },
                    { x: new Date(143234652600), y: 40 },
                    { x: new Date(143340052600), y: 45 },
                    { x: new Date(143366652600), y: 40 },
                    { x: new Date(143410652600), y: 20 },
                    { x: new Date(143508652600), y: 32 },
                    { x: new Date(143569652600), y: 18 },
                    { x: new Date(143579652600), y: 11 },
                ],
            },
            {
                name: this.$t('stats.closedFlags') as string,
                data: [
                    { x: new Date(143134652600), y: 53 },
                    { x: new Date(143234652600), y: 35 },
                    { x: new Date(143334652600), y: 30 },
                    { x: new Date(143384652600), y: 30 },
                    { x: new Date(143568652600), y: 10 },
                ],
            },
        ],
    };
}
</script>

<style lang="scss" scoped></style>
