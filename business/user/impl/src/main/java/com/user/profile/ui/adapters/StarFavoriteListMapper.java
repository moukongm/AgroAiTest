package com.user.profile.ui.adapters;

import com.agri.pest.client.model.response.PostResponseDto;
import com.user.profile.model.StarFavoriteMutiItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class StarFavoriteListMapper {

    private StarFavoriteListMapper() {
    }

    public static List<StarFavoriteMutiItem> toMultiList(List<PostResponseDto> posts) {
        List<StarFavoriteMutiItem> result = new ArrayList<>();
        if(posts == null || posts.isEmpty()){
            return result;
        }
        //排序
        List<PostResponseDto> list = new ArrayList<>(posts);
        //sort参数需要一个比较器
        // 第一个参数定义比较的方法，第二个参数可选定义null时的处理，这里写了null的排在最后
        // 第一：StarFavoriteListMapper::createdAtString是简写语法，表示用这个类的静态方法；意思是定义比较方式；

        list.sort(Comparator.comparing(StarFavoriteListMapper::createdAtString,Comparator.nullsLast(String :: compareTo)).reversed());

        String lastTime = null;
        for (PostResponseDto postResponseDto : list) {
            String time = extractDateKey(postResponseDto);
            if(!time.equals(lastTime)){
                result.add(StarFavoriteMutiItem.dateHeader(time.isEmpty() ? "--" : time));
                lastTime= time;
            }
            result.add(StarFavoriteMutiItem.postItem(postResponseDto));
        }
        return result;
    }

    private static String createdAtString(PostResponseDto p) {
        if (p == null || p.getCreatedAt() == null) {
            return "";
        }
        return p.getCreatedAt().toString();
    }

    private static String extractDateKey(PostResponseDto p) {
        String s = createdAtString(p);
        if (s.length() >= 10) {
            return s.substring(0, 10);
        }
        return "";
    }
}
