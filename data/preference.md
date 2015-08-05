# Preference Pages

**标识符Identifier**：org.eclipse.ui.preferencePages

**描述Description**：

工作台提供为偏好设置提供了一个常规的对话框。这个扩展点的目的是允许插件在偏好设置中增加对话框。当偏好设置对话框被打开（在菜单条），通过这种方式构建的页面将会出现在对话框中

偏好设置对话框提供分级的页面。由于这个原因，一个页面可以指定catagory（目录）属性。这个属性代表了由parent page IDs组成的路径（通过/分隔）。如果这个属性被删除或者路径中任何一个父节点没有被找到，页面将会被添加到跟

>The workbench provides one common dialog box for preferences. The purpose of this extension point is to allow plug-ins to add pages to the preference dialog box. When preference dialog box is opened (initiated from the menu bar), pages contributed in this way will be added to the dialog box.

>The preference dialog box provides for hierarchical grouping of the pages. For this reason, a page can optionally specify a category attribute. This attribute represents a path composed of parent page IDs separated by '/'. If this attribute is omitted or if any of the parent nodes in the path cannot be found, the page will be added at the root level.

**配置加成Configuration Markup**：

extension节点

属性

point 扩展点的合法标识符

id 可选

name 可选

》page节点

属性

id 唯一的名字用来标识页面

name 能够被用来在UI中代表page的名称

class 实现org.eclipse.ui.IWokbenchPreferencePage的类的合法名称

category 路径，指定页面在偏好树中的位置。可以是node ID也可以是'/'分隔的序列

**示例Examples**：

```xml
<extension 
    point="org.eclipse.ui.preferencePages"> 
    <page 
        id="com.xyz.prefpage1" 
        name="XYZ" 
        class="com.xyz.prefpages.PrefPage1"> 
        <keywordReference id="xyz.Keyword"/>
    </page> 
    <page 
        id="com.xyz.prefpage2" 
        name="Keyboard Settings" 
        class="com.xyz.prefpages.PrefPage2" 
        category="com.xyz.prefpage1"> 
    </page> 
</extension> 
```

