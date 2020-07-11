@*
The main entry point of Ore. This page displays a list of Projects that can be
sorted according to different criteria.
*@
@import scala.util.Random

@import controllers.sugar.Requests.OreRequest
@import ore.OreConfig
@()(implicit messages: Messages, flash: Flash, request: OreRequest[_], config: OreConfig, assetsFinder: AssetsFinder)

<#import "/spring.ftl" as spring />
<#import "../utils/hangar.ftlh" as hangar />

@randomSponsor = @{
    val logos = config.sponge.sponsors

    val index = new Random().nextInt(logos.size)
    logos(index)
}

@scripts = {
    <script src="<@spring.url "build/home.js" />"></script>
}

@stylesheets = {
    <link rel="stylesheet" type="text/css" href="@routes.Assets.versioned("build/home.css")" />
}

@meta = {
    <meta property="og:title" content="<@spring.message "general.appName" />">
    <meta property="og:type" content="website" />
    <meta property="og:url" content="@config.app.baseUrl">
    <meta property="og:image" content="<@spring.url "images/ore-dark.png" />" />
    <meta property="og:description" content="<@spring.message "general.description" />" />
}

@layout.base(messages("general.title"), scripts, additionalMeta = meta, additionalStyling = stylesheets) {

    <!-- Header -->
    <div class="index-header">
        <div class="row centered-content-row">
            <div class="col-md-9 ore-banner">
                <div class="row aligned-row">
                    <div class="col-xs-2 ore-logo">
                        <img src="<@spring.url "images/ore-colored.svg" />" alt="Ore logo" />
                    </div>
                    <div class="col-xs-10 text">
                        <div class="headline">Hangar</div>
                        <div>A Minecraft package repository</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3 sponsor">
                <div class="panel sponsor-panel">
                    <span>Sponsored by</span>
                    <div class="panel-body">
                    @defining(randomSponsor) { sponsor =>
                        <a href="@sponsor.link">
                            <img class="logo" src="<@spring.urls sponsor.image />" alt="Sponsor" />
                        </a>
                    }
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="home"></div>
}
