# [kTest](README.md) :: Parent POM / BOM

This module contains only maven POM file with all kTest modules and their dependencies described in dependencyManagement
section. As bonus all versions are written in properties.

## Usage

### Maven

There are two possible ways to use this artifact in your maven project:
 
1. #### As Parent POM

You can use maven's POM inheritance mechanism:

```xml
<parent>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-pom</artifactId>
    <version>VERSION</version>
    <relativePath />
</parent>
```

2. #### As maven BOM

If you can't use inheritance (for ex. when you already have your company's base POM) you can just import it
through maven BOM:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>run.smt.ktest</groupId>
            <artifactId>ktest-pom</artifactId>
            <version>VERSION</version>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Gradle

You can use Spring `dependency-management-plugin`:

```groovy
dependencyManagement {
    imports {
        mavenBom 'run.smt.ktest:ktest-pom:VERSION'
    }
}
```
