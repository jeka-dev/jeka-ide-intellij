package dev.jeka.ide.intellij;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {

    private static final String JKCOMMANDS_NAME = "dev.jeka.core.tool.JkCommandSet";

    public static String getPathVariable(String varName) {
        PathMacros pathMacros = PathMacros.getInstance();
        return pathMacros.getValue(varName);
    }

    public static void setPathVariable(String varName, String value) {
        PathMacros pathMacros = PathMacros.getInstance();
        pathMacros.setMacro(varName, value);
    }

    public static void unzip(final InputStream zipSource, final Path targetFolder) {
        try (ZipInputStream zipInputStream = new ZipInputStream(zipSource)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final Path toPath = targetFolder.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectory(toPath);
                } else {
                    Files.copy(zipInputStream, toPath);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isExtendingJkCommandSet(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        if (JKCOMMANDS_NAME.equals(psiClass.getQualifiedName())) {
            return true;
        }
        PsiClassType[] psiClassTypes = psiClass.getExtendsListTypes();
        for (PsiClassType psiClassType : psiClassTypes) {
            if (psiClassType == null) {
                return false;
            }
            PsiClass currentPsiClass = psiClassType.resolve();
            if (isExtendingJkCommandSet(currentPsiClass)) {
                return true;
            }
        }
        return false;
    }

    public static Module getModule(AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            return null;
        }
        return ModuleUtil.findModuleForFile(virtualFile, event.getProject());
    }

    public static VirtualFile getModuleDir(Module module) {
        VirtualFile candidate = module.getModuleFile().getParent();
        if (candidate.getName().equals(".idea")) {
            return candidate.getParent();
        }
        return candidate;
    }

    public static void deleteDir(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .peek(System.out::println)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void addModule(Path projectDir, Path modulesXmlPath, Path moduleImlFile) {
        SAXBuilder builder = new SAXBuilder();
        org.jdom2.Document doc;
        try {
            doc = builder.build(modulesXmlPath.toFile());
        } catch (JDOMException | IOException e) {
            throw new RuntimeException(e);
        }
        String modulesQuery = "/project/component[@name='ProjectModuleManager']/modules";
        XPathFactory xpfac = XPathFactory.instance();
        XPathExpression<Element> xp = xpfac.compile(modulesQuery, Filters.element());
        List<Element> modulesEl = xp.evaluate(doc);
        if (modulesEl.isEmpty()) {
            return;
        }
        Element modules = modulesEl.get(0);
        String relativePath = projectDir.relativize(moduleImlFile).toString();
        modules.addContent(new Element("module")
                .setAttribute("fileurl", "file://$PROJECT_DIR$/" + relativePath)
                .setAttribute("filepath", "$PROJECT_DIR$/" + relativePath)
        );
        XMLOutputter output = new XMLOutputter();
        output.setFormat(Format.getPrettyFormat());
        try (OutputStream os = Files.newOutputStream(modulesXmlPath)) {
            output.output(doc, os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // has .iml file at its direct child so "is" or "can become" a module
    static boolean isPotentialModule(VirtualFile directory) {
        String name = directory.getName();
        if (directory.findChild(name + ".iml") != null) {
            return true;
        }
        VirtualFile ideaDir = directory.findChild(".idea");
        if (ideaDir == null) {
            return false;
        }
        return ideaDir.findChild(name + ".iml") != null;
    }

    static boolean isExistingModuleRoot(Project project, VirtualFile directory) {
        return getModuleHavingRootDir(project, directory) != null;
    }

    static Module getModuleHavingRootDir(Project project, VirtualFile directory) {
        Module module = ModuleUtil.findModuleForFile(directory, project);
        if (module == null) {
            return null;
        }
        VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
        if (roots.length == 0) {
            return null;
        }
        for (VirtualFile root : roots) {
            if (root.equals(directory) && directory.getName().equals(module.getName())) {
                return module;
            }
        }
        return null;
    }

    static boolean containsJekaDir(VirtualFile dir) {
        for (VirtualFile virtualFile : dir.getChildren()) {
            if ("jeka".equals(virtualFile.getName()) && virtualFile.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    static String getJekaRedirectModule(Project project, VirtualFile moduleRoot) {
        Path root = Paths.get(moduleRoot.getPath()).normalize();
        Path jekawBat = root.resolve("jekaw.bat");
        Path jekaw = root.resolve("jekaw");
        if (Files.exists(jekawBat)) {
            String result = extract(project, moduleRoot, jekawBat, "%*", "\\");
            if (result != null) {
                return result;
            }
        }
        if (Files.exists(jekaw)) {
            return extract(project, moduleRoot, jekaw, "$@", "/");
        }
        return null;
    }

    private static String extract(Project project, VirtualFile moduleRoot, Path jekawFile, String joker, String pathSeparator) {
        List<String> lines;
        try {
            lines = Files.readAllLines(jekawFile, Charset.forName("utf-8")).stream()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (lines.size() > 10) {
            return null;  // Not a wrapper
        }
        for (String line : lines) {
            String[] items = line.trim().split(" ");
            if (items.length > 1 && items[1].trim().equals(joker)) {
                String command = items[0].trim().replace('\\', '/');
                return Paths.get(command).getParent().getFileName().toString();
            }
        }
        return null;
    }



}
