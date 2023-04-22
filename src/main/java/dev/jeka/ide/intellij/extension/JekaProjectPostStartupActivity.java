package dev.jeka.ide.intellij.extension;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.startup.StartupActivity;
import dev.jeka.ide.intellij.extension.action.SyncAllImlAction;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JekaProjectPostStartupActivity implements ProjectActivity {

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        SyncAllImlAction.exec(project);
        return null;
    }
}
