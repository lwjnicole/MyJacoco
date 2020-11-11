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
   打包成功之后，会在项目目录下(jacoco\jacoco\target)生成zip包,这个zip包就是release包，包含我们需要的jar包(jacocoagent.jar)
   
# 四、代码分支比较差分说明：
   以我的[测试代码](https://github.com/lwjnicole/JacocoTest.git)为例，下载测试代码，假设我们新开发的分支为test分支，master分支为基线分支。测试目的是测试新开发的test分支代码是否被全部测试覆盖到。我们测试时，首先把将测试代码打包，然后发布启动服务，使用手动或者自动的方式，完成测试(例如：接口自动化测试)；
   
   启动方式为在测试代码项目目录下执行命令：``java -javaagent:jacocoagent.jar=includes=*,output=tcpserver,port=9100,address=127.0.0.1 -jar target/demo-0.0.1-SNAPSHOT.jar``
   
# 五、下载jacoco.exec（这里主要考虑到集成到Devops平台，通过代码获取）
   我们做完测试之后，就通过以下方式来下载jacoco.exec文件：
   ```Java
   /*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.myexamples;

import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * 用于生成exec文件
 */
public class ExecutionDataClient {
    private static final String DESTFILE = "E:\\workspace\\JacocoTest\\jacoco.exec";// 导出的文件路径

    private static final String ADDRESS = "127.0.0.1";// 配置的Jacoco的IP

    private static final int PORT = 9100;// Jacoco监听的端口

    public static void main(final String[] args) throws IOException {
        final FileOutputStream localFile = new FileOutputStream(DESTFILE);
        final ExecutionDataWriter localWriter = new ExecutionDataWriter(
                localFile);

        // 连接Jacoco服务
        final Socket socket = new Socket(InetAddress.getByName(ADDRESS), PORT);
        final RemoteControlWriter writer = new RemoteControlWriter(
                socket.getOutputStream());
        final RemoteControlReader reader = new RemoteControlReader(
                socket.getInputStream());
        reader.setSessionInfoVisitor(localWriter);
        reader.setExecutionDataVisitor(localWriter);

        // 发送Dump命令，获取Exec数据
        writer.visitDumpCommand(true, false);
        if (!reader.read()) {
            throw new IOException("Socket closed unexpectedly.");
        }

        socket.close();
        localFile.close();
    }

    private ExecutionDataClient() {
    }
}
```
