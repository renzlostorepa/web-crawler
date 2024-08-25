package crawler.common;


import java.util.*;
import java.util.stream.Collectors;

public class Report {
    private Set<ReportRecord> records;

    public Report() {
        this.records = new TreeSet<>(Comparator.comparing(ReportRecord::getSrcUrl));
    }

    public synchronized Set<ReportRecord> getRecords() {
        return records.stream().
                sorted(Comparator.comparing(ReportRecord::getCounter))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public synchronized void addRecord(ReportRecord record) {
        if (record.getCounter() != 0) {
            this.records.add(record);
        }
    }

    @Override
    public String toString() {
        return "Report{" +
                "records=" + records +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Objects.equals(records, report.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(records);
    }
}
