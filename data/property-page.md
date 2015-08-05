# Property Pages

[原文链接](http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fextension-points%2Forg_eclipse_core_resources_builders.html)

**标识符Identifier**：org.eclipse.ui.propertyPages

**描述Description**：

这个扩展点被用来增加给定类型对象的额外的属性页。一旦定义，这些属性页将会出现在指定类型对象的Properties对话框中。

property page是用户友好地与对象的属性进行交互。不同于properties view（限制编辑对象属性的空间），property page可能受益于自由定义 关于标签、图标等 的更大、更复杂的控制。逻辑上应该在一块的属性可能被聚集在一个页面，而不是随意放置在property sheet。但是在大多数应用中，可能会通过property sheet暴露一些对象属性，通过property pages暴露特定的对象类型

property pages被展示在一个对话框中，这个对话框将会在一个对象的"Properties"项被选中为弹出式菜单时展现。除了object class之外，名称过滤器可以选择性地支持，以便注册属性页仅仅为了特定的object types

如果这些filtering mechanisms(机制)不足够，property page可能使用filter mechanism。这种情况下，目标对象的属性将会使用键值对来描述。用于选择的属性是类型指定的，同时超越工作台的区域，所以
so the workbench will delegate filtering at this level to the actual selection.

>This extension point is used to add additional property page for objects of a given type. Once defined, these property pages will appear in the Properties Dialog for objects of that type.

>A property page is a user friendly way to interact with the properties of an object. Unlike the Properties view, which restricts the space available for editing an object property, a property page may benefit from the freedom to define larger, more complex controls with labels, icons, etc. Properties which logically go together may also be clustered in a page, rather than scattered in the property sheet. However, in most applications it will be appropriate to expose some properties of an object via the property sheet and some via the property pages.

>Property pages are shown in a dialog box that is normally visible when the "Properties" menu item is selected on a pop-up menu for an object. In addition to the object class, the name filter can optionally be supplied to register property pages only for specific object types.

>If these filtering mechanisms are inadequate a property page may use the filter mechanism. In this case the attributes of the target object are described in a series of key value pairs. The attributes which apply to the selection are type specific and beyond the domain of the workbench itself, so the workbench will delegate filtering at this level to the actual selection.

**配置加成Configuration Markup**：

extension节点

》page节点

属性

id 一个唯一的名称用来表示页面

name 一个可翻译的名称，在UI中会被使用，用来表示这个page

icon 

class 一个实现org.eclipse.ui.IWorkbenchPropertyPage的合法名称

》》enableWhen节点

这个节点用于指定property page被加到properties dialog的条件

》》》adapt节点

这个节点用于调整对象，使其重点关注指定类型（属性type指定）

属性

type 对象重点关注的类型

》》》》instanceof

这个节点用来进行对象指定类型的instanceof（子类）检查

属性

value 接口或类的合法名称

**示例Examples**：

```xml
<extension point="org.eclipse.ui.propertyPages"> 
    <page 
        id="com.xyz.projectPage" 
        name="XYZ Java Properties" 
        class="com.xyz.ppages.JavaPropertyPage" 
        nameFilter="*.java"> 
        <enabledWhen>
             <instanceof
                 value="org.eclipse.core.resources.IFile">
             </instanceof>
        </enabledWhen>
        <filter name="readOnly" value="true"/> 
    </page> 
</extension> 
```