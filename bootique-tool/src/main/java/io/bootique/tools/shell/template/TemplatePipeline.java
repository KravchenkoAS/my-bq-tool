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

package io.bootique.tools.shell.template;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import io.bootique.tools.shell.template.processor.TemplateProcessor;

public class TemplatePipeline {

    private final List<String> sources;
    private final TemplateProcessor processor;
    private final TemplateLoader loader;
    private final TemplateSaver saver;
    private final Predicate<Properties> filter;

    private TemplatePipeline(List<String> sources, TemplateProcessor processor,
                             TemplateLoader loader, TemplateSaver saver, Predicate<Properties> filter) {
        this.sources = sources;
        this.processor = processor;
        this.loader = loader;
        this.saver = saver;
        this.filter = filter;
    }

    public void process(Properties properties) {
        sources.stream()
                .filter(source -> filter.test(properties))
                .map(source -> loader.load(source, properties))
                .map(template -> processor.process(template, properties))
                .forEach(template -> saver.save(template, properties));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<String> sources;
        private TemplateProcessor processor;
        private TemplateLoader loader;
        private TemplateSaver saver;
        private Predicate<Properties> filter;

        private Builder() {
            sources = new ArrayList<>();
        }

        public Builder source(String source) {
            sources.add(source);
            return this;
        }

        public Builder processor(TemplateProcessor processor) {
            if(this.processor == null) {
                this.processor = processor;
                return this;
            }

            this.processor = this.processor.andThen(processor);
            return this;
        }

        public Builder loader(TemplateLoader loader) {
            this.loader = loader;
            return this;
        }

        public Builder saver(TemplateSaver saver) {
            this.saver = saver;
            return this;
        }

        public Builder filter(Predicate<Properties> filter) {
            this.filter = filter;
            return this;
        }

        public TemplatePipeline build() {
            if(sources.isEmpty()) {
                throw new TemplateException("No sources set for template pipeline");
            }
            if(processor == null) {
                processor = (t, p) -> t;
            }
            if(loader == null) {
                loader = new TemplateResourceLoader();
            }
            if(saver == null) {
                saver = new TemplateFileSaver();
            }
            if(filter == null) {
                filter = properties -> true;
            }
            return new TemplatePipeline(sources, processor, loader, saver, filter);
        }

    }
}
