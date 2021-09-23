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

package io.jmix.uiexport.action;

import io.jmix.core.Messages;
import io.jmix.ui.Dialogs;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.uiexport.exporter.ExportMode;
import io.jmix.uiexport.exporter.TableExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Base action for export table content with defined exporter.
 * <code>tableExporter</code> is required for this action
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 */
@SuppressWarnings("rawtypes")
public class ExportAction extends ListAction implements ApplicationContextAware {

    public static final String ID = "export";

    protected ApplicationContext applicationContext;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Downloader downloader;

    protected TableExporter tableExporter;

    public ExportAction(String id) {
        this(id, null);
    }

    public ExportAction() {
        this(ID);
    }

    public ExportAction(String id, @Nullable String shortcut) {
        super(id, shortcut);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Sets the table exporter instance
     */
    public void setTableExporter(TableExporter tableExporter) {
        this.tableExporter = tableExporter;
        this.caption = tableExporter.getCaption() != null ? tableExporter.getCaption() : this.caption;
    }

    /**
     * Autowire table exporter instance by exporter class
     */
    public <T> T withExporter(Class<T> exporterClass) {
        setTableExporter((TableExporter) applicationContext.getBean(exporterClass));
        return (T) tableExporter;
    }

    /**
     * Adds a function to get value from the column.
     *
     * @param columnId       column id
     * @param exportProvider export provider function
     */
    public void addColumnExportProvider(String columnId, Function<TableExporter.ExportColumnContext, Object> exportProvider) {
        if (tableExporter != null) {
            tableExporter.addColumnExportProvider(columnId, exportProvider);
        }
    }

    /**
     * Removes an export provider function by column id.
     *
     * @param columnId column id
     */
    public void removeColumnExportProvider(String columnId) {
        if (tableExporter != null) {
            tableExporter.removeColumnExportProvider(columnId);
        }
    }

    /**
     * @param columnId column id
     * @return export provider function for the column id
     */
    @Nullable
    public Function<TableExporter.ExportColumnContext, Object> getColumnExportProvider(String columnId) {
        return tableExporter != null
                ? tableExporter.getColumnExportProvider(columnId)
                : null;
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    protected void execute() {
        if (tableExporter == null) {
            throw new IllegalStateException("Table exporter is not defined");
        }
        if (needExportAll()) {
            doExport(ExportMode.ALL);

        } else {
            AbstractAction exportSelectedAction = new AbstractAction("actions.export.SELECTED_ROWS", Status.PRIMARY) {
                @Override
                public void actionPerform(Component component) {
                    doExport(ExportMode.SELECTED);
                }
            };
            exportSelectedAction.setCaption(getMessage(exportSelectedAction.getId()));

            AbstractAction exportAllAction = new AbstractAction("actions.export.ALL_ROWS") {
                @Override
                public void actionPerform(Component component) {
                    doExport(ExportMode.ALL);
                }
            };
            exportAllAction.setCaption(getMessage(exportAllAction.getId()));

            Action[] actions = new Action[]{
                    exportSelectedAction,
                    exportAllAction,
                    new DialogAction(DialogAction.Type.CANCEL)
            };

            Dialogs dialogs = ComponentsHelper.getScreenContext(target).getDialogs();

            dialogs.createOptionDialog()
                    .withCaption(getMessage("actions.exportSelectedTitle"))
                    .withMessage(getMessage("actions.exportSelectedCaption"))
                    .withActions(actions)
                    .show();
        }
    }

    protected void doExport(ExportMode exportMode) {
        if (getTarget() instanceof Table) {
            tableExporter.exportTable(downloader, (Table<Object>) getTarget(), exportMode);
        } else if (getTarget() instanceof DataGrid) {
            tableExporter.exportDataGrid(downloader, (DataGrid<Object>) getTarget(), exportMode);
        } else {
            throw new UnsupportedOperationException("Unsupported component for export");
        }
    }

    protected String getMessage(String id) {
        return messages.getMessage(id);
    }

    protected boolean needExportAll() {
        if (target.getSelected().isEmpty()
                || !(target.getItems() instanceof ContainerDataUnit)) {
            return true;
        }
        CollectionContainer container = ((ContainerDataUnit) target.getItems()).getContainer();
        return container != null && container.getItems().size() <= 1;
    }
}
