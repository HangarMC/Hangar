<template>
    <v-card>
        <v-card-title>
            {{ $t('stats.title') }}
        </v-card-title>
        <v-card-text>
            <v-menu ref="menu" v-model="dateMenu" :close-on-content-click="false" transition="scale-transition" offset-y min-width="auto">
                <template #activator="{ on, attrs }">
                    <v-text-field v-model="dateRangeText" prepend-icon="mdi-calendar" readonly v-bind="attrs" v-on="on" />
                </template>
                <v-date-picker ref="picker" v-model="dates" range header-color="info" color="info" />
            </v-menu>
            <h2>{{ $t('stats.plugins') }}</h2>
            <client-only>
                <Chart id="stats" :data="pluginData" :options="options" bar-type="Line" />
            </client-only>
            <h2>{{ $t('stats.downloads') }}</h2>
            <client-only>
                <Chart id="downloads" :data="downloadData" :options="options" bar-type="Line" />
            </client-only>

            <h2>{{ $t('stats.flags') }}</h2>
            <client-only>
                <Chart id="flags" :data="flagData" :options="options" bar-type="Line" />
            </client-only>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'nuxt-property-decorator';
import * as Chartist from 'chartist';
import { IChartistData, IChartistSeriesData, ILineChartOptions } from 'chartist';
import { Context } from '@nuxt/types';
import Chart from '~/components/chart/Chart.vue';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

interface DayStat {
    x: Date;
    y: number;
}

interface DayStats {
    day: string;
    flagsClosed: number;
    flagsOpened: number;
    reviews: number;
    totalDownloads: number;
    unsafeDownloads: number;
    uploads: number;
}

@Component({
    components: { Chart },
})
@GlobalPermission(NamedPermission.VIEW_STATS)
export default class AdminStatsPage extends Vue {
    // dates!: string[];
    dates: string[] = [];
    dateMenu = false;

    head() {
        return this.$seo.head(this.$t('stats.title'), null, this.$route, null);
    }

    get dateRangeText() {
        return this.dates && this.dates.length > 1
            ? this.$util.prettyDate(this.$util.fromISOString(this.dates[0])) + ' ~ ' + this.$util.prettyDate(this.$util.fromISOString(this.dates[1]))
            : null;
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

    pluginData!: IChartistData;
    downloadData!: IChartistData;
    flagData!: IChartistData;

    @Watch('dates')
    async onDatesChange(val: string[]) {
        if (val.length !== 2) {
            return;
        }
        const data: DayStats[] = await this.$api
            .requestInternal<DayStats[]>('admin/stats', true, 'get', {
                from: this.dates[0],
                to: this.dates[1],
            })
            .catch<any>(this.$util.handlePageRequestError);
        if (!data) {
            return;
        }
        const reviews: DayStat[] = [];
        const uploads: DayStat[] = [];
        const totalDownloads: DayStat[] = [];
        const unsafeDownloads: DayStat[] = [];
        const openedFlags: DayStat[] = [];
        const closedFlags: DayStat[] = [];
        data.forEach((statDay) => {
            const day = this.$util.fromISOString(statDay.day);
            reviews.push({ x: day, y: statDay.reviews });
            uploads.push({ x: day, y: statDay.uploads });
            totalDownloads.push({ x: day, y: statDay.totalDownloads });
            unsafeDownloads.push({ x: day, y: statDay.unsafeDownloads });
            openedFlags.push({ x: day, y: statDay.flagsOpened });
            closedFlags.push({ x: day, y: statDay.flagsClosed });
        });
        (this.pluginData.series[0] as IChartistSeriesData).data = reviews;
        (this.pluginData.series[1] as IChartistSeriesData).data = uploads;
        (this.downloadData.series[0] as IChartistSeriesData).data = totalDownloads;
        (this.downloadData.series[1] as IChartistSeriesData).data = unsafeDownloads;
        (this.flagData.series[0] as IChartistSeriesData).data = openedFlags;
        (this.flagData.series[1] as IChartistSeriesData).data = closedFlags;
    }

    // TODO figure out a way of not having a ton of duplicate code here
    async asyncData({ $util, $api, app: { i18n } }: Context) {
        const now = new Date();
        const oneMonthBefore = new Date(now.getFullYear(), now.getMonth() - 1, now.getDate());
        const dates = [$util.toISODateString(oneMonthBefore), $util.toISODateString(now)];
        const data: DayStats[] = await $api
            .requestInternal<DayStats[]>('admin/stats', true, 'get', {
                from: dates[0],
                to: dates[1],
            })
            .catch<any>($util.handlePageRequestError);
        if (!data) {
            return;
        }
        const reviews: DayStat[] = [];
        const uploads: DayStat[] = [];
        const totalDownloads: DayStat[] = [];
        const unsafeDownloads: DayStat[] = [];
        const openedFlags: DayStat[] = [];
        const closedFlags: DayStat[] = [];
        data.forEach((statDay) => {
            const day = $util.fromISOString(statDay.day);
            reviews.push({ x: day, y: statDay.reviews });
            uploads.push({ x: day, y: statDay.uploads });
            totalDownloads.push({ x: day, y: statDay.totalDownloads });
            unsafeDownloads.push({ x: day, y: statDay.unsafeDownloads });
            openedFlags.push({ x: day, y: statDay.flagsOpened });
            closedFlags.push({ x: day, y: statDay.flagsClosed });
        });

        const pluginData = {
            series: [
                {
                    name: i18n.t('stats.reviews') as string,
                    data: reviews,
                },
                {
                    name: i18n.t('stats.uploads') as string,
                    data: uploads,
                },
            ],
        };

        const downloadData = {
            series: [
                {
                    name: i18n.t('stats.totalDownloads') as string,
                    data: totalDownloads,
                },
                {
                    name: i18n.t('stats.unsafeDownloads') as string,
                    data: unsafeDownloads,
                },
            ],
        };

        const flagData = {
            series: [
                {
                    name: i18n.t('stats.openedFlags') as string,
                    data: openedFlags,
                },
                {
                    name: i18n.t('stats.closedFlags') as string,
                    data: closedFlags,
                },
            ],
        };
        return { pluginData, downloadData, flagData, dates };
    }
}
</script>

<style lang="scss" scoped></style>
