package io.siggi.der.path;

import io.siggi.der.Der;
import io.siggi.der.DerSequence;

import java.util.Arrays;

public final class DerPath {

    private static final DerPath EMPTY = new DerPath(new int[0]);

    private final int[] path;
    private String toString;

    private DerPath(int[] path) {
        this.path = path;
    }

    public static DerPath create(int... path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (path.length == 0) {
            return EMPTY;
        }
        return new DerPath(Arrays.copyOf(path, path.length));
    }

    public static DerPath fromString(String path) {
        if (path.isEmpty()) return EMPTY;
        String[] split = path.split("\\.");
        int[] newPath = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            newPath[i] = Integer.parseInt(split[i]);
        }
        return new DerPath(newPath);
    }

    public int[] getPath() {
        return Arrays.copyOf(path, path.length);
    }

    public Der get(Der root) {
        return get(root, 0);
    }

    public void set(Der root, Der item) {
        DerSequence der = (DerSequence) get(root, -1);
        der.getItems().set(path[path.length - 1], item);
    }

    public Der get(Der root, int steps) {
        if (steps <= 0) steps += path.length;
        Der current = root;
        for (int i = 0; i < steps; i++) {
            current = ((DerSequence) current).getItems().get(path[i]);
        }
        return current;
    }

    public DerPath backtrack(int distance) {
        if (distance < 0) throw new IllegalArgumentException("distance must be >= 0");
        if (distance == 0) return this;
        if (distance > path.length) throw new IllegalArgumentException("distance must be <= the path length");
        if (distance == path.length) return EMPTY;
        int[] copy = Arrays.copyOf(path, path.length - distance);
        return new DerPath(copy);
    }

    public DerPath extend(int... extend) {
        if (extend == null) {
            throw new NullPointerException("extend");
        }
        if (extend.length == 0) {
            return this;
        }
        int[] newPath = Arrays.copyOf(path, path.length + extend.length);
        System.arraycopy(extend, 0, newPath, path.length, extend.length);
        return new DerPath(newPath);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DerPath)) return false;
        DerPath other = (DerPath) o;
        return Arrays.equals(path, other.path);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(path);
    }

    @Override
    public String toString() {
        if (toString == null) {
            StringBuilder builder = new StringBuilder();
            for (int item : path) {
                if (builder.length() > 0) builder.append('.');
                builder.append(item);
            }
            toString = builder.toString();
        }
        return toString;
    }
}
