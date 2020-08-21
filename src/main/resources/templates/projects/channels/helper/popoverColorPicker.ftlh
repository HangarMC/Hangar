<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro popoverColorPicker>
    <#assign Color=@helper["io.papermc.hangar.model.Color"]>
    <table class="popover-color-picker" style="display: none;">
        <tbody>
            <tr>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(0).getHex()}"></i>
                </td>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(1).getHex()}"></i>
                </td>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(2).getHex()}"></i>
                </td>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(3).getHex()}"></i>
                </td>
            </tr>
            <tr>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(4).getHex()}"></i>
                </td>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(5).getHex()}"></i>
                </td>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(6).getHex()}"></i>
                </td>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(7).getHex()}"></i>
                </td>
            </tr>
            <tr>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(8).getHex()}"></i>
                </td>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(9).getHex()}"></i>
                </td>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(10).getHex()}"></i>
                </td>
                <td style="padding: 3px;">
                    <i class="fas fa-circle channel-id" style="color: ${Color.getById(11).getHex()}"></i>
                </td>
            </tr>
        </tbody>
    </table>
</#macro>
