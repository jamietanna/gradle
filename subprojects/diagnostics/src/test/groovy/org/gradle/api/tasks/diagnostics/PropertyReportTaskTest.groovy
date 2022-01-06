/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.tasks.diagnostics

import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.diagnostics.internal.PropertyReportRenderer
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.util.TestUtil
import org.junit.Rule
import spock.lang.Specification

class PropertyReportTaskTest extends Specification {
    private ProjectInternal project = Mock()
    private PropertyReportRenderer renderer = Mock()
    private PropertyReportTask task

    @Rule
    public TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider(getClass())

    def setup() {
        _ * project.absoluteProjectPath("list") >> ":path"
        _ * project.convention >> null

        task = TestUtil.create(temporaryFolder).task(PropertyReportTask.class)
        task.setRenderer(renderer)
    }

    def passesEachProjectPropertyToRenderer() {
        when:
        task.generate(project)

        then:
        1 * project.properties >> ["b": "value2", "a": "value1"]
        1 * renderer.addProperty("a", "value1")
        1 * renderer.addProperty("b", "value2")
    }

    def doesNotShowContentsOfThePropertiesProperty() {
        when:
        task.generate(project)

        then:
        1 * project.properties >> ["prop": "value", "properties": "prop"]
        1 * renderer.addProperty("prop", "value")
        1 * renderer.addProperty("properties", "{...}")
    }

    def "when a specific property is requested, only that property is requested"() {
        when:
        task.setProperty("prop")
        task.generate(project)

        then:
        1 * project.properties >> ["prop": "value", "properties": "prop"]
        1 * renderer.addProperty("prop", "value")
        0 * renderer.addProperty("properties", "{...}")
    }

    def "does not show contents of the properties property when requested"() {
        when:
        task.setProperty("properties")
        task.generate(project)

        then:
        1 * project.properties >> ["prop": "value", "properties": "prop"]
        1 * renderer.addProperty("properties", "{...}")
    }


    def "when a specific property is requested, but isn't found"() {
        when:
        task.setProperty("not-found")
        task.generate(project)

        then:
        1 * project.properties >> ["properties": "prop"]
        1 * renderer.addProperty("not-found", null)
    }
}
