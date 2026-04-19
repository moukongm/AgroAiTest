package com.detection.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;


public class DetectionViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public DetectionViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DetectionViewModel.class)) {
            return (T) new DetectionViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
