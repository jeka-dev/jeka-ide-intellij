package dev.jeka.ide.intellij.common;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.JdkUtils;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.util.lang.UrlClassLoader;
import dev.jeka.core.api.utils.JkUtilsReflect;
import lombok.SneakyThrows;
import lombok.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class JkCommandSetHelper {

    private static final String BASE_DIR_METHOD = "baseDirContext";

    private static final String FACTORY_METHOD = "ofUninitialised";

    @SneakyThrows
    public static State getState(Module module, PsiClass commandSetPsiClass) {
        VirtualFile[] roots = ModuleRootManager.getInstance(module).orderEntries().getClassesRoots();
        List<URL> urls = Arrays.stream(roots)
                .map(virtualFile -> FileHelper.toUrl(virtualFile))
                .collect(Collectors.toList());
        UrlClassLoader classLoader = UrlClassLoader.build()
                .urls(urls)
                .get();
        Object commandSet = instantiate(module, commandSetPsiClass, classLoader);
        Class jkPluginClass = classLoader.loadClass(PsiClassHelper.JKPLUGIN_CLASS_NAME);
        Map<String, String> publicClassFields = getPublicFields(commandSet, jkPluginClass);
        Set<Object> plugins = getPlugins(commandSet, jkPluginClass);
        Map<String, Map<String, String>> pluginFields = new LinkedHashMap<>();
        for (Object plugin : plugins) {
            String pluginName = MiscHelper.pluginName(plugin.getClass().getSimpleName());
            Map<String, String> fields = getPublicFields(plugin, jkPluginClass);
            pluginFields.put(pluginName, fields);
        }
        return new State(publicClassFields, pluginFields);

    }

    @SneakyThrows
    private static Object instantiate(Module module, PsiClass commandSetPsiClass, ClassLoader classLoader) {
        Class clazz = classLoader.loadClass(commandSetPsiClass.getQualifiedName());
        Method baseDirMethod = JkUtilsReflect.findMethodMethodDeclaration(clazz, BASE_DIR_METHOD, Path.class);
        VirtualFile dir = ModuleHelper.getModuleDir(module);
        Path path = Paths.get(dir.getPresentableUrl());
        baseDirMethod.setAccessible(true);
        baseDirMethod.invoke(null, path);
        Method factoryMethod = JkUtilsReflect.findMethodMethodDeclaration(clazz, FACTORY_METHOD, Class.class);
        factoryMethod.setAccessible(true);
        ClassLoader currentClassloader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            return factoryMethod.invoke(null, clazz);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassloader);
        }
    }

    @SneakyThrows
    private static Map<String, String> getPublicFields(Object object, Class jkPluginClass) {
        Map<String, String> result = new LinkedHashMap<>();
        Field[] fields = object.getClass().getFields();
        for (Field field : fields) {
            if (jkPluginClass.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            result.put(field.getName(), Objects.toString(field.get(object)));
        }
        return result;
    }

    @SneakyThrows
    private static Set<Object> getPlugins(Object object, Class jkPluginClass) {
        Set<Object> result = new LinkedHashSet<>();
        List<Field> fields = getAllFields(object.getClass());
        for (Field field : fields) {
            if (jkPluginClass.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                Object plugin = field.get(object);
                result.add(plugin);
                result.addAll(getPlugins(plugin, jkPluginClass));
            }
        }
        return result;
    }

    @SneakyThrows
    private static Object getPlugin(String pluginClassName, Object commandSet) {
        List<Field> fields = getAllFields(commandSet.getClass());
        for (Field field : fields) {
            if (field.getDeclaringClass().getName().equals(pluginClassName)) {
                field.setAccessible(true);
                return field.get(commandSet);
            }
        }
        return null;
    }

    private static List<Field> getAllFields(Class clazz) {
        List<Field> result = new LinkedList<>(Arrays.asList(clazz.getDeclaredFields()));
        if (!clazz.getSuperclass().equals(Object.class)) {
            result.addAll(getAllFields(clazz.getSuperclass()));
        }
        return result;
    }

    @Value
    public static class State {
        Map<String, String> fields;
        Map<String, Map<String, String>> pluginFields;

        public String getValue(String pluginName, String fieldName) {
            if (pluginName == null) {
                return fields.get(fieldName);
            }
            return pluginFields.getOrDefault(pluginName, Collections.emptyMap()).get(fieldName);
        }
    }

}
