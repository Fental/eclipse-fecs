# Incremental Project Builders

[原文链接](http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fextension-points%2Forg_eclipse_core_resources_builders.html)

**标识符Identifier**：org.eclipse.core.resources.builders

**描述Description**：

工作区支持incremental project builder（简称为builder）的概念。builder的工作主要是处理一系列资源改变（supplied as a resource delta被用来作为资源偏移？）。譬如，一个Java builder将会重新编译改变的Java文件，提供新的class文件

Builders配置在每个项目基础上，并且当这些项目的资源被改变时，Builders会自动运行。因此Builders应该在项目资源改变量方面保持快速和规模，而不是项目资源数量。这典型暗示builders能够增长地更新他们的“built state”。

builders扩展点允许builders的编写人员使用象征性的名字注册builder的实现，这个象征性的名字被用来在工作区查找和运行builders。the symbolic name is the id of the builder extension。When defining a builder extension, users are encouraged to include a human-readable value for the "name" attribute whitch identifies their builder and potentially(潜在的) may be presented to users.

>  The workspace supports the notion of an incremental project builder (or "builder" for short"). The job of a builder is to process a set of resource changes (supplied as a resource delta). For example, a Java builder would recompile changed Java files and produce new class files.

> Builders are configured on a per-project basis and run automatically when resources within their project are changed. As such, builders should be fast and scale with respect to the amount of change rather than the number of resources in the project. This typically implies that builders are able to incrementally update their "built state".

> The builders extension-point allows builder writers to register their builder implementation under a symbolic name that is then used from within the workspace to find and run builders. The symbolic name is the id of the builder extension. When defining a builder extension, users are encouraged to include a human-readable value for the "name" attribute which identifies their builder and potentially may be presented to users.

**配置加成configuration markup**：

extension节点

》builder节点

属性

hasNature 表明是否有一个nature project 拥有builder

isConfigurable 表示是否允许触发器的定制

callOnEmptyDelta 表明是否builder应该被调用当工程为空

supportsConfigurations：表明是否支持多build配置

》》run节点

属性

class org.eclipse.core.resources.IncrementalProjectBuilder子类合法的名字

》》》parameter节点

**示例Examples**：

```xml
<extension id="coolbuilder" name="Cool Builder" point="org.eclipse.core.resources.builders"> 
    <builder hasNature="false"> 
        <run class="com.xyz.builders.Cool"> 
            <parameter name="optimize" value="true"/> 
            <parameter name="comment" value="Produced by the Cool Builder"/> 
         </run> 
    </builder> 
</extension> 
```