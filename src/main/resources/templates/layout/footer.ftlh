<#--
@()(implicit messages: Messages, assetsFinder: AssetsFinder)
-->

<#macro footer>
    <footer class="footer">
        <div class="above">
            <div class="container">
                <div class="row">
                    <div class="col-lg-12">
                        By using this site you're accepting our <a class="tos"
                                                                   href="https://docs.spongepowered.org/stable/en/about/tos.html">@messages("general.terms")</a>
                    </div>
                </div>
            </div>
        </div>
        <div class="middle">
            <div class="container">
                <div class="row">
                    <div class="col-sm-2 col-xs-12">
                        <h5>Hangar API</h5>
                        <a class="btn dark" href="${@routes.Application.swagger()}">${@messages("general.api")}</a>
                    </div>
                    <div class="col-sm-2 col-xs-12">
                        <h5>Help Needed?</h5>
                        <a class="btn dark" href="https://papermc.io/forums/">${@messages("general.contact")}</a>
                    </div>
                    <div class="col-sm-4 col-xs-12">
                        <a href="https://www.papermc.io">
                            <img class="spongie"
                                 src="https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png"
                                 alt="Paper logo"/>
                        </a>
                    </div>
                    <div class="col-sm-2 col-xs-12">
                        <h5>Found an Issue?</h5>
                        <a class="btn dark" target="_blank" rel="noopener"
                           href="https://github.com/MiniDigger/Hangar/issues">${@messages("general.issues")}</a>
                    </div>
                    <div class="col-sm-2 col-xs-12">
                        <h5>Our Sponsors</h5>
                        <a class="btn dark" href="https://www.papermc.io/sponsors">${@messages("general.sponsors")}</a>
                    </div>
                </div>
            </div>
        </div>
        <div class="below">
            <div class="container">
                <div class="row">
                    <div class="col-lg-12">
                        <p>Copyright &copy; ${@messages("general.organization")}
                            2016-${Calendar.getInstance().get(Calendar.YEAR)}</p>
                    </div>
                </div>
            </div>
        </div>
    </footer>
</#macro>
