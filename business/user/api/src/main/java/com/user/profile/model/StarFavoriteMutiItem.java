package com.user.profile.model;

import androidx.annotation.Nullable;

import com.agri.pest.client.model.response.PostResponseDto;
import com.chad.library.adapter.base.entity.MultiItemEntity;

public class StarFavoriteMutiItem implements MultiItemEntity {
    public static final int TYPE_DATE = 1;
    public static final int TYPE_POST = 2;
    @Nullable
    private final String dateLabel;
    @Nullable
    private final PostResponseDto post;
    private final int itemType;
    @Override
    public int getItemType() {
        return itemType;
    }

    public static StarFavoriteMutiItem dateHeader(@Nullable String dateLabel) {
        return new StarFavoriteMutiItem(TYPE_DATE, dateLabel, null);
    }

    public static StarFavoriteMutiItem postItem(@Nullable PostResponseDto post) {
        return new StarFavoriteMutiItem(TYPE_POST, null, post);
    }
    public StarFavoriteMutiItem( int itemType,@Nullable String dateLabel, @Nullable PostResponseDto post) {
        this.dateLabel = dateLabel;
        this.post = post;
        this.itemType = itemType;
    }

    @Nullable
    public String getDateLabel() {
        return dateLabel;
    }

    @Nullable
    public PostResponseDto getPost() {
        return post;
    }
}
