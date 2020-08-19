### MyBatis分页实现

###  1.基于Mybatis扩展插件

修改generatorConfig.xml

```xml
<!-- 为模型生成limit offset方法-->
<plugin type="org.mybatis.generator.plugins.RowBoundsPlugin"></plugin>
```

可见生成的接口

```xml
<select id="selectByExampleWithRowbounds" parameterType="com.histor.demo.mbg.model.MpSuccessRatioExample" resultMap="BaseResultMap">
  select
  <if test="distinct">
    distinct
  </if>
  <include refid="Base_Column_List" />
  from mp_success_ratio
  <if test="_parameter != null">
    <include refid="Example_Where_Clause" />
  </if>
  <if test="orderByClause != null">
    order by ${orderByClause}
  </if>
</select>
```

分页使用：

```java
List<SuccessRatio> SuccessRatioList =   successRatioMapper.selectByExampleWithRowbounds(successRatioExample,
        new RowBounds(RowBounds.NO_ROW_OFFSET,1));
```

问题：

RowBounds的构造方法**new RowBounds(offset, limit)**中的offset、limit参数就相当于MySQL的select语句limit后的offset和rows。如果此时仔细观察一下日志打出来的SQL语句或者看下生成的XxxMapper.xml文件中的selectByExampleWithRowbounds元素，可以发现select语句并没有使用limit。实际上RowBounds原理是通过ResultSet的游标来实现分页，也就是并不是用select语句的limit分页而是用Java代码分页，查询语句的结果集会包含符合查询条件的所有数据，使用不慎会导致性能问题，所以并不推荐使用RowBoundsPlugin来实现分页。

### 2. 修改代码实现

在实现MySQL分页时更推荐使用select语句的limit来实现分页，然而MyBatis Generator目前并没有提供这样的插件。好在MyBatis Generator支持插件扩展，我们可以自己实现一个基于limit来分页的插件。如何实现一个插件可以参考官方文档：http://www.mybatis.org/generator/reference/pluggingIn.html 。

实现思路
在生成的XxxExample中加入两个属性limit和offset，同时加上set和get方法。也就是需要生成以下代码：

复制代码

```java
private Integer limit;
private Integer offset;
public void setLimit(Integer limit) {
    this.limit = limit;
}
public Integer getLimit() {
    return limit;
}
public void setOffset(Integer offset) {
    this.offset = offset;
}
public Integer getOffset() {
    return offset;
}
```

XxxMapper.xml中在通过selectByExample查询时，添加limit：

复制代码

```sql
<select id="selectByExample" parameterType="com.xxg.bean.XxxExample" resultMap="BaseResultMap">
  ...
  <if test="limit != null">
    <if test="offset != null">
      limit ${offset}, ${limit}
    </if>
    <if test="offset == null">
      limit ${limit}
    </if>
  </if>
</select>
```

### 插件实现代码

复制代码

```java
package com.xxg.mybatis.plugins;
 
import java.util.List;
 
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.PrimitiveTypeWrapper;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
 
public class MySQLLimitPlugin extends PluginAdapter {
 
    @Override
    public boolean validate(List<String> list) {
        return true;
    }
 
    /**
     * 为每个Example类添加limit和offset属性已经set、get方法
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
 
        PrimitiveTypeWrapper integerWrapper = FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper();
 
        Field limit = new Field();
        limit.setName("limit");
        limit.setVisibility(JavaVisibility.PRIVATE);
        limit.setType(integerWrapper);
        topLevelClass.addField(limit);
 
        Method setLimit = new Method();
        setLimit.setVisibility(JavaVisibility.PUBLIC);
        setLimit.setName("setLimit");
        setLimit.addParameter(new Parameter(integerWrapper, "limit"));
        setLimit.addBodyLine("this.limit = limit;");
        topLevelClass.addMethod(setLimit);
 
        Method getLimit = new Method();
        getLimit.setVisibility(JavaVisibility.PUBLIC);
        getLimit.setReturnType(integerWrapper);
        getLimit.setName("getLimit");
        getLimit.addBodyLine("return limit;");
        topLevelClass.addMethod(getLimit);
 
        Field offset = new Field();
        offset.setName("offset");
        offset.setVisibility(JavaVisibility.PRIVATE);
        offset.setType(integerWrapper);
        topLevelClass.addField(offset);
 
        Method setOffset = new Method();
        setOffset.setVisibility(JavaVisibility.PUBLIC);
        setOffset.setName("setOffset");
        setOffset.addParameter(new Parameter(integerWrapper, "offset"));
        setOffset.addBodyLine("this.offset = offset;");
        topLevelClass.addMethod(setOffset);
 
        Method getOffset = new Method();
        getOffset.setVisibility(JavaVisibility.PUBLIC);
        getOffset.setReturnType(integerWrapper);
        getOffset.setName("getOffset");
        getOffset.addBodyLine("return offset;");
        topLevelClass.addMethod(getOffset);
 
        return true;
    }
 
    /**
     * 为Mapper.xml的selectByExample添加limit
     */
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
 
        XmlElement ifLimitNotNullElement = new XmlElement("if");
        ifLimitNotNullElement.addAttribute(new Attribute("test", "limit != null"));
 
        XmlElement ifOffsetNotNullElement = new XmlElement("if");
        ifOffsetNotNullElement.addAttribute(new Attribute("test", "offset != null"));
        ifOffsetNotNullElement.addElement(new TextElement("limit ${offset}, ${limit}"));
        ifLimitNotNullElement.addElement(ifOffsetNotNullElement);
 
        XmlElement ifOffsetNullElement = new XmlElement("if");
        ifOffsetNullElement.addAttribute(new Attribute("test", "offset == null"));
        ifOffsetNullElement.addElement(new TextElement("limit ${limit}"));
        ifLimitNotNullElement.addElement(ifOffsetNullElement);
 
        element.addElement(ifLimitNotNullElement);
 
        return true;
    }
}
```

### 插件的使用

在MyBatis Generator配置文件中配置plugin：

复制代码

```xml
<context id="mysqlgenerator" targetRuntime="MyBatis3">
    <plugin type="com.xxg.mybatis.plugins.MySQLLimitPlugin"></plugin>
    ...
</context>
```

如果直接加上以上配置运行**mvn mybatis-generator:generate**肯定会出现找不到这个插件的错误：

> java.lang.ClassNotFoundException: com.xxg.mybatis.plugins.MySQLLimitPlugin

为了方便大家的使用，我已经把插件打包上传到GitHub，可以在pom.xml直接依赖使用：



## 使用生成的代码分页

复制代码

```java
xxExample example = new XxxExample();
...
example.setLimit(10); // page size limit
example.setOffset(20); // offset
List<Xxx> list = xxxMapper.selectByExample(example);
```

以上代码运行时执行的SQL是：select ... limit 20, 10。

复制代码

```java
XxxExample example = new XxxExample();
...
example.setLimit(10); // limit
List<Xxx> list = xxxMapper.selectByExample(example);
```

以上代码运行时执行的SQL是：select ... limit 10。
