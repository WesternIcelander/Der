package io.siggi.der.path;

import io.siggi.der.Der;
import io.siggi.der.DerSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DerPathFinder {
    private DerPathFinder() {
    }

    public static List<DerPath> find(Der root, Predicate<Der> match) {
        List<DerPath> paths = new ArrayList<>();
        if (root instanceof DerSequence) {
            find((DerSequence) root, match, paths, DerPath.create());
        }
        return paths;
    }

    private static void find(DerSequence sequence, Predicate<Der> match, List<DerPath> paths, DerPath currentPath) {
        List<Der> items = sequence.getItems();
        for (int i = 0; i < items.size(); i++) {
            Der item = items.get(i);
            if (match.test(item)) {
                paths.add(currentPath.extend(i));
            }
            if (item instanceof DerSequence) {
                find((DerSequence) item, match, paths, currentPath.extend(i));
            }
        }
    }
}
