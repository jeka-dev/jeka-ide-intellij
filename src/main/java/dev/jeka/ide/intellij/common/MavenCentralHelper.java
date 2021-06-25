package dev.jeka.ide.intellij.common;

import com.github.djeang.vincerdom.VDocument;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.net.URL;

public class MavenCentralHelper {

    @SneakyThrows
    public static String getLatestestJekaVersion() {
        String url = "https://search.maven.org/solrsearch/select?q=g:%22dev.jeka%22+AND+a:%22jeka-core%22&rows=1&wt=xml";
        try (InputStream inputStream = new URL(url).openStream()){
            VDocument doc = VDocument.parse(inputStream);
            return doc.root().xpath("result/doc/str[@name='latestVersion']").get(0).getText();
        }
    }
}
