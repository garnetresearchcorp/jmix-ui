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

package io.jmix.ui.settings.facet;

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.impl.AbstractFacet;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Screen.AfterDetachEvent;
import io.jmix.ui.screen.Screen.AfterShowEvent;
import io.jmix.ui.screen.Screen.BeforeShowEvent;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.settings.ScreenSettingsManager;
import io.jmix.ui.settings.ScreenSettings;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Internal
public class ScreenSettingsFacetImpl extends AbstractFacet implements ScreenSettingsFacet {

    private static final Logger log = LoggerFactory.getLogger(ScreenSettingsFacetImpl.class);

    protected Set<String> componentIds;

    protected boolean auto = false;

    protected ScreenSettings screenSettings;

    protected Consumer<SettingsContext> applySettingsDelegate;
    protected Consumer<SettingsContext> applyDataLoadingSettingsDelegate;
    protected Consumer<SettingsContext> saveSettingsDelegate;

    protected Subscription beforeShowSubscription;
    protected Subscription afterShowSubscription;
    protected Subscription afterDetachedSubscription;

    @Autowired(required = false)
    protected ScreenSettingsManager settingsManager;

    @Autowired
    protected BeanFactory beanFactory;

    @Override
    public ScreenSettings getSettings() {
        return screenSettings;
    }

    @Override
    public void applySettings() {
        Collection<Component> components = getComponents();

        applyScreenSettings(components);
    }

    @Override
    public void applySettings(Collection<Component> components) {
        Collection<Component> componentsToApply = filterByManagedComponents(components);

        applyScreenSettings(componentsToApply);
    }

    @Override
    public void applyDataLoadingSettings() {
        Collection<Component> components = getComponents();

        applyDataLoadingScreenSettings(components);
    }

    @Override
    public void applyDataLoadingSettings(Collection<Component> components) {
        Collection<Component> componentsToApply = filterByManagedComponents(components);

        applyDataLoadingScreenSettings(componentsToApply);
    }

    @Override
    public void saveSettings() {
        Collection<Component> components = getComponents();

        saveScreenSettings(components);
    }

    @Override
    public void saveSettings(Collection<Component> components) {
        Collection<Component> componentsToSave = filterByManagedComponents(components);

        saveScreenSettings(componentsToSave);
    }

    @Override
    public boolean isAuto() {
        return auto;
    }

    @Override
    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    @Override
    public void addComponentIds(String... ids) {
        if (componentIds == null) {
            componentIds = new HashSet<>();
        }

        componentIds.addAll(Arrays.asList(ids));
    }

    @Override
    public Set<String> getComponentIds() {
        if (componentIds == null) {
            return Collections.emptySet();
        }

        return componentIds;
    }

    @Override
    public Collection<Component> getComponents() {
        checkAttachedFrame();
        assert getOwner() != null;

        if (auto) {
            return getOwner().getComponents();
        }

        if (CollectionUtils.isNotEmpty(componentIds)) {
            return getOwner().getComponents().stream()
                    .filter(component -> componentIds.contains(component.getId()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public Consumer<SettingsContext> getApplySettingsDelegate() {
        return applySettingsDelegate;
    }

    @Override
    public void setApplySettingsDelegate(Consumer<SettingsContext> delegate) {
        this.applySettingsDelegate = delegate;
    }

    @Override
    public Consumer<SettingsContext> getApplyDataLoadingSettingsDelegate() {
        return applyDataLoadingSettingsDelegate;
    }

    @Override
    public void setApplyDataLoadingSettingsDelegate(Consumer<SettingsContext> delegate) {
        this.applyDataLoadingSettingsDelegate = delegate;
    }

    @Override
    public Consumer<SettingsContext> getSaveSettingsDelegate() {
        return saveSettingsDelegate;
    }

    @Override
    public void setSaveSettingsDelegate(Consumer<SettingsContext> delegate) {
        this.saveSettingsDelegate = delegate;
    }

    @Override
    public void setOwner(@Nullable Frame owner) {
        super.setOwner(owner);

        unsubscribe();
        screenSettings = null;

        if (getScreenOwner() != null) {
            screenSettings = beanFactory.getBean(ScreenSettings.class, getScreenOwner().getId());

            subscribe();

            if (!isSettingsEnabled()) {
                log.warn("ScreenSettingsFacet does not work for '{}' due to add-on "
                        + "that provides the ability to work with settings is not added", getScreenOwner().getId());
            }
        }
    }

    protected void subscribe() {
        checkAttachedFrame();

        //noinspection ConstantConditions
        EventHub screenEvents = UiControllerUtils.getEventHub(getScreenOwner());

        beforeShowSubscription = screenEvents.subscribe(BeforeShowEvent.class, this::onScreenBeforeShow);
        afterShowSubscription = screenEvents.subscribe(AfterShowEvent.class, this::onScreenAfterShow);
        afterDetachedSubscription = screenEvents.subscribe(AfterDetachEvent.class, this::onScreenAfterDetach);
    }

    protected void unsubscribe() {
        if (beforeShowSubscription != null) {
            beforeShowSubscription.remove();
            beforeShowSubscription = null;
        }
        if (afterShowSubscription != null) {
            afterShowSubscription.remove();
            afterShowSubscription = null;
        }
        if (afterDetachedSubscription != null) {
            afterDetachedSubscription.remove();
            afterDetachedSubscription = null;
        }
    }

    @Nullable
    protected Screen getScreenOwner() {
        Frame frame = getOwner();
        if (frame == null) {
            return null;
        }
        if (frame.getFrameOwner() instanceof ScreenFragment) {
            throw new IllegalStateException("ScreenSettingsFacet does not work in fragments");
        }

        return  (Screen) getOwner().getFrameOwner();
    }

    protected void onScreenBeforeShow(BeforeShowEvent event) {
        checkAttachedFrame();

        if (applyDataLoadingSettingsDelegate != null) {
            //noinspection ConstantConditions
            applyDataLoadingSettingsDelegate.accept(new SettingsContext(
                    getScreenOwner().getWindow(),
                    getComponents(),
                    screenSettings));
        } else {
            applyDataLoadingSettings();
        }
    }

    protected void onScreenAfterShow(AfterShowEvent event) {
        checkAttachedFrame();

        if (applySettingsDelegate != null) {
            //noinspection ConstantConditions
            applySettingsDelegate.accept(new SettingsContext(
                    getScreenOwner().getWindow(),
                    getComponents(),
                    screenSettings));
        } else {
            applySettings();
        }
    }

    protected void onScreenAfterDetach(AfterDetachEvent event) {
        checkAttachedFrame();

        if (saveSettingsDelegate != null) {
            //noinspection ConstantConditions
            saveSettingsDelegate.accept(new SettingsContext(
                    getScreenOwner().getWindow(),
                    getComponents(),
                    screenSettings));
        } else {
            saveSettings();
        }
    }

    protected void applyScreenSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.applySettings(components, screenSettings);
        }
    }

    protected void applyDataLoadingScreenSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.applyDataLoadingSettings(components, screenSettings);
        }
    }

    protected void saveScreenSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.saveSettings(components, screenSettings);
        }
    }

    protected boolean isSettingsEnabled() {
        return settingsManager != null;
    }

    protected Collection<Component> filterByManagedComponents(Collection<Component> components) {
        Collection<Component> attachedComponents = getComponents();
        return components.stream()
                .filter(attachedComponents::contains)
                .collect(Collectors.toList());
    }

    protected void checkAttachedFrame() {
        Frame frame = getOwner();
        if (frame == null) {
            throw new IllegalStateException("ScreenSettingsFacet is not attached to the screen");
        }
    }
}