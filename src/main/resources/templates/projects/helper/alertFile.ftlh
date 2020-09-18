<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro alertFile>
    <div class="alert-file file-project float-right" style="display: none;">
        <div class="alert alert-info float-left">
            <i class="far fa-file-archive"></i>
            <strong class="file-name"></strong>
            <span class="file-size float-right"></span>
        </div>
        <div class="file-upload float-right">
            <button data-toggle="tooltip" data-placement="right" title="Sign plugin" type="submit" name="submit"
                    form="form-upload"
                    class="btn btn-info btn-block btn-sign">
                <i class="fas fa-pencil-alt"></i>
            </button>
        </div>
    </div>
</#macro>