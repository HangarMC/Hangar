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

      window.MAX_DATE = {
          FROM: '${@helper["java.time.LocalDate"].now().minus(1, @helper["java.time.temporal.ChronoUnit"].DAYS)}',
          TO: '${@helper["java.time.LocalDate"].now()}',
      };
    </script>
    <script <#--@CSPNonce.attr--> type="text/javascript" src="<@hangar.url "js/stats.js" />"></script>
</#assign>

<@base.base title="Stats" additionalScripts=scriptsVar>
    <h1>Stats</h1>
    <div id="stats"></div>
</@base.base>
