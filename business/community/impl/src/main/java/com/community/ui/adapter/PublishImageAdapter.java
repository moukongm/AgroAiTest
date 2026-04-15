package com.community.ui.adapter;

import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.community.databinding.ItemPublishImageBinding;
import com.uikit.base.BaseBindingMultiAdapter;

import java.util.ArrayList;
import java.util.List;

import coil.Coil;
import coil.request.ImageRequest;

public class PublishImageAdapter extends BaseBindingMultiAdapter<MultiItemEntity> {

    public static final int TYPE_ADD = 0;
    public static final int TYPE_IMAGE = 1;

    private OnAddClickListener addClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnAddClickListener {
        void onAddClick();
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public PublishImageAdapter() {
        super();
        addItemBinding(TYPE_ADD, ItemPublishImageBinding::inflate);
        addItemBinding(TYPE_IMAGE, ItemPublishImageBinding::inflate);
        initEmpty();
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getItemType();
    }

    @Override
    public void convert(@NonNull ViewBinding binding, @NonNull MultiItemEntity item, int itemType, int position) {
        ItemPublishImageBinding imgBinding = (ItemPublishImageBinding) binding;
        if (itemType == TYPE_ADD) {
            imgBinding.layoutImagePreview.setVisibility(View.GONE);
            imgBinding.layoutAddButton.setVisibility(View.VISIBLE);
            imgBinding.layoutAddButton.setOnClickListener(v -> {
                if (addClickListener != null) {
                    addClickListener.onAddClick();
                }
            });
        } else {
            imgBinding.layoutImagePreview.setVisibility(View.VISIBLE);
            imgBinding.layoutAddButton.setVisibility(View.GONE);
            ImageRequest request = new ImageRequest.Builder(imgBinding.getRoot().getContext())
                    .data(((ImageItem) item).uri)
                    .crossfade(true)
                    .target(imgBinding.ivImage)
                    .build();
            Coil.imageLoader(imgBinding.getRoot().getContext()).enqueue(request);
            imgBinding.btnDelete.setOnClickListener(v -> {
                if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION && deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(position);
                }
            });
        }
    }

    public void setOnAddClickListener(OnAddClickListener listener) {
        this.addClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setImages(List<Uri> uris) {
        List<MultiItemEntity> items = new ArrayList<>();
        if (uris != null) {
            for (Uri uri : uris) {
                items.add(new ImageItem(uri));
            }
        }
        if (items.size() < 9) {
            items.add(new AddPlaceholderItem());
        }
        setList(items);
    }

    public void initEmpty() {
        List<MultiItemEntity> items = new ArrayList<>();
        items.add(new AddPlaceholderItem());
        setList(items);
    }

    public int getRealImageCount() {
        int count = 0;
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i) instanceof ImageItem) count++;
        }
        return count;
    }
    public static class ImageItem implements MultiItemEntity {
        public final Uri uri;

        public ImageItem(Uri uri) {
            this.uri = uri;
        }

        @Override
        public int getItemType() {
            return TYPE_IMAGE;
        }
    }
    public static class AddPlaceholderItem implements MultiItemEntity {
        @Override
        public int getItemType() {
            return TYPE_ADD;
        }
    }
}
