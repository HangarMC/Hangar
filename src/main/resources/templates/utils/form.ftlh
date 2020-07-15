<#macro form action method id="" class="" enctype="">
    <form action="${action}" method="${method}" <#if id??>id="${id}"</#if> <#if class??>class="${class}"</#if> <#if enctype??>enctype="${enctype}"</#if>>
        <#nested>
    </form>
</#macro>
