package crawler.common.gui;

import crawler.common.InputListener;
import crawler.common.ReportRecord;

import java.util.Set;

public class UrlAnalyzerView {

    private UrlAnalyzerViewFrame gui;

    public UrlAnalyzerView(String url, String word, int depth, String title){
        gui = new UrlAnalyzerViewFrame(url, word, depth, title);
    }

    public  void display() {
        gui.display();
    }

    public void reset() {
        gui.reset();
    }

    public void addListener(InputListener controller) {
        gui.addListener(controller);
    }

    public synchronized void update(Set<ReportRecord> report) {
        gui.update(report);
    }

    public void done() {
        gui.done();
    }
}
