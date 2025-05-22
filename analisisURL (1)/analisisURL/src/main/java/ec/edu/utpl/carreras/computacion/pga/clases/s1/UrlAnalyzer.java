package ec.edu.utpl.carreras.computacion.pga.clases.s1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;

public class UrlAnalyzer implements Runnable {
    private final String url;
    private int internalLinks = 0;

    public UrlAnalyzer(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        try {
            Document doc = Jsoup.connect(url).get();
            URL base = new URL(url);
            String host = base.getHost();

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String absHref = link.absUrl("href");
                if (!absHref.isEmpty()) {
                    URL linkUrl = new URL(absHref);
                    if (linkUrl.getHost().equalsIgnoreCase(host)) {
                        internalLinks++;
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error procesando " + url + ": " + e.getMessage());
        }
    }

    public String getUrl() {
        return url;
    }

    public int getInternalLinks() {
        return internalLinks;
    }
}
