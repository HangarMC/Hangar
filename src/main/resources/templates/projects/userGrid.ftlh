<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/userAvatar.ftlh" as userAvatar />
<#import "*/projects/view.ftlh" as projects />

<#assign columns = 3>
<#-- @ftlvariable name="pageSize" type="java.lang.Integer" -->
<#-- @ftlvariable name="page" type="java.lang.Integer" -->
<@projects.view p=p sp=sp active="">
    <div class="row user-grid">
        <div class="col-12">
            <#-- @ftlvariable name="title" type="java.lang.String" -->
            <h3 class="text-bold mb-0">${title}</h3>
            <div class="extra-divider"></div>
        </div>

        <#list 1..columns as div>
            <div class="col-md-4">
                <#-- @ftlvariable name="users" type="java.util.Collection<io.papermc.hangar.db.model.UsersTable>" -->
                <#-- @ftlvariable name="gridUser" type="io.papermc.hangar.db.model.UsersTable" -->
                <#list users?filter(u -> (users?seq_index_of(u) + 1) % columns == div) as gridUser>
                    <div class="media">
                        <div class="media-left">
                            <@userAvatar.userAvatar userName=gridUser.name avatarUrl=utils.avatarUrl(gridUser.name) clazz="user-avatar-sm mr-3" />
                        </div>
                        <div class="media-body align-self-center">
                            <a href="${Routes.USERS_SHOW_PROJECTS.getRouteUrl(gridUser.name)}">${gridUser.name}</a>
                        </div>
                    </div>
                </#list>
            </div>
        </#list>

        <#if p.starCount gte pageSize || page gt 1>
            <div class="col-12">
                <div class="float-right next-back">
                    <#if page gt 1>
                        <a href="@call(page - 1)" class="btn btn-default"><i class="fa fa-arrow-left"></i></a>
                    </#if>
                    <#if p.starCount gt pageSize * page>
                        <a href="@call(page + 1)" class="btn btn-default"><i class="fa fa-arrow-right"></i></a>
                    </#if>
                </div>
            </div>
        </#if>
    </div>
</@projects.view>
