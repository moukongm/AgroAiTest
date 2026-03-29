package com.user.profile.ui.adapters;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.agri.pest.client.model.response.PostResponseDto;
import com.common.utils.ImageLoader;
import com.uikit.base.BaseBindingMultiAdapter;
import com.user.R;
import com.user.databinding.ItemStarProfileDateBinding;
import com.user.databinding.ItemStarProfilePostBinding;
import com.user.profile.model.StarFavoriteMutiItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StarFavoriteMultiAdapter extends BaseBindingMultiAdapter<StarFavoriteMutiItem> {

    public StarFavoriteMultiAdapter() {
        super(null);
        addItemBinding(StarFavoriteMutiItem.TYPE_DATE, (inflater, parent, attach) ->
                ItemStarProfileDateBinding.inflate(inflater, parent, false));
        addItemBinding(StarFavoriteMutiItem.TYPE_POST, (inflater, parent, attach) ->
                ItemStarProfilePostBinding.inflate(inflater, parent, false));
    }

    @Override
    public void convert(@NotNull ViewBinding binding, @NonNull StarFavoriteMutiItem item, int itemType, int position) {
        if (itemType == StarFavoriteMutiItem.TYPE_DATE) {
            ItemStarProfileDateBinding b = (ItemStarProfileDateBinding) binding;
            b.tvStarDate.setText(item.getDateLabel() != null ? item.getDateLabel() :"");
            return;
        }
        if (itemType == StarFavoriteMutiItem.TYPE_POST) {
            ItemStarProfilePostBinding b = (ItemStarProfilePostBinding) binding;
            PostResponseDto post = item.getPost();
            if (post == null) {
                return;
            }
            // 封面图（首张）
            List<String> imgs = post.getImages();
            if (imgs != null && !imgs.isEmpty()) {
                ImageLoader.INSTANCE.load(b.ivStarPostCover, imgs.get(0));
            } else {
                b.ivStarPostCover.setImageResource(R.drawable.ic_load);
            }
            // 描述（title 或 content）
            String title = post.getTitle();
            String content = post.getContent();
            String displayText = (title != null && !title.isEmpty()) ? title : (content != null ? content : "");
            b.tvStarPostDesc.setText(displayText);
            // 作者头像
            String avatar = post.getAuthorAvatar();
            if (avatar != null && !avatar.isEmpty()) {
                ImageLoader.INSTANCE.loadCircle(b.ivStarPostAvatar, avatar);
            } else {
                b.ivStarPostAvatar.setImageResource(R.drawable.ic_etp_settitle);
            }
            // 作者昵称
            b.tvStarPostUsername.setText(post.getAuthorName() != null ? post.getAuthorName() : "");
            // 点赞数
            b.tvStarPostLikeCount.setText(String.valueOf(post.getFavoriteCount()));
        }
    }
}
