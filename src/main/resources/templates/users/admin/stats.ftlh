<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign scriptsVar>
    <script <#--@CSPNonce.attr-->>
      window.DAYS = ${days};
      window.REVIEW_DATA = ${reviewData};
      window.UPLOAD_DATA = ${uploadData};
      window.TOTAL_DOWNLOAD_DATA = ${totalDownloadData};
      window.UNSAFE_DOWNLOAD_DATA = ${unsafeDownloadData};
      window.OPEN_FLAGS_DATA = ${openFlagsData};
      window.CLOSED_FLAGS_DATA = ${closedFlagsData};
    </script>
    <script <#--@CSPNonce.attr--> type="text/javascript" src="<@hangar.url "js/stats.js" />"></script>
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
