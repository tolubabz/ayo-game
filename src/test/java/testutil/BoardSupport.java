package testutil;

import model.Board;

import java.lang.reflect.Field;

/**
 * Test-only helpers for setting up and inspecting {@link Board} positions.
 *
 * <p>{@code Board} keeps its pits private and exposes no seed-level accessors, so tests use
 * reflection here to build arbitrary positions and read pit contents without adding
 * test-only methods to the production API.
 */
public final class BoardSupport {

    private BoardSupport() {}

    /** Overwrites a board's 12 pits with the given values. */
    public static void seed(Board board, int... pits) {
        if (pits.length != 12) {
            throw new IllegalArgumentException("expected exactly 12 pit values, got " + pits.length);
        }
        int[] arr = (int[]) get(board, "pits");
        System.arraycopy(pits, 0, arr, 0, 12);
    }

    /** Returns a snapshot copy of a board's 12 pits. */
    public static int[] pits(Board board) {
        return ((int[]) get(board, "pits")).clone();
    }

    private static Object get(Board board, String field) {
        try {
            Field f = Board.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(board);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("could not access Board." + field, e);
        }
    }
}