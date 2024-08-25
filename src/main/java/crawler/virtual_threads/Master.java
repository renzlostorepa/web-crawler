package crawler.virtual_threads;

import crawler.common.Flag;
import crawler.common.Report;
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
        Report report = new Report();
        VtWebPageAnalyzerLib vtWebPageAnalyzerLib = new VtWebPageAnalyzerLib(url, word, depth, flag, report);
        vtWebPageAnalyzerLib.start();

        ViewUpdateAgent viewer = new ViewUpdateAgent(report, view, flag);
        viewer.start();
    }
}
