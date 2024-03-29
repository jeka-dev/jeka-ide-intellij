package dev.jeka.ide.intellij.panel;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import dev.jeka.core.api.system.JkLocator;
import dev.jeka.core.api.utils.JkUtilsPath;
import dev.jeka.ide.intellij.common.JekaDistributions;
import lombok.Getter;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AppSettingsComponent {

    @Getter
    private final JPanel mainPanel;

    @Getter
    private JBTextField distributionPathText = new ExtendableTextField();

    public AppSettingsComponent() {
        TextFieldWithBrowseButton textFieldWithBrowseButton = new TextFieldWithBrowseButton(distributionPathText);
        JPanel distributionPanel = UI.PanelFactory.panel(textFieldWithBrowseButton)
                .withLabel("Jeka distribution:")
                .withComment("The location of the distribution for running JeKa when no wrapper is used." +
                        "<br/>This distribution is also used to generate Jeka modules from scratch.")
                .createPanel();
        textFieldWithBrowseButton.addBrowseFolderListener(
                "Choose JeKa Distribution",
                "JeKa version picked from Maven central repository.",
                null,
                fileChooserDescriptor()
                );
        JPanel installPanel = UI.PanelFactory.panel(comboAndButton())
                .withLabel("Install distribution:")
                .withComment("Install a Jeka distribution from Maven central repository.")
                .createPanel();
        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(distributionPanel)
                .addComponent(installPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private static FileChooserDescriptor fileChooserDescriptor() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, true,
                true, true, false, false) {

            public void validateSelectedFiles(VirtualFile[] files) {
                for (VirtualFile virtualFile : files) {
                    Path dir = virtualFile.toNioPath();
                    if (!Files.isDirectory(dir)) {
                        throw new IllegalArgumentException("Please, select folder only.");
                    }
                    Path jekaJar = JkLocator.getJekaJarPath().getFileName();
                    if (!Files.exists(dir.resolve(jekaJar.getFileName()))) {
                        throw new IllegalArgumentException("Selected folder does not contain " + jekaJar);
                    }
                }
            }

            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                return true;
            }
        };
        Condition<VirtualFile> filter = file -> file.toNioPath().startsWith(JekaDistributions.getDistributionsDir());
        JkUtilsPath.createDirectories(JekaDistributions.getDistributionsDir());
        VirtualFile distribs = LocalFileSystem.getInstance().findFileByNioFile(JekaDistributions.getDistributionsDir());
        VirtualFile root = LocalFileSystem.getInstance().findFileByNioFile(Paths.get("/"));
        List<VirtualFile> roots = new LinkedList<>();
        roots.add(distribs);
        roots.add(root);
        fileChooserDescriptor.withFileFilter(filter);
        fileChooserDescriptor.setRoots(roots);
        return fileChooserDescriptor;
    }

    private ComboBox<String> versionsCombo() {
        ComboBox<String> comboBox = new ComboBox<>();
        List<String> versions = JekaDistributions.searchVersionsSortedByDesc();
        for (String version : versions) {
            comboBox.addItem(version);
        }
        return comboBox;
    }

    private JButton installButton(ComboBox<String> combo) {
        JButton button = new JButton();
        button.setText("Install");
        button.addActionListener(event -> {
            AtomicReference<Path> resultPath = new AtomicReference<>();
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                    () -> {
                        Path result = JekaDistributions.install(combo.getItem());
                        resultPath.set(result);
                        NotificationGroupManager.getInstance()
                                .getNotificationGroup("jeka.notifGroup")
                                .createNotification("Distribution  " + combo.getItem() + " installed. ",
                                        NotificationType.INFORMATION)
                                .notify(null);

                    },
                    "Installing JeKa " + combo.getItem(),
                    true,
                    null
            );
            distributionPathText.setText(resultPath.get().toString());
            distributionPathText.grabFocus();
        });
        return button;
    }

    private JComponent comboAndButton() {
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
        panel.setLayout(layout);
        ComboBox<String> versionCombo = versionsCombo();
        JButton installButton = installButton(versionCombo);
        panel.add(versionCombo);
        panel.add(installButton);
        return panel;
    }

    public JComponent getPreferredFocusedComponent() {
        return distributionPathText;
    }

}
