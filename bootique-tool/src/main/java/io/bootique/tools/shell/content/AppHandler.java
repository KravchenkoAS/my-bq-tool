/*
 *   Licensed to ObjectStyle LLC under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ObjectStyle LLC licenses
 *   this file to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package io.bootique.tools.shell.content;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.bootique.command.CommandOutcome;
import io.bootique.tools.shell.template.EmptyTemplateLoader;
import io.bootique.tools.shell.template.Properties;
import io.bootique.tools.shell.template.TemplateDirOnlySaver;
import io.bootique.tools.shell.template.TemplatePipeline;
import io.bootique.tools.shell.template.processor.BQModuleProviderProcessor;
import io.bootique.tools.shell.template.processor.JavaPackageProcessor;

public abstract class AppHandler extends ContentHandler {

    public AppHandler() {
        // java sources
        addPipeline(TemplatePipeline.builder()
                .source("src/main/java/example/Application.java")
                .source("src/main/java/example/ApplicationModuleProvider.java")
                .source("src/test/java/example/ApplicationTest.java")
                .source("src/test/java/example/ApplicationModuleProviderTest.java")
                .processor(new JavaPackageProcessor())
        );

        // folders
        addPipeline(TemplatePipeline.builder()
                .source("src/main/resources")
                .source("src/test/resources")
                .loader(new EmptyTemplateLoader())
                .saver(new TemplateDirOnlySaver())
        );

        addPipeline(TemplatePipeline.builder()
                .source("src/main/resources/META-INF/services/io.bootique.BQModuleProvider")
                .processor(new BQModuleProviderProcessor())
        );

        // .gitignore
        addPipeline(TemplatePipeline.builder()
                .source("gitignore")
                .processor((tpl, p) -> tpl.withPath(tpl.getPath().getParent().resolve(".gitignore")))
        );
    }

    protected abstract String getBuildSystemName();

    protected abstract Properties getProperties(NameComponents components, Path outputRoot);

    @Override
    public CommandOutcome handle(NameComponents components) {
        log("Generating new " + getBuildSystemName() + " project @|bold " + components.getName() + "|@ ...");

        Path outputRoot = shell.workingDir().resolve(components.getName());
        if(Files.exists(outputRoot)) {
            return CommandOutcome.failed(-1, "Directory '" + components.getName() + "' already exists");
        }

        Properties properties = getProperties(components, outputRoot);
        pipelines.forEach(p -> p.process(properties));

        log("done.");
        return CommandOutcome.succeeded();
    }
}
