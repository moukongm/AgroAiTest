package com.user.profile.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public class GuideMultiItem implements MultiItemEntity {
    public static final int TYPE_NEW = 1;
    public static final int TYPE_EDIT = 2;
    public static final int TYPE_ADD = 3;

    private String newCrop;
    private int itemType;
    private String editText;

    public GuideMultiItem(int itemType, String newCrop) {
        this.itemType = itemType;
        this.newCrop = newCrop;
    }

    public String getEditText() {
        return editText;
    }

    public void setEditText(String editText) {
        this.editText = editText;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getNewCrop() {
        return newCrop;
    }

    public void setNewCrop(String newCrop) {
        this.newCrop = newCrop;
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
