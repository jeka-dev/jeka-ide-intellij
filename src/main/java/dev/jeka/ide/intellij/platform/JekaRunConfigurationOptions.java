/*
 * Copyright 2018-2019 original author or authors
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

package dev.jeka.ide.intellij.platform;

import com.intellij.execution.application.JvmMainMethodRunConfigurationOptions;

import java.util.Objects;

/**
 * @author Jerome Angibaud
 */
public class JekaRunConfigurationOptions extends JvmMainMethodRunConfigurationOptions {

    String myMethodName = "";

    public String getMyMethodName() {
        return myMethodName;
    }

    public void setMyMethodName(String myMethodName) {
        this.myMethodName = myMethodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JekaRunConfigurationOptions)) return false;
        if (!super.equals(o)) return false;
        JekaRunConfigurationOptions that = (JekaRunConfigurationOptions) o;
        return getMyMethodName().equals(that.getMyMethodName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMyMethodName());
    }
}
