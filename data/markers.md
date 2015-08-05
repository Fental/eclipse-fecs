# Resource Markers

[参考链接](http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fextension-points%2Forg_eclipse_core_resources_builders.html)

**标识符identifier**：org.eclipse.core.resources.markers

**描述Description**：

工作区支持在任意资源上标记的概念。一个标记是一种元数据（类似于属性）能够用来给资源加上用户信息的标签。工作区可以选择让标记是否一致存在无论工作区被保存或者保存快照

用户能够定义和查询给定类型的标记。标记类型在支持多继承的体系中定义。标记类型定义也指定一些属性，这些属性必须或可能（must or may be present on a marker of that type as well as whether or not markers of that type should be persisted.???）

markers扩展点允许编写marker的人使用象征性的名字注册marker types，这个名字能够被用来在工作区创建和查询markers。象征性的名字是marker扩展的id。when defining a marker extension, users are encouraged to include a human-readable value for the "name" attribute whitch indentifies(识别) their marker and potentially may be presented to users.

>The workspace supports the notion of markers on arbitrary resources. A marker is a kind of metadata (similar to properties) which can be used to tag resources with user information. Markers are optionally persisted by the workspace whenever a workspace save or snapshot is done.

>Users can define and query for markers of a given type. Marker types are defined in a hierarchy that supports multiple-inheritance. Marker type definitions also specify a number attributes which must or may be present on a marker of that type as well as whether or not markers of that type should be persisted.

>The markers extension-point allows marker writers to register their marker types under a symbolic name that is then used from within the workspace to create and query markers. The symbolic name is the id of the marker extension. When defining a marker extension, users are encouraged to include a human-readable value for the "name" attribute which indentifies their marker and potentially may be presented to users.

**配置加成configuration Markup**

extension节点

》persistent节点

属性

value 表明是否markers在工作区持续存在

》super节点

属性

type 一个marker超类型的合法id（i.e., a marker type difined by another marker extension）

》attribute节点

属性

name 属性的名字，这个名字会存在于该类型的标记

**示例Examples**：

```xml
<extension id="com.xyz.coolMarker" point="org.eclipse.core.resources.markers" name="Cool Marker"> 
    <persistent value="true"/> 
    <super type="org.eclipse.core.resources.problemmarker"/> 
    <super type="org.eclipse.core.resources.textmarker"/> 
    <attribute name="owner"/> 
</extension> 
```