/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package entity_fields

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.dataeclipselink.DataEclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.UiProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        DataEclipselinkConfiguration, UiTestConfiguration])
class EntityFieldPropertiesTest extends Specification {

    @Autowired
    UiProperties properties

    def "properties are parsed from file"() {
        expect:
        properties.getEntityFieldType().get('test_SomeEntity') == 'entityComboBox'

        def actionIds = properties.getEntityFieldActions().get('test_SomeEntity')
        actionIds instanceof List
        actionIds.containsAll('entity_lookup', 'entity_open', 'entity_clear')
    }
}
