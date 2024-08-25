package crawler.reactive;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import crawler.common.Flag;
import crawler.common.Report;
import crawler.common.ReportRecord;
import crawler.common.lib.WebPageAnalyzerLib;

import java.util.*;
import java.util.stream.Stream;

public class RxWebPageAnalyzerLib implements WebPageAnalyzerLib<Flowable<ReportRecord>> {
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    private final RestTemplate restTemplate;
    private final Report report;
    private final Flag flag;

    public RxWebPageAnalyzerLib(Flag flag) {
        this.flag = flag;
        this.report = new Report();
        this.restTemplate = new RestTemplate();
        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(5000);
    }

    @Override
    public Flowable<ReportRecord> getWordOccurrences(String url, String word, int depth) {
        List<Document> documentList = new ArrayList<>();
        recursiveGetAllDocuments(url, 0, documentList);
        Stream<String> links = documentList.getFirst().select("a[href]").stream().map(l -> l.attr("abs:href"));
        Flowable<Document> docStream = this.getDocumentsStream(depth - 1, links);
        return this.createAnalyzedDocFlowable(docStream, word);
    }

    private Flowable<Document> getDocumentsStream(int depth, Stream<String> url) {
        return Flowable.fromStream(url).parallel()
                .runOn(Schedulers.io())
                .map(rep -> {
                    List<Document> docsList = new ArrayList<>();
                    recursiveGetAllDocuments(rep, depth, docsList);
                    return docsList.stream();
                }).flatMap(Flowable::fromStream)
                .sequential();
    }


    private Flowable<ReportRecord> createAnalyzedDocFlowable(Flowable<Document> search, String word) {
        return search.parallel().runOn(Schedulers.computation()).
                map(doc -> {
                    String text = doc.text();
                    long countWordOccurrence = Arrays.stream(text.split(" ")).
                            filter(s -> s.toLowerCase().contains(word.toLowerCase())).count();
                    return new ReportRecord(doc.baseUri(), countWordOccurrence);
                }).sequential();
    }

    private void recursiveGetAllDocuments(String url, int depth, List<Document> docsList) {
        if (UrlValidator.getInstance().isValid(url) && !flag.isSet()) {
            try {
                ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
                if (Objects.nonNull(entity.getBody())) {
                    Document doc = Jsoup.parse(entity.getBody());
                    doc.setBaseUri(url);
                    docsList.add(doc);
                    if (depth - 1 > 0 && !flag.isSet()) {
                        Elements links = doc.select("a[href]");
                        for (Element link : links) {
                            if (!flag.isSet()) {
                                recursiveGetAllDocuments(link.attr("abs:href"), depth - 1, docsList);
                            }
                        }
                    }
                }
            } catch (RestClientException e) {
                print(ANSI_RED + "Error fetching %s", url + ANSI_RESET);
            }
        }
    }

    protected void updateReport(ReportRecord record) {
        this.report.addRecord(record);
    }

    protected Set<ReportRecord> retrieveReport() {
        return this.report.getRecords();
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

}
