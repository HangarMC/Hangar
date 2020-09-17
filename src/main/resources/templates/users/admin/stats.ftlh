<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign scriptsVar>
    <script <#--@CSPNonce.attr--> type="text/javascript" src="<@hangar.url "lib/chart.js/dist/Chart.min.js" />"></script>
    <script <#--@CSPNonce.attr-->>
        $(function(){
            var timeFrame = ${days}
            var domChartReview = document.getElementById("chart-reviews");
            var chartReviews = new Chart(domChartReview, {
                responsive: true,
                type: 'line',
                data: {
                    labels: timeFrame,
                    datasets: [{
                        label: "Reviews",
                        backgroundColor: "cornflowerblue",
                        borderColor: "dodgerblue",
                        fill: false,
                        data: ${reviewData}
                    }, {
                        label: "Uploads",
                        backgroundColor: "lightseagreen",
                        borderColor: "darkseagreen",
                        fill: false,
                        data: ${uploadData}
                    }]
                },
                options: {
                    title: {
                        text: "Reviews"
                    }
                }
            });

            var domChartDownload = document.getElementById("chart-downloads");
            var chartDownloads = new Chart(domChartDownload, {
                responsive: true,
                type: 'line',
                data: {
                    labels: timeFrame,
                    datasets: [{
                        label: "Total Downloads",
                        backgroundColor: "cornflowerblue",
                        borderColor: "dodgerblue",
                        fill: false,
                        data: ${totalDownloadData}
                    }, {
                        label: "Unsafe Downloads",
                        backgroundColor: "lightseagreen",
                        borderColor: "darkseagreen",
                        fill: false,
                        data: ${unsafeDownloadData}
                    }]
                },
                options: {
                    title: {
                        text: "Downloads"
                    }
                }
            });

            var domChartFlags = document.getElementById("chart-flags");
            var chartFlags = new Chart(domChartFlags, {
                responsive: true,
                type: 'line',
                data: {
                    labels: timeFrame,
                    datasets: [{
                        label: "Opened flags", // 'Open flags' is a bit of a misleading name
                        backgroundColor: "cornflowerblue",
                        borderColor: "dodgerblue",
                        fill: false,
                        data: ${openFlagsData}
                    }, {
                        label: "Closed flags",
                        backgroundColor: "lightseagreen",
                        borderColor: "darkseagreen",
                        fill: false,
                        data: ${closedFlagsData}
                    }]
                },
                options: {
                    title: {
                        text: "Flags"
                    }
                }
            });
        });
    </script>
    <script <#--@CSPNonce.attr--> type="text/javascript" src="<@hangar.url "javascripts/stats.js" />"></script>
</#assign>

<@base.base title="Stats" additionalScripts=scriptsVar>
    <h1>Stats</h1>
    <div class="row">
        <div class="col-md-12">
            <div class="form-inline">
                <div class="form-group">
                    <label for="fromDate">From:</label>
                    <input id="fromDate" type="date" class="form-control" max="${@helper["java.time.LocalDate"].now().minus(1, @helper["java.time.temporal.ChronoUnit"].DAYS)}" <#if fromDate??> value="${fromDate}"</#if>>
                </div>
                <div class="form-group">
                    <label for="toDate">To:</label>
                    <input id="toDate" type="date" class="form-control" max="${@helper["java.time.LocalDate"].now()}" <#if toDate??> value="${toDate}"</#if>>
                </div>
            <button id="dateGoButton" class="btn btn-default">Go</button>
            </div>
        </div>

        <div class="col-md-12">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title float-left">Plugins</h4>
                    <div class="clearfix"></div>
                </div>
                <canvas id="chart-reviews" height="40vh" width="100%"></canvas>
            </div>
        </div>
        <div class="col-md-12">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title float-left">Downloads</h4>
                    <div class="clearfix"></div>
                </div>
                <canvas id="chart-downloads" height="40vh" width="100%"></canvas>
            </div>
        </div>
        <div class="col-md-12">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title float-left">Flags</h4>
                    <div class="clearfix"></div>
                </div>
                <canvas id="chart-flags" height="40vh" width="100%"></canvas>
            </div>
        </div>
    </div>
</@base.base>
