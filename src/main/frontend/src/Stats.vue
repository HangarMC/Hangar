<template>
    <div class="row">
        <div class="col-12">
            <div class="form-inline mb-3">
                <div class="input-group mr-2">
                    <div class="input-group-prepend">
                        <label for="fromDate" class="input-group-text">From:</label>
                    </div>
                    <input v-model="form.from" id="fromDate" type="date" class="form-control" :max="maxDate.from" />
                </div>
                <div class="input-group mr-2">
                    <div class="input-group-prepend">
                        <label for="toDate" class="input-group-text">To:</label>
                    </div>
                    <input v-model="form.to" id="toDate" type="date" class="form-control" :max="maxDate.to" />
                </div>
                <button class="btn btn-primary" @click="setDates">Go</button>
            </div>
        </div>
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title float-left">Plugins</h4>
                    <div class="clearfix"></div>
                </div>
                <canvas id="chart-reviews" height="40vh" width="100%"></canvas>
            </div>
        </div>
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title float-left">Downloads</h4>
                    <div class="clearfix"></div>
                </div>
                <canvas id="chart-downloads" height="40vh" width="100%"></canvas>
            </div>
        </div>
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title float-left">Flags</h4>
                    <div class="clearfix"></div>
                </div>
                <canvas id="chart-flags" height="40vh" width="100%"></canvas>
            </div>
        </div>
    </div>
</template>

<script>
import Chart from 'chart.js';
import { go } from '@/utils';

export default {
    name: 'Stats',
    data() {
        return {
            form: {
                from: null,
                to: null,
            },
            maxDate: {
                from: window.MAX_DATE.FROM,
                to: window.MAX_DATE.TO,
            },
            timeFrame: window.DAYS,
        };
    },
    methods: {
        setDates() {
            let from = this.form.from;
            let to = this.form.to;

            if (!to) {
                to = new Date();
            } else {
                to = new Date(to);
            }
            this.removeTime(to);

            if (!from) {
                from = new Date();
                from.setDate(to.getDate() - 10);
            } else {
                from = new Date(from);
            }
            this.removeTime(from);

            const now = this.removeTime(new Date());
            if (to > now) {
                to.setDate(now.getDate());
            }

            if (from > to) {
                from = new Date();
                from.setDate(to.getDate() - 2);
            }

            const url = `/admin/stats?from=${from.toISOString().substr(0, 10)}&to=${to.toISOString().substr(0, 10)}`;
            go(url);
        },
        removeTime(dateTime) {
            dateTime.setHours(0, 0, 0, 0);
            dateTime.setDate(dateTime.getDate() + 1);
        },
    },
    mounted() {
        const domChartReview = document.getElementById('chart-reviews');
        new Chart(domChartReview, {
            responsive: true,
            type: 'line',
            data: {
                labels: this.timeFrame,
                datasets: [
                    {
                        label: 'Reviews',
                        backgroundColor: 'cornflowerblue',
                        borderColor: 'dodgerblue',
                        fill: false,
                        data: window.REVIEW_DATA,
                    },
                    {
                        label: 'Uploads',
                        backgroundColor: 'lightseagreen',
                        borderColor: 'darkseagreen',
                        fill: false,
                        data: window.UPLOAD_DATA,
                    },
                ],
            },
            options: {
                title: {
                    text: 'Reviews',
                },
            },
        });

        const domChartDownload = document.getElementById('chart-downloads');
        new Chart(domChartDownload, {
            responsive: true,
            type: 'line',
            data: {
                labels: this.timeFrame,
                datasets: [
                    {
                        label: 'Total Downloads',
                        backgroundColor: 'cornflowerblue',
                        borderColor: 'dodgerblue',
                        fill: false,
                        data: window.TOTAL_DOWNLOAD_DATA,
                    },
                    {
                        label: 'Unsafe Downloads',
                        backgroundColor: 'lightseagreen',
                        borderColor: 'darkseagreen',
                        fill: false,
                        data: window.UNSAFE_DOWNLOAD_DATA,
                    },
                ],
            },
            options: {
                title: {
                    text: 'Downloads',
                },
            },
        });

        const domChartFlags = document.getElementById('chart-flags');
        new Chart(domChartFlags, {
            responsive: true,
            type: 'line',
            data: {
                labels: this.timeFrame,
                datasets: [
                    {
                        label: 'Opened flags', // 'Open flags' is a bit of a misleading name
                        backgroundColor: 'cornflowerblue',
                        borderColor: 'dodgerblue',
                        fill: false,
                        data: window.OPEN_FLAGS_DATA,
                    },
                    {
                        label: 'Closed flags',
                        backgroundColor: 'lightseagreen',
                        borderColor: 'darkseagreen',
                        fill: false,
                        data: window.CLOSED_FLAGS_DATA,
                    },
                ],
            },
            options: {
                title: {
                    text: 'Flags',
                },
            },
        });
    },
};
</script>
