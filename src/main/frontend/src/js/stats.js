import $ from 'jquery';
import { go } from '@/utils';
import Chart from 'chart.js';

//=====> DOCUMENT READY

$(function() {
    const TIME_FRAME = window.DAYS;
    var domChartReview = document.getElementById('chart-reviews');
    new Chart(domChartReview, {
        responsive: true,
        type: 'line',
        data: {
            labels: TIME_FRAME,
            datasets: [
                {
                    label: 'Reviews',
                    backgroundColor: 'cornflowerblue',
                    borderColor: 'dodgerblue',
                    fill: false,
                    data: window.REVIEW_DATA
                },
                {
                    label: 'Uploads',
                    backgroundColor: 'lightseagreen',
                    borderColor: 'darkseagreen',
                    fill: false,
                    data: window.UPLOAD_DATA
                }
            ]
        },
        options: {
            title: {
                text: 'Reviews'
            }
        }
    });

    var domChartDownload = document.getElementById('chart-downloads');
    new Chart(domChartDownload, {
        responsive: true,
        type: 'line',
        data: {
            labels: TIME_FRAME,
            datasets: [
                {
                    label: 'Total Downloads',
                    backgroundColor: 'cornflowerblue',
                    borderColor: 'dodgerblue',
                    fill: false,
                    data: window.TOTAL_DOWNLOAD_DATA
                },
                {
                    label: 'Unsafe Downloads',
                    backgroundColor: 'lightseagreen',
                    borderColor: 'darkseagreen',
                    fill: false,
                    data: window.UNSAFE_DOWNLOAD_DATA
                }
            ]
        },
        options: {
            title: {
                text: 'Downloads'
            }
        }
    });

    var domChartFlags = document.getElementById('chart-flags');
    new Chart(domChartFlags, {
        responsive: true,
        type: 'line',
        data: {
            labels: TIME_FRAME,
            datasets: [
                {
                    label: 'Opened flags', // 'Open flags' is a bit of a misleading name
                    backgroundColor: 'cornflowerblue',
                    borderColor: 'dodgerblue',
                    fill: false,
                    data: window.OPEN_FLAGS_DATA
                },
                {
                    label: 'Closed flags',
                    backgroundColor: 'lightseagreen',
                    borderColor: 'darkseagreen',
                    fill: false,
                    data: window.CLOSED_FLAGS_DATA
                }
            ]
        },
        options: {
            title: {
                text: 'Flags'
            }
        }
    });

    $('#dateGoButton').click(function() {
        var from = $('#fromDate').val();
        var to = $('#toDate').val();

        function removeTime(date) {
            date.setHours(0, 0, 0, 0);
            date.setDate(date.getDate() + 1);
        }

        if (!to) {
            to = new Date();
        } else {
            to = new Date(to);
        }
        removeTime(to);

        if (!from) {
            from = new Date();
            from.setDate(to.getDate() - 10);
        } else {
            from = new Date(from);
        }
        removeTime(from);

        var now = removeTime(new Date());
        if (to > now) {
            to.setDate(now.getDate());
        }

        if (from > to) {
            from = new Date();
            from.setDate(to.getDate() - 2);
        }

        var url = '/admin/stats?from=' + from.toISOString().substr(0, 10) + '&to=' + to.toISOString().substr(0, 10);
        go(url);
    });
});
