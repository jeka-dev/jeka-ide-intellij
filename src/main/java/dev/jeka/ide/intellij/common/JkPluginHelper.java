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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JkPluginHelper {

    public static Collection<PsiClass> getPluginClasses(Module module,
                                                        Map<String, Collection<PsiClass>> context) {
        Iml iml;
        Path moduleDir = Paths.get(ModuleHelper.getModuleDir(module).getPath());
        try (InputStream is = module.getModuleFile().getInputStream()) {
            iml = Iml.of(is, moduleDir);
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
        ModuleManager moduleManager = ModuleManager.getInstance(module.getProject());
        for (String moduleName : iml.getAllJekaModules()) {
            if (context.containsKey(moduleName)) {
                result.addAll(context.get(moduleName));
            } else {
                Module otherModule = moduleManager.findModuleByName(moduleName);
                if (otherModule != null) {
                    result.addAll(getPluginClasses(otherModule, context));
                }
            }
        }
        context.put(module.getName(), result);
        return result.stream().distinct().collect(Collectors.toList());
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
