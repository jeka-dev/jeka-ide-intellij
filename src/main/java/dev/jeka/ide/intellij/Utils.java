package dev.jeka.ide.intellij;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
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


import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {

    private static final String JKCOMMANDS_NAME = "dev.jeka.core.tool.JkCommands";

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

    public static boolean isExtendingJkCommands(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        if (JKCOMMANDS_NAME.equals(psiClass.getQualifiedName())) {
            return true;
        }
        PsiClassType[] psiClassTypes = psiClass.getExtendsListTypes();
        for (PsiClassType psiClassType : psiClassTypes) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiClassType;
            if (psiClassReferenceType == null) {
                return false;
            }
            PsiClass currentPsiClass = psiClassReferenceType.resolve();
            if (isExtendingJkCommands(currentPsiClass)) {
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

    public static Path getModuleDir(Module module) {
        Path candidate = Paths.get(ModuleUtilCore.getModuleDirPath(module));
        if (candidate.getFileName().toString().equals(".idea")) {
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

}
