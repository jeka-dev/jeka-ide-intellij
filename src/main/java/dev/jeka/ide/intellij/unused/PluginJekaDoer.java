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

package dev.jeka.ide.intellij.unused;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.lang.UrlClassLoader;
import dev.jeka.ide.intellij.common.Constants;
import dev.jeka.ide.intellij.common.MiscHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Jerome Angibaud
 */
public class PluginJekaDoer {

    static final PluginJekaDoer INSTANCE = new PluginJekaDoer();

    private Map<Path,ClassLoader> classLoaders = new HashMap<>();

    public void generateIml(Project project, Path moduleDir, String qualifiedClassName, VirtualFile moduleRoot) {
        List<String> argList = new LinkedList<>();
        if (qualifiedClassName != null) {
            argList.add("-JKC=" + qualifiedClassName);
        }
        argList.add("intellij#iml");
        argList.add("java#");
        String [] args = argList.toArray(new String[0]);
        Class mainClass = getJekaMainClass(moduleDir);
        Throwable error = execute(mainClass, moduleDir, args);
        if (error != null) {
            if ( error.getClass().getSimpleName().equals("JkException")) {
                argList.remove("-JKC=" + qualifiedClassName);
                argList.add("-JKC=");
                error = execute(mainClass, moduleDir, argList.toArray(new String[0]));
                handle(error);
            } else {
                handle(error);
            }
        }
    }

    public void scaffoldModule(Project project, Path moduleDir) {
        String[] args = new String[] {"scaffold#run", "scaffold#wrap", "scaffold#wrap", "java#", "intellij#iml"};
        Class mainClass = getJekaMainClass(moduleDir);
        handle(execute(mainClass, moduleDir, args));
    }

    private static void handle(Throwable throwable) {
        if (throwable != null) {
            throw new RuntimeException(throwable);
        }
    }

    private static Throwable execute(Class mainClass, Path moduleDir, String[] args)  {
        ClassLoader original =Thread.currentThread().getContextClassLoader();
        final Method method;
        try {
            method = mainClass.getMethod("exec", Path.class, args.getClass());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
        Thread.currentThread().setContextClassLoader(mainClass.getClassLoader());
        registerLog(mainClass.getClassLoader());
        try {
            method.invoke(null, moduleDir, args);
        } catch (InvocationTargetException e) {
            return e.getTargetException();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
        return null;
    }

    private Class getJekaMainClass(Path moduleRoot) {
        Path jar = Paths.get(MiscHelper.getPathVariable(Constants.JEKA_HOME)).resolve("dev.jeka.jeka-core.jar");
        if (!Files.exists(jar)) {
            throw new IllegalStateException("Cannot  find " + jar);
        }
        ClassLoader classloader= classLoaders.computeIfAbsent(jar, path -> {
            try {
                UrlClassLoader urlClassLoader = UrlClassLoader.build()
                        .parent(ClassLoader.getSystemClassLoader())
                        .urls(jar.toUri().toURL()).get();
                return urlClassLoader;
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            Class jklogClass = classloader.loadClass("dev.jeka.core.api.system.JkLog");
            Method method = jklogClass.getMethod("register", Consumer.class);
            method.invoke(null, new EventConsumer());
            return classloader.loadClass("dev.jeka.core.tool.Main");
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void registerLog(ClassLoader classloader) {
        try {
            Class jklogClass = classloader.loadClass("dev.jeka.core.api.system.JkLog");
            Method method = jklogClass.getMethod("register", Consumer.class);
            method.invoke(null, new EventConsumer());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static class EventConsumer implements Consumer<Map> {

        @Override
        public void accept(Map map) {
            System.out.println(map.get("type") + " : " + map.get("message"));
        }

    }

}
