package dev.jeka.ide.intellij.common.model;

import com.github.djeang.vincerdom.VDocument;
import com.github.djeang.vincerdom.VElement;
import com.intellij.util.Url;
import dev.jeka.ide.intellij.common.Constants;
import dev.jeka.ide.intellij.common.MiscHelper;
import lombok.Value;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class Iml {

    List<LibraryOrderEntry> orderEntries;

    @Value
    static class LibraryOrderEntry {
        boolean forJeka;
        List<String> binUrls;
        String moduleName;
    }

    public static Iml of(InputStream is) {
        VDocument vdoc = VDocument.parse(is);
        List<VElement> orderEntries = vdoc.root().xpath("component/orderEntry");
        List<LibraryOrderEntry> entries = new LinkedList<>();
        for (VElement el : orderEntries) {
            boolean forJeka = el.getW3cElement().hasAttribute("forJeka");
            String type = el.getW3cElement().getAttribute("type");
            if ("module-library".equals(type)) {
                List<VElement> roots = el.xpath("library/CLASSES/root");
                List<String> urls = new LinkedList<>();
                for (VElement elem : roots) {
                    urls.add(elem.getW3cElement().getAttribute("url"));
                }
                entries.add(new LibraryOrderEntry(forJeka, urls, null));
            } else if ("module".equals(type)) {
                String moduleName = el.getW3cElement().getAttribute("module-name");
                entries.add(new LibraryOrderEntry(forJeka, null, moduleName));
            }
        }
        return new Iml(entries);
    }

    public List<String> getAllJekaBinUrl() {
        List<String> result = new LinkedList<>();
        return orderEntries.stream()
                .filter(loe -> loe.forJeka)
                .filter(loe -> loe.binUrls != null)
                .flatMap(loe -> loe.binUrls.stream())
                .map(Iml::resolve)
                .collect(Collectors.toList());
    }

    public List<String> getAllJekaModules() {
        List<String> result = new LinkedList<>();
        return orderEntries.stream()
                .filter(loe -> loe.forJeka)
                .filter(loe -> loe.moduleName != null)
                .map(loe -> loe.moduleName)
                .collect(Collectors.toList());
    }

    private static String resolve(String url) {
        String result = url
                .replace("$" + Constants.JEKA_USER_HOME + "$", MiscHelper.getPathVariable(Constants.JEKA_USER_HOME))
                .replace("$" + Constants.JEKA_HOME + "$", MiscHelper.getPathVariable(Constants.JEKA_HOME));
        if (result.startsWith("jar:")) {
            result = result.replace("jar:", "file:");
            result = result.substring(0, result.length()-2);
        }
        return result;

    }

}
