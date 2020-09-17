<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro footer>
    <footer class="footer">
        <div class="above">
            <div class="container">
                <div class="row">
                    <div class="col-lg-12">
                        By using this site you're accepting our
                        <a class="tos"
                           href="https://docs.spongepowered.org/stable/en/about/tos.html"><@spring.message "general.terms" /></a>
                    </div>
                </div>
            </div>
        </div>
        <div class="middle">
            <div class="container">
                <div class="row">
                    <div class="col-sm-2 col-12">
                        <h5>Hangar API</h5>
                        <a class="btn dark"
                           href="${Routes.SWAGGER.getRouteUrl()}"><@spring.message "general.api" /></a>
                    </div>
                    <div class="col-sm-2 col-12">
                        <h5>Help Needed?</h5>
                        <a class="btn dark" href="https://papermc.io/forums/"><@spring.message "general.contact" /></a>
                    </div>
                    <div class="col-sm-4 col-12">
                        <a href="https://www.papermc.io">
                            <img class="spongie"
                                 src="https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png"
                                 alt="Paper logo"/>
                        </a>
                    </div>
                    <div class="col-sm-2 col-12">
                        <h5>Found an Issue?</h5>
                        <a class="btn dark" target="_blank" rel="noopener"
                           href="https://github.com/PaperMC/Hangar/issues"><@spring.message "general.issues" /></a>
                    </div>
                    <div class="col-sm-2 col-12">
                        <h5>Our Sponsors</h5>
                        <a class="btn dark"
                           href="https://www.papermc.io/sponsors"><@spring.message "general.sponsors" /></a>
                    </div>
                </div>
            </div>
        </div>
        <div class="below">
            <div class="container">
                <div class="row">
                    <div class="col-lg-12">
                        <#assign date = .now>
                        <p>Copyright &copy; <@spring.message "general.organization" />
                            <#--noinspection FtlReferencesInspection-->
                            2016-${date?string.yyyy}</p>
                    </div>
                </div>
            </div>
        </div>
    </footer>
</#macro>
