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

package io.jmix.ui.component;

/**
 * Component that makes a data binding to load data by pages. It contains current items count label
 * and navigation buttons (next, last etc).
 */
public interface SimplePagination extends PaginationComponent {

    String NAME = "simplePagination";

    /**
     * @return whether items count should be loaded automatically
     */
    boolean isAutoLoad();

    /**
     * Sets whether items count should be loaded automatically.
     *
     * @param autoLoad pass true to enable auto load, or false otherwise
     */
    void setAutoLoad(boolean autoLoad);
}