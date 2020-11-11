JaCoCo Java Code Coverage Library
=================================

[![Build Status](https://travis-ci.org/jacoco/jacoco.svg?branch=master)](https://travis-ci.org/jacoco/jacoco)
[![Build status](https://ci.appveyor.com/api/projects/status/g28egytv4tb898d7/branch/master?svg=true)](https://ci.appveyor.com/project/JaCoCo/jacoco/branch/master)
[![Maven Central](https://img.shields.io/maven-central/v/org.jacoco/jacoco.svg)](http://search.maven.org/#search|ga|1|g%3Aorg.jacoco)

JaCoCo is a free Java code coverage library distributed under the Eclipse Public
License. Check the [project homepage](http://www.jacoco.org/jacoco)
for downloads, documentation and feedback.

Please use our [mailing list](https://groups.google.com/forum/?fromgroups=#!forum/jacoco)
for questions regarding JaCoCo which are not already covered by the
[extensive documentation](http://www.jacoco.org/jacoco/trunk/doc/).

Jacoco二次开发
-------------------------------------------------------------------------
JaCoCo二次开发基于Git分支差分实现增量代码覆盖率

# 一、原理：  
   通过使用org.eclipse.jgit比较新旧分支代码差分，取得变更行信息，生成报告时高亮线上变更行信息，未检出变更行不做处理。从而达到，增量显示代码覆盖率的目的。  
# 二、实现的功能：  
   * 分支与master对比；  
   * 分支与分支之间对比；  
   * tag与tag之间对比；  
   * 支持增量代码覆盖率统计和全量代码覆盖率统计；  
# 三、使用方法：  
   下载我的源代码，然后在命令行中执行maven打包命令：``mvn clean package -Dmaven.javadoc.test=true -Dmaven.test.skip=true``  
   打包成功之后，会在项目目录下(jacoco\jacoco\target)生成zip包
   
