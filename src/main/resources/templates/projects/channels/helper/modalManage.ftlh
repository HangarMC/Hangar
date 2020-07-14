<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/projects/channels/helper/popoverColorPicker.ftlh" as popoverColorPicker />

@import ore.OreConfig
@import views.html.helper.{CSRF, form}
@()(implicit messages: Messages, config: OreConfig, request: Request[_])

<#macro modalManage>
    <div class="modal fade" id="channel-settings" tabindex="-1" role="dialog" aria-labelledBy="settings-label">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="<@spring.message "general.cancel" />">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title"></h4>
                </div>
                @form(action = routes.Application.showHome()) {
                    @CSRF.formField
                    <div class="modal-body">
                        <div class="form-inline">
                            <label for="channel-input"><@spring.message "channel.name" /></label>
                            <input class="form-control channel-input" name="channel-input" type="text" value=""
                                   maxlength="@config.ore.channels.maxNameLen"/>
                            <input type="hidden" name="channel-color-input" class="channel-color-input" value="" />
                            <a href="#">
                                <span class="color-picker" data-toggle="popover" data-placement="right" data-trigger="hover">
                                    <i class="fas fa-circle channel-id" style=""></i>
                                </span>
                            </a>
                            <@popoverColorPicker.popoverColorPicker />
                            <span class="pull-right channel preview" style="display: none;"></span>
                            <p class="help-block"><@spring.message "channel.nameRequirements" /></p>
                            <br/>
                            <span class="minor"><@spring.message "channel.nonReviewed" /></span>
                            <input class="form-control non-reviewed"
                                   name="non-reviewed"
                                   type="checkbox"
                                   value="true" />
                            <a href="#">
                                <i class="fas fa-question-circle"
                                   title="<@spring.message "channel.nonReviewed.info" />"
                                   data-toggle="tooltip"></i>
                            </a>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">
                            <@spring.message "channel.edit.close" />
                        </button>
                        <input type="submit" value="" class="btn btn-primary" disabled />
                    </div>
                }
            </div>
        </div>
    </div>
</#macro>
