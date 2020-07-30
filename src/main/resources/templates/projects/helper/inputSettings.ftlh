<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro inputSettings form homepage="" issues="" source="" support="" licenseName="" licenseUrl="" selected=@helper["me.minidigger.hangar.model.Category"].UNDEFINED forumSync=true keywords=[]>
    <div class="setting">
        <div class="setting-description">
            <h4>Category</h4>
            <p>
                Categorize your project into one of ${@helper["me.minidigger.hangar.model.Category"].visible()?size} categories. Appropriately categorizing your
                project makes it easier for people to find.
            </p>
        </div>
        <div class="setting-content">
            <select class="form-control" id="category" name="category" form="${form}">
                <#-- @ftlvariable name="category" type="me.minidigger.hangar.model.Category" -->
                <#list @helper["me.minidigger.hangar.model.Category"].values() as category>
                    <#if category.isVisible()>
                        <option <#if selected?? && selected.equals(category)> selected </#if> >
                            ${category.title}
                        </option>
                    </#if>
                </#list>
            </select>
        </div>
        <div class="clearfix"></div>
    </div>

    <div class="setting">
        <div class="setting-description">
            <h4>Keywords <i>(optional)</i></h4>
            <p>
                These are special words that will return your project when people add them to their searches. Max 5.
            </p>
        </div>
        <input <#if keywords?size gt 0> value="${keywords?join(" ")}" </#if> form="${form}" type="text" class="form-control" id="keywords"
        name="keywords" placeholder="sponge server plugins mods" />
        <div class="clearfix"></div>
    </div>

    <div class="setting">
        <div class="setting-description">
            <h4>Homepage <i>(optional)</i></h4>
            <p>
                Having a custom homepage for your project helps you look more proper, official, and gives you another place
                to gather information about your project.
            </p>
        </div>
        <input <#if homepage?has_content> value="${homepage}" </#if> form="${form}" type="url" class="form-control" id="homepage" name="homepage" placeholder="https://papermc.io" />
        <div class="clearfix"></div>
    </div>

    <div class="setting">
        <div class="setting-description">
            <h4>Issue tracker <i>(optional)</i></h4>
            <p>
                Providing an issue tracker helps your users get support more easily and provides you with an easy way to
                track bugs.
            </p>
        </div>
        <input <#if issues?has_content> value="${issues}" </#if> form="${form}" type="url" class="form-control" id="issues"
               name="issues" placeholder="https://github.com/MiniDigger/Hangar/issues" />
        <div class="clearfix"></div>
    </div>

    <div class="setting">
        <div class="setting-description">
            <h4>Source code <i>(optional)</i></h4>
            <p>Support the community of developers by making your project open source!</p>
        </div>
        <input <#if source?has_content> value="${source}" </#if> form="${form}" type="url" class="form-control" id="source" name="source" placeholder="https://github.com/MiniDigger/Hangar" />
    </div>

    <div class="setting">
        <div class="setting-description">
            <h4>External support <i>(optional)</i></h4>
            <p>
                An external place where you can offer support to your users. Could be a forum, a Discord server, or
                somewhere else.
            </p>
        </div>
        <input <#if support?has_content> value="${support}" </#if> form="${form}" type="url" class="form-control" id="support" name="support" placeholder="https://discord.gg/papermc" />
        <div class="clearfix"></div>
    </div>

    <div class="setting">
        <div class="setting-description">
            <h4><@spring.message "project.settings.license" /> <i>(<@spring.message "general.optional" />)</i></h4>
            <p><@spring.message "project.settings.license.info" /></p>
        </div>
        <div class="input-group pull-left">
            <div class="input-group-btn">
                <button type="button" class="btn btn-default btn-license dropdown-toggle" data-toggle="dropdown"
                        aria-haspopup="true" aria-expanded="false">
                    <span class="license"><#if licenseName?has_content>${licenseName}<#else><@spring.message "licenses.mit" /></#if></span>
                    <span class="caret"></span>
                </button>
                <input type="text" class="form-control" style="display: none;" name="license-name" form="${form}"
                       value="<#if licenseName?has_content>${licenseName}<#else><@spring.message "licenses.mit" /></#if>" />
                <ul class="dropdown-menu dropdown-license">
                    <li><a><@spring.message "licenses.mit" /></a></li>
                    <li><a><@spring.message "licenses.apache2.0" /></a></li>
                    <li><a><@spring.message "licenses.gpl" /></a></li>
                    <li><a><@spring.message "licenses.lgpl" /></a></li>
                    <li role="separator" class="divider"></li>
                    <li><a class="license-custom"><@spring.message "licenses.custom" />&hellip;</a></li>
                </ul>
            </div>
            <input type="text" name="license-url" class="form-control" form="${form}"
                   placeholder="https://github.com/MiniDigger/Hangar/LICENSE.txt" value="${licenseUrl}">
        </div>
        <div class="clearfix"></div>
    </div>

    <div class="setting">
        <div class="setting-description">
            <h4><@spring.message "project.settings.forumSync" /></h4>
            <p><@spring.message "project.settings.forumSync.info" /></p>
        </div>
        <div class="setting-content">
            <label>
                <input <#if forumSync> checked </#if> value="true" form="${form}" type="checkbox" id="forum-sync" name="forum-sync">
                Make forum posts
            </label>
        </div>
        <div class="clearfix"></div>
    </div>
</#macro>

