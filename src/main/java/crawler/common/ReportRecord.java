package crawler.common;

import java.util.Objects;

public class ReportRecord {
    private String srcUrl;

    private Long counter;

    public ReportRecord(String srcUrl, long counter) {
        this.srcUrl = srcUrl;
        this.counter = counter;
    }

    public String getSrcUrl() {
        return srcUrl;
    }

    public Long getCounter() {
        return counter;
    }

    @Override
    public String toString() {
        return "ReportRecord{" +
                "srcUrl='" + srcUrl + '\'' +
                ", counter=" + counter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportRecord that = (ReportRecord) o;
        return Objects.equals(srcUrl, that.srcUrl) && Objects.equals(counter, that.counter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcUrl, counter);
    }

}
