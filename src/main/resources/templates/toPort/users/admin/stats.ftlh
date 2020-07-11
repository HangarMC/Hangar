@import java.time.LocalDate
@import java.time.temporal.ChronoUnit

@import play.api.libs.json.Json

@import controllers.sugar.Requests.OreRequest
@import models.querymodels.Stats
@import ore.OreConfig
@import views.html.helper.CSPNonce

@(stats: List[Stats], fromTime: LocalDate, toTime: LocalDate)(implicit messages: Messages,
        request: OreRequest[_], config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)

@scripts = {
    <script @CSPNonce.attr type="text/javascript" src="@assetsFinder.path("lib/chart.js/dist/Chart.min.js")"></script>
    <script @CSPNonce.attr>
            $(function(){
                var domChartReview = document.getElementById("chart-reviews");
                var chartReviews = new Chart(domChartReview, {
                    responsive: true,
                    type: 'line',
                    data: {
                        labels: @Html(Json.stringify(Json.toJson(stats.map(_.day.toString)))),
                        datasets: [{
                            label: "Reviews",
                            backgroundColor: "cornflowerblue",
                            borderColor: "dodgerblue",
                            fill: false,
                            data: @Html(Json.stringify(Json.toJson(stats.map(_.reviews))))
                        }, {
                            label: "Uploads",
                            backgroundColor: "lightseagreen",
                            borderColor: "darkseagreen",
                            fill: false,
                            data: @Html(Json.stringify(Json.toJson(stats.map(_.uploads))))
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
                        labels: @Html(Json.stringify(Json.toJson(stats.map(_.day.toString).takeRight(30)))),
                        datasets: [{
                            label: "Total Downloads",
                            backgroundColor: "cornflowerblue",
                            borderColor: "dodgerblue",
                            fill: false,
                            data: @Html(Json.stringify(Json.toJson(stats.map(_.totalDownloads).takeRight(30))))
                        }, {
                            label: "Unsafe Downloads",
                            backgroundColor: "lightseagreen",
                            borderColor: "darkseagreen",
                            fill: false,
                            data: @Html(Json.stringify(Json.toJson(stats.map(_.unsafeDownloads).takeRight(30))))
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
                        labels: @Html(Json.stringify(Json.toJson(stats.map(_.day.toString)))),
                        datasets: [{
                            label: "Open flags",
                            backgroundColor: "cornflowerblue",
                            borderColor: "dodgerblue",
                            fill: false,
                            data: @Html(Json.stringify(Json.toJson(stats.map(_.flagsOpened))))
                        }, {
                            label: "Closed flags",
                            backgroundColor: "lightseagreen",
                            borderColor: "darkseagreen",
                            fill: false,
                            data: @Html(Json.stringify(Json.toJson(stats.map(_.flagsClosed))))
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
    <script @CSPNonce.attr type="text/javascript" src="@assetsFinder.path("javascripts/stats.js")"></script>
}

@layout.base("Stats", scripts) {
    <div class="row">
        <div class="col-md-12">
            <div class="form-inline">
                <div class="form-group">
                    <label for="fromDate">From:</label>
                    <input id="fromDate" type="date" class="form-control" value="@fromTime.toString" max="@LocalDate.now().minus(1, ChronoUnit.DAYS)">
                </div>
                <div class="form-group">
                    <label for="toDate">To:</label>
                    <input id="toDate" type="date" class="form-control" value="@toTime.toString" max="@LocalDate.now().toString">
                </div>
            <button id="dateGoButton" class="btn btn-default">Go</button>
            </div>
        </div>

        <div class="col-md-12">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title pull-left">Plugins</h4>
                    <div class="clearfix"></div>
                </div>
                <canvas id="chart-reviews" height="40vh" width="100%"></canvas>
            </div>
        </div>
        <div class="col-md-12">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title pull-left">Downloads</h4>
                    <div class="clearfix"></div>
                </div>
                <canvas id="chart-downloads" height="40vh" width="100%"></canvas>
            </div>
        </div>
        <div class="col-md-12">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title pull-left">Flags</h4>
                    <div class="clearfix"></div>
                </div>
                <canvas id="chart-flags" height="40vh" width="100%"></canvas>
            </div>
        </div>
    </div>
}
