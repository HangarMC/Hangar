<#macro form action method id="">
    <form action="${action}" method="${method}" <#if id??>id="${id}"</#if>>
        <#nested>
    </form>
</#macro>
