<#function licenseFormat licenses>
    <#assign result><#list licenses as license>[${license}]<#if (license_has_next)> - </#if></#list></#assign>
    <#assign result = "\"" + result + "\","/>
    <#return result>
</#function>
<#function artifactFormat p>
    <#if p.name?index_of('Unnamed') &gt; -1>
        <#return "\"" + p.artifactId + "\",\"" + p.groupId + ":" + p.artifactId + ":" + p.version + "\",\"" + (p.url!"no url defined") + "\"">
    <#else>
        <#return "\"" + p.name + "\",\"" + p.groupId + ":" + p.artifactId + ":" + p.version + "\",\"" + (p.url!"no url defined") + "\"">
    </#if>
</#function>
<#if dependencyMap?size == 0>
    The project has no dependencies.
<#else>
    "License","Name","Artifact","URL"
    <#list dependencyMap as e>
        <#assign project = e.getKey()/>
        <#assign licenses = e.getValue()/>
        ${licenseFormat(licenses)}${artifactFormat(project)}
    </#list>
</#if>
