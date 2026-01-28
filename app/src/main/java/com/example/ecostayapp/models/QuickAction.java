package com.example.ecostayapp.models;

import java.io.Serializable;

public class QuickAction implements Serializable {
    private String title;
    private String subtitle;
    private int iconResId;
    private int tabIndex;

    public QuickAction() {}

    public QuickAction(String title, String subtitle, int iconResId, int tabIndex) {
        this.title = title;
        this.subtitle = subtitle;
        this.iconResId = iconResId;
        this.tabIndex = tabIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getTabIndex() {
        return tabIndex;
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }
}







