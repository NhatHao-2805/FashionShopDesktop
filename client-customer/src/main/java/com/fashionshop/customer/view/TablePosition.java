package com.fashionshop.customer.view;

import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.VirtualFlow;

import java.util.List;
import java.util.function.ToLongFunction;

/** Keeps the first visible row and selection when a list is refreshed. */
record TablePosition(int firstIndex, Long firstKey, Long selectedKey) {
    static <T> TablePosition capture(TableView<T> table, ToLongFunction<T> key) {
        int firstIndex = -1;
        if (table.lookup(".virtual-flow") instanceof VirtualFlow<?> flow) {
            IndexedCell<?> first = flow.getFirstVisibleCell();
            if (first != null) firstIndex = first.getIndex();
        }
        List<T> items = table.getItems();
        Long firstKey = firstIndex >= 0 && firstIndex < items.size()
                ? key.applyAsLong(items.get(firstIndex)) : null;
        T selected = table.getSelectionModel().getSelectedItem();
        return new TablePosition(firstIndex, firstKey, selected == null ? null : key.applyAsLong(selected));
    }

    <T> void restore(TableView<T> table, ToLongFunction<T> key) {
        if (!table.getSortOrder().isEmpty()) table.sort();
        List<T> items = table.getItems();
        int selected = find(items, selectedKey, key);
        if (selected >= 0) table.getSelectionModel().select(selected);
        else table.getSelectionModel().clearSelection();

        if (firstIndex >= 0 && !items.isEmpty()) {
            int first = find(items, firstKey, key);
            int index = first >= 0 ? first : Math.min(firstIndex, items.size() - 1);
            table.layout();
            if (table.lookup(".virtual-flow") instanceof VirtualFlow<?> flow) {
                flow.scrollToTop(index);
            } else {
                table.scrollTo(index);
            }
        }
    }

    private static <T> int find(List<T> items, Long target, ToLongFunction<T> key) {
        if (target != null) {
            for (int index = 0; index < items.size(); index++) {
                if (key.applyAsLong(items.get(index)) == target) return index;
            }
        }
        return -1;
    }
}
