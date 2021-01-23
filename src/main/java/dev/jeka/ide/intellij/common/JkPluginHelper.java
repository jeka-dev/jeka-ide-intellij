package dev.jeka.ide.intellij.common;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.ModuleLibraryOrderEntryImpl;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.util.Query;
import dev.jeka.ide.intellij.common.model.Iml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JkPluginHelper {

    public static Collection<PsiClass> getPluginClasses(Module module) {
        Iml iml;
        try (InputStream is = module.getModuleFile().getInputStream()) {
            iml = Iml.of(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        List<String> jekaUrls = iml.getAllJekaBinUrl();
        List<String> pluginClassNames = jekaUrls.stream()
                .flatMap(url -> jekaPluginClassNamesContainedIn(url).stream())
                .collect(Collectors.toList());
        List<PsiClass> result = new LinkedList<>();
        JavaPsiFacade facade = JavaPsiFacade.getInstance(module.getProject());
        GlobalSearchScope searchScope = module.getModuleWithLibrariesScope();
        for (String className : pluginClassNames) {
            PsiClass psiClass = facade.findClass(className, searchScope);
            result.add(psiClass);
        }
        return result;
    }

    private static List<String> jekaPluginClassNamesContainedIn(String url) {
        String path = URI.create(url).getPath();
        //String path = LocalFileSystem.getInstance().extractPresentableUrl(url);
        List<String> result = new LinkedList<>();
        try (ZipFile zf = new ZipFile(path)) {
            Enumeration<? extends ZipEntry> zipEntries = zf.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry entry = zipEntries.nextElement();
                if (!entry.isDirectory()) {
                    String name = entry.getName();
                    int lastIndexOf = name.lastIndexOf("/");
                    String shortName = lastIndexOf == -1 ? name : name.substring(lastIndexOf + 1);
                    if (shortName.startsWith("JkPlugin") && shortName.endsWith(".class")
                            && !shortName.equals("JkPlugin.class")) {
                        String className = name.replace("/", ".").replace(".class", "");
                        if (!className.contains("$")) {
                            result.add(className);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return result;
    }

}
