package dev.jeka.ide.intellij.platform;

import com.intellij.execution.application.JvmMainMethodRunConfigurationOptions;

import java.util.Objects;

public class JekaRunConfigurationOptions extends JvmMainMethodRunConfigurationOptions {

    String methodName = "";

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JekaRunConfigurationOptions)) return false;
        if (!super.equals(o)) return false;
        JekaRunConfigurationOptions that = (JekaRunConfigurationOptions) o;
        return getMethodName().equals(that.getMethodName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMethodName());
    }
}
