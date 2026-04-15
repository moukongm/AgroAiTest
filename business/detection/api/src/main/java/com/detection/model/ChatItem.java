package com.detection.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public class ChatItem  implements MultiItemEntity {
    public static final int TYPE_USER = 1;
    public static final int TYPE_AI = 2;

    public int type;
    public String content;
    public String imageUrl;
    public static ChatItem ai(int type,String content){
        return new ChatItem(content,null,TYPE_AI);
    }

    public static ChatItem mine(int type,String content,String imageUrl){
        if(imageUrl != null && !imageUrl.isEmpty()){
            return new ChatItem(content,imageUrl,TYPE_USER);
        }
        return new ChatItem(content,null,TYPE_USER);
    }

    public ChatItem(String content, String imageUrl, int type) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.type = type;
    }

    @Override
    public int getItemType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }


}
