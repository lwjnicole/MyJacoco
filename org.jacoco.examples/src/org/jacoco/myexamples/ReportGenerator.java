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
