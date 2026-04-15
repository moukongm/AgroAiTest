package com.main.ui.adapter;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.agri.pest.client.model.response.MyCropResponseDto;
import com.common.storage.database.CropRecord;
import com.common.utils.ImageLoader;
import com.common.utils.LogUtils;
import com.main.impl.R;
import com.main.impl.databinding.ItemMainPlantBinding;
import com.uikit.base.BaseBindingAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyCropAdapter extends BaseBindingAdapter<Object, ItemMainPlantBinding> {

    private OnCropClickListener onCropClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private boolean deleteEnabled = true;
    private boolean cropClickEnabled = true;
    private List<MyCropResponseDto> networkData = new ArrayList<>();
    private List<CropRecord> localData = new ArrayList<>();
    private String userAvatarPath;

    public interface OnCropClickListener {
        void onCropClick(Object crop);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Object crop, int position);
    }

    public void setOnCropClickListener(OnCropClickListener listener) {
        this.onCropClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public void setDeleteEnabled(boolean enabled) {
        this.deleteEnabled = enabled;
        notifyDataSetChanged();
    }

    public void setCropClickEnabled(boolean enabled) {
        this.cropClickEnabled = enabled;
        notifyDataSetChanged();
    }

    public void setUserAvatarPath(String path) {
        this.userAvatarPath = path;
        notifyDataSetChanged();
    }

    public void setData(List<MyCropResponseDto> data) {
        this.networkData = data != null ? data : new ArrayList<>();
        this.localData = new ArrayList<>();
        setList(networkData);
    }

    public void setDataWithUserAvatar(List<MyCropResponseDto> data, String userAvatarPath) {
        this.networkData = data != null ? data : new ArrayList<>();
        this.localData = new ArrayList<>();
        this.userAvatarPath = userAvatarPath;
        setList(this.networkData);
        LogUtils.INSTANCE.d("MyCropAdapter", "after setList, wadapter data count=" + getData().size());
    }

    public void setLocalData(List<CropRecord> data) {
        this.localData = data != null ? data : new ArrayList<>();
        this.networkData = new ArrayList<>();
        // 从 CropRecord 中获取用户头像路径（如果有的话）
        if (data != null && !data.isEmpty()) {
            CropRecord first = data.get(0);
            if (first.getAvatarLocalPath() != null) {
                this.userAvatarPath = first.getAvatarLocalPath();
            }
        }
        setList(new ArrayList<>(localData));
        LogUtils.INSTANCE.d("MyCropAdapter", "after setList, adapter data count=" + getData().size());
    }

    @Override
    public void convert(@NonNull ItemMainPlantBinding binding, @NonNull Object item, int position) {
        // 加载用户头像（所有item都显示同一个用户头像）
        if (userAvatarPath != null && !userAvatarPath.isEmpty()) {
            ImageLoader.INSTANCE.load(binding.ivSettingTitle, userAvatarPath);
        }
        LogUtils.INSTANCE.d("HomeViewModel",item.getClass().getName());

        if (item instanceof MyCropResponseDto) {
            MyCropResponseDto crop = (MyCropResponseDto) item;
            binding.tvCropname.setText(crop.getPlantName());
            if (crop.getImageUrl() != null && !crop.getImageUrl().isEmpty()) {
                ImageLoader.INSTANCE.load(binding.ivPlant, crop.getImageUrl());
            }
        } else if (item instanceof CropRecord) {
            CropRecord crop = (CropRecord) item;
            binding.tvCropname.setText(crop.getCropName());
            // 离线模式优先使用本地图片路径，在线模式使用网络URL
            String imagePath = crop.getCropImageUrl();
            if (imagePath != null && !imagePath.isEmpty()) {
                ImageLoader.INSTANCE.load(binding.ivPlant, imagePath);
            } else if (crop.getCropImageUrl() != null && !crop.getCropImageUrl().isEmpty()) {
                ImageLoader.INSTANCE.load(binding.ivPlant, crop.getCropImageUrl());
            }
        }

        if (cropClickEnabled) {
            binding.getRoot().setOnClickListener(v -> {
                if (onCropClickListener != null) {
                    onCropClickListener.onCropClick(item);
                }
            });
        } else {
            binding.getRoot().setOnClickListener(null);
        }

        if (deleteEnabled) {
            if (item instanceof MyCropResponseDto) {
                binding.ivDelete.setVisibility(View.VISIBLE);
                binding.ivDelete.setOnClickListener(v -> {
                    if (onDeleteClickListener != null) {
                        onDeleteClickListener.onDeleteClick(item, position);
                    }
                });
            } else {
                // CropRecord 等本地数据不显示删除按钮
                binding.ivDelete.setVisibility(View.GONE);
            }
        } else {
            binding.ivDelete.setVisibility(View.GONE);
        }

    }
}