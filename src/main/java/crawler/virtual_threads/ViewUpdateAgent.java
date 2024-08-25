package crawler.virtual_threads;

import crawler.common.Flag;
import crawler.common.Report;
import crawler.common.gui.UrlAnalyzerView;

public class ViewUpdateAgent extends Thread {
    private final Report report;
    private final UrlAnalyzerView view;
    private final Flag flag;

    public ViewUpdateAgent(Report report, UrlAnalyzerView view, Flag flag) {
        this.report = report;
        this.view = view;
        this.flag = flag;
    }

    public void run() {
        long t0 = System.currentTimeMillis();
        while (!flag.isSet()) {
            try {
                view.update(report.getRecords());
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        view.update(report.getRecords());
        view.done();
        log("Done in " + (System.currentTimeMillis() - t0) + " ms");
    }

    private void log(String msg) {
        System.out.println("[" + getClass().getName() + "] " + msg);
    }
}
