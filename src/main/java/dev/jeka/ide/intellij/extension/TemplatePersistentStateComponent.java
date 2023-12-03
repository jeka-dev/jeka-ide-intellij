package dev.jeka.ide.intellij.extension;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Storage;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Stores customized templates.
 * The builtin templates are hardcoded and immutables.
 */
@com.intellij.openapi.components.State(
        name = "dev.jeka.ide.templates",
        storages = @Storage("dev.jeka.ide.templates.xml")
)
@Service
public final class TemplatePersistentStateComponent implements PersistentStateComponent<Element> {

    @Setter
    private List<JekaTemplate> customizedTemplates = new LinkedList<>();

    public static TemplatePersistentStateComponent getInstance() {
        return ApplicationManager.getApplication().getService(TemplatePersistentStateComponent.class);
    }

    public List<JekaTemplate> getCustomizedTemplates() {
        return customizedTemplates;
    }

    public List<JekaTemplate> getAllTemplates() {
        List<JekaTemplate> result = new LinkedList<>();
        result.addAll(JekaTemplate.builtins());
        result.addAll(getCustomizedTemplates());
        return result;
    }


    @Override
    public Element getState() {
        Element root = new Element("templates");
        for (JekaTemplate template : customizedTemplates) {
            Element templateEl = new Element("template");
            root.addContent(templateEl);
            templateEl.addContent(new Element("name").setText(template.getName()));
            templateEl.addContent(new Element("cmd").setText(template.getCommandArgs()));
            templateEl.addContent(new Element("description").setText(template.getDescription()));
            templateEl.addContent(new Element("builtin").setText(Boolean.toString(template.isBuiltin())));
        }
        return root;
    }

    @Override
    public void loadState(@NotNull Element state) {
        List<Element> templateEls = state.getChildren();
        List<JekaTemplate> result = new LinkedList<>();
        for (Element templateEl : templateEls) {
            JekaTemplate template = JekaTemplate.builder()
                    .name(templateEl.getChildText("name"))
                    .commandArgs(templateEl.getChildText("cmd"))
                    .description(templateEl.getChildText("description"))
                    .builtin("true".equals(templateEl.getChildText("builtin")))
                    .build();
            result.add(template);
        }
        customizedTemplates.clear();
        customizedTemplates.addAll(result);
    }



}
