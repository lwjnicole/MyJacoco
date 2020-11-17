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
   打包成功之后，会在项目目录下(jacoco\jacoco\target)生成zip包，这个zip包就是release包，包含我们需要的jar包(jacocoagent.jar)
   
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

# 六、基于Git分支差分解析exec文件生成覆盖率报告：
   在做完第五步之后，我们通过以下方式来解析生成的exec文件，从而生成覆盖率报告：
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

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.internal.diff.GitAdapter;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;

/**
 * 用于根据exec文件生成增量覆盖率报告
 */
public class ReportGenerator {

    private final String title;
    private final File executionDataFile;
    private final File classesDirectory;
    private final File sourceDirectory;
    private final File reportDirectory;
    private ExecFileLoader execFileLoader;

    public ReportGenerator(final File projectDirectory) {
        this.title = projectDirectory.getName();
        this.executionDataFile = new File(projectDirectory, "jacoco.exec"); // 第一步生成的exec的文件
        this.classesDirectory = new File(projectDirectory, "target/classes"); // 目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录
        this.sourceDirectory = new File(projectDirectory, "src/main/java"); // 源码目录
        this.reportDirectory = new File(projectDirectory, "coveragereport"); // 要保存报告的地址
    }

    public void create() throws IOException {
        loadExecutionData();
        final IBundleCoverage bundleCoverage = analyzeStructure();
        createReport(bundleCoverage);
    }

    private void createReport(final IBundleCoverage bundleCoverage)
            throws IOException {

        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter
                .createVisitor(new FileMultiReportOutput(reportDirectory));

        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
                execFileLoader.getExecutionDataStore().getContents());

        visitor.visitBundle(bundleCoverage,
                new DirectorySourceFileLocator(sourceDirectory, "utf-8", 4));

        // //多源码路径
        // MultiSourceFileLocator sourceLocator = new MultiSourceFileLocator(4);
        // sourceLocator.add( new DirectorySourceFileLocator(sourceDir1,
        // "utf-8", 4));
        // sourceLocator.add( new DirectorySourceFileLocator(sourceDir2,
        // "utf-8", 4));
        // sourceLocator.add( new DirectorySourceFileLocator(sourceDir3,
        // "utf-8", 4));
        // visitor.visitBundle(bundleCoverage,sourceLocator);

        visitor.visitEnd();
    }

    private void loadExecutionData() throws IOException {
        execFileLoader = new ExecFileLoader();
        execFileLoader.load(executionDataFile);
    }

    private IBundleCoverage analyzeStructure() throws IOException {
        // git登录授权
        GitAdapter.setCredentialsProvider("2570908152", "mima2570908152");
        // 全量覆盖
//		 final CoverageBuilder coverageBuilder = new CoverageBuilder();

        // 基于分支比较覆盖，参数1：本地仓库，参数2：开发分支（预发分支），参数3：基线分支(不传时默认为master)
        // 本地Git路径，新分支 第三个参数不传时默认比较master，传参数为待比较的基线分支
        final CoverageBuilder coverageBuilder = new CoverageBuilder(
                "E:\\workspace\\JacocoTest", "test");

        // 基于Tag比较的覆盖 参数1：本地仓库，参数2：代码分支，参数3：新Tag(预发版本)，参数4：基线Tag（变更前的版本）
        // final CoverageBuilder coverageBuilder = new
        // CoverageBuilder("E:\workspace\JacocoTest","test","v004","v003");

        final Analyzer analyzer = new Analyzer(
                execFileLoader.getExecutionDataStore(), coverageBuilder);

        analyzer.analyzeAll(classesDirectory);

        return coverageBuilder.getBundle(title);
    }

    public static void main(final String[] args) throws IOException {
        final ReportGenerator generator = new ReportGenerator(
                new File("E:\\workspace\\JacocoTest"));
        generator.create();
    }
}
 ```
 执行完后就生成报告了，通过第五步和第六步的结合，我们就可以实现随时到处exec文件，解析exec文件生成覆盖率报告，查看我们的测试覆盖率情况了。通过这种代码的方式来导出exec文件、解析exec文件生成报告是不需要启动停止服务的。

## 七、报告展示：
覆盖率概览：
![JacocoTest1图片](https://github.com/lwjnicole/JacocoTest/blob/test/JacocoTest1.jpg)

![JacocoTest2图片](https://github.com/lwjnicole/JacocoTest/blob/test/JacocoTest2.jpg)

其中绿色表示的是覆盖的代码，红色表示的是没有被覆盖到的代码
![JacocoTest3图片](https://github.com/lwjnicole/JacocoTest/blob/test/JacocoTest3.jpg)

## 八、传参说明：
1. 全量覆盖率统计：``final CoverageBuilder coverageBuilder = new CoverageBuilder();``

2. 增量覆盖率统计：
   + 基于分支对比：``final CoverageBuilder coverageBuilder = new CoverageBuilder("E:\\workspace\\JacocoTest", "test");``  
   
   参数说明：
      + 参数1：本地仓库（本地代码的git路径）
      + 参数2：开发分支（预发分支，即新分支）
      + 参数3：基线分支(不传时默认为master，传参数为待比较的基线分支)
      
   + 基于tag号对比：``final CoverageBuilder coverageBuilder = new CoverageBuilder("E:\workspace\JacocoTest","test","v004","v003");`` 
   
   参数说明：
      + 参数1：本地仓库（本地代码的git路径）
      + 参数2：开发分支（预发分支，即新分支）
      + 参数3：新Tag（预发版本）
      + 参数4：基线Tag（变更前的版本）
