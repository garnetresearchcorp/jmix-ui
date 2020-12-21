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

package io.jmix.ui.app.filter.condition.validation;

import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.component.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("ui_ValueComponentNameValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Internal
public class ValueComponentNameValidator implements Validator<String> {

    protected UiComponents uiComponents;
    protected Messages messages;

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void accept(String valueComponentName) throws ValidationException {
        if (!uiComponents.isComponentRegistered(valueComponentName)) {
            throw new ValidationException(messages.formatMessage(ValueComponentNameValidator.class,
                    "valueComponentNameValidator.validationMessage", valueComponentName));
        }
    }
}