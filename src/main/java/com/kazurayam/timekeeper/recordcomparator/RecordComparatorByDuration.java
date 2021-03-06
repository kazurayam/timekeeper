package com.kazurayam.timekeeper.recordcomparator;

import com.kazurayam.timekeeper.Record;
import com.kazurayam.timekeeper.RecordComparator;
import com.kazurayam.timekeeper.RowOrder;

public class RecordComparatorByDuration implements RecordComparator {

    private final RowOrder rowOrder;

    public RecordComparatorByDuration() {
        this(RowOrder.ASCENDING);
    }

    public RecordComparatorByDuration(RowOrder rowOrder) {
        this.rowOrder = rowOrder;
    }

    @Override
    public int compare(Record left, Record right) {
        return rowOrder.order() * left.getDuration().compareTo(right.getDuration());
    }

    @Override
    public String getDescription() {
        return String.format("sorted by duration (%s)", rowOrder.description());
    }
}
