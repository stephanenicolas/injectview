/*
 * Copyright 2009 Michael Burton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.github.stephanenicolas.injectview;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that an {@code Activity} or siblings will use a given layout as
 * its content view.
 *
 * The value corresponds to the id of the view.<br />
 *
 * Usage example:<br />
 * {@code @ContentView(R.layout.main_layout) class MyActivity extends ... {}<br/>
 *
 * @author Mike Burton
 */
@Retention(RUNTIME)
@Target( { ElementType.TYPE })
public @interface ContentView {
    int value();
}