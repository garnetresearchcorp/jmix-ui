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

package responsive_grid_layout

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.ui.UiComponents
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.TextField
import io.jmix.ui.component.impl.WebResponsiveGridLayout
import io.jmix.ui.sys.WebUiComponents
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import responsive_grid_layout.component.TestResponsiveGridLayout
import responsive_grid_layout.screen.ResponsiveGridLayoutTestScreen
import test_support.UiTestConfiguration

import static io.jmix.ui.component.ResponsiveGridLayout.*

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration, UiTestConfiguration])
class ResponsiveGridLayoutTest extends ScreenSpecification {

    @Autowired
    protected UiComponents uiComponents

    @Override
    void setup() {
        exportScreensPackages(["responsive_grid_layout"])
        ((WebUiComponents) uiComponents).register(NAME, TestResponsiveGridLayout)
    }

    @Override
    void cleanup() {
        ((WebUiComponents) uiComponents).register(NAME, WebResponsiveGridLayout)
    }

    def "load component from XML"() {
        showTestMainScreen()

        def screen = getScreens().create(ResponsiveGridLayoutTestScreen)
        screen.show()

        def gridLayout = screen.responsiveGridLayout

        when:
        def rows = gridLayout.getRows()

        then:
        rows.size() == 2

        when:
        def firstRow = rows.get(0)
        def secondRow = rows.get(1)

        then:
        firstRow.getColumns().size() == 3
        secondRow.getColumns().size() == 2

        when:
        def column = firstRow.getColumns().get(0)
        def columnsMap = column.getColumns()

        then:
        columnsMap.size() == 1

        and:
        columnsMap.get(Breakpoint.SM) == ColumnsValue.DEFAULT
    }

    def "change grid settings after screen is opened"() {
        showTestMainScreen()

        def screen = getScreens().create(ResponsiveGridLayoutTestScreen)
        screen.show()

        def gridLayout = screen.responsiveGridLayout as TestResponsiveGridLayout
        gridLayout.setInitialized(true)

        when: "set container type"
        gridLayout.setContainerType(ContainerType.FLUID)

        then: "exception is thrown"
        thrown(IllegalStateException)

        when: "add row"
        def rowsCount = gridLayout.getRows().size()
        gridLayout.addRow()

        then: "exception is thrown"
        thrown(IllegalStateException)

        and: "rows count remains the same"
        rowsCount == gridLayout.getRows().size()

        when: "remove row"
        gridLayout.removeAllRows()

        then: "exception is thrown"
        thrown(IllegalStateException)

        and: "rows count remains the same"
        rowsCount == gridLayout.getRows().size()
    }

    def "change grid's row settings after screen is opened"() {
        showTestMainScreen()

        def screen = getScreens().create(ResponsiveGridLayoutTestScreen)
        screen.show()

        def gridLayout = screen.responsiveGridLayout as TestResponsiveGridLayout
        gridLayout.setInitialized(true)

        def row = gridLayout.getRows().get(0)
        def columnsCount = row.getColumns().size()

        when: "add column"
        row.addColumn()

        then: "exception is thrown"
        thrown(IllegalStateException)

        and: "columns count remains the same"
        columnsCount == row.getColumns().size()

        when: "remove column"
        row.removeAllColumns()

        then: "exception is thrown"
        thrown(IllegalStateException)

        and: "columns count remains the same"
        columnsCount == row.getColumns().size()

        when: "set row columns"
        row.setRowColumns(RowColumnsValue.columns(2))

        then: "exception is thrown"
        thrown(IllegalStateException)

        when: "set align items"
        row.setAlignItems(AlignItems.END)

        then: "exception is thrown"
        thrown(IllegalStateException)

        when: "set justify content"
        row.setJustifyContent(JustifyContent.END)

        then: "exception is thrown"
        thrown(IllegalStateException)
    }

    def "change grid's column settings after screen is opened"() {
        showTestMainScreen()

        def screen = getScreens().create(ResponsiveGridLayoutTestScreen)
        screen.show()

        def gridLayout = screen.responsiveGridLayout as TestResponsiveGridLayout
        gridLayout.setInitialized(true)

        def row = gridLayout.getRows().get(0)
        def column = row.getColumns().get(0)

        when: "set columns"
        column.setColumns(ColumnsValue.AUTO)

        then: "exception is thrown"
        thrown(IllegalStateException)

        when: "set align self"
        column.setAlignSelf(AlignSelf.CENTER)

        then: "exception is thrown"
        thrown(IllegalStateException)

        when: "set order"
        column.setOrder(OrderValue.LAST)

        then: "exception is thrown"
        thrown(IllegalStateException)

        when: "set offset"
        column.setOffset(OffsetValue.columns(2))

        then: "exception is thrown"
        thrown(IllegalStateException)

        when: "set component"
        def component = uiComponents.create(TextField.NAME)
        column.setComponent(component)

        then: "no exception is thrown"
        noExceptionThrown()
    }
}
