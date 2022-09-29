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

@com.intellij.openapi.components.State(
        name = "dev.jeka.ide.templates",
        storages = @Storage("dev.jeka.ide.templates.xml")
)
@Service
public final class TemplatePersistentStateComponent implements PersistentStateComponent<Element> {

    @Setter
    private List<JekaTemplate> templates = new LinkedList<>();

    public static TemplatePersistentStateComponent getInstance() {
        return ApplicationManager.getApplication().getService(TemplatePersistentStateComponent.class);
    }

    public List<JekaTemplate> getTemplates() {
        if (templates.isEmpty()) {
            List<JekaTemplate> newList = new LinkedList<>(templates);
            newList.addAll(JekaTemplate.builtins());
            return newList;
        }
        return templates;
    }

    @Override
    public Element getState() {
        Element root = new Element("templates");
        for (JekaTemplate template : templates) {
            Element templateEl = new Element("template");
            root.addContent(templateEl);
            templateEl.addContent(new Element("name").setText(template.getName()));
            templateEl.addContent(new Element("cmd").setText(template.getCommandArgs()));
            templateEl.addContent(new Element("description").setText(template.getDescription()));
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
                    .build();
            result.add(template);
        }
        templates.clear();
        templates.addAll(result);
    }

}
