package com.example.swooshv2;

public class RunnerMenuItem {
    private String title;
    private int iconResId;

    public RunnerMenuItem(String title, int iconResId) {
        this.title = title;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }
}
