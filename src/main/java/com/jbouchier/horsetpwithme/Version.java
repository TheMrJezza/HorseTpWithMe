package com.jbouchier.horsetpwithme;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Version implements Comparable<Version> {

    private final String version;
    private final int[] nums;

    public Version(@NotNull String version) {
        if (!version.matches("[0-9]+(\\.[0-9]+)*"))
            throw new IllegalArgumentException("Invalid version format");

        String[] thisParts = version.split("\\.");

        nums = new int[thisParts.length];
        for (int i = 0; i < nums.length; i++)
            nums[i] = Integer.parseInt(thisParts[i]);

        this.version = version;
    }

    @Override
    public int compareTo(@NotNull Version that) {
        int length = Math.max(this.nums.length, that.nums.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < this.nums.length ? this.nums[i] : 0;
            int thatPart = i < that.nums.length ? that.nums[i] : 0;
            if (thisPart < thatPart) return -1;
            if (thisPart > thatPart) return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(@NotNull Object that) {
        if (this == that) return true;
        if (this.getClass() != that.getClass()) return false;
        return this.compareTo((Version) that) == 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(nums);
    }

    @Override
    public String toString() {
        return this.version;
    }
}