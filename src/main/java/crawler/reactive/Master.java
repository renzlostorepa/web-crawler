package crawler.reactive;

import io.reactivex.rxjava3.core.Flowable;
import crawler.common.Flag;
import crawler.common.ReportRecord;
import crawler.common.gui.UrlAnalyzerView;

public class Master extends Thread {
    private final String url;
    private final String word;
    private final int depth;
    private final Flag flag;
    private final UrlAnalyzerView view;

    public Master(String url, String word, int depth, Flag flag, UrlAnalyzerView view) {
        super("Master");
        this.url = url;
        this.word = word;
        this.depth = depth;
        this.flag = flag;
        this.view = view;
    }

    public void run() {
        long t0 = System.currentTimeMillis();
        RxWebPageAnalyzerLib rxWebPageAnalyzerLib = new RxWebPageAnalyzerLib(flag);
        Flowable<ReportRecord> wordOccurrences = rxWebPageAnalyzerLib.getWordOccurrences(url, word, depth);
        wordOccurrences.subscribe(record -> {
            rxWebPageAnalyzerLib.updateReport(record);
            view.update(rxWebPageAnalyzerLib.retrieveReport());
        }, e -> {
            view.done();
            log("stopped.");
            log("Done in " + (System.currentTimeMillis() - t0) + " ms");
        }, () -> {
            view.done();
            log("done.");
            log("Done in " + (System.currentTimeMillis() - t0) + " ms");
        });
    }

    private void log(String msg) {
        System.out.println("[" + getClass().getName() + "] " + msg);
    }
}
