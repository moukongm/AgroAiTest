package com.detection.ui.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseFragment;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.router.RouterPath;
import com.detection.R;
import com.detection.databinding.DialogDatePickerBinding;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@Route(path = RouterPath.COMMON_FILTER)
public class CalendarFragment extends BaseFragment<DialogDatePickerBinding> {
    private int currentYear;
    private int selectedMonth = 1;
    private List<TextView> monthViews = new ArrayList<>();

    DialogDatePickerBinding binding;


    private void close() {

        LiveDataBus.getInstance().with(BusKey.FILTER).setValue("close");

        getParentFragmentManager()
                .beginTransaction()
                .remove(CalendarFragment.this)
                .commit();
    }

    @NonNull
    @Override
    public DialogDatePickerBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return DialogDatePickerBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        binding = getBinding();
        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        binding.tvYear.setText(String.valueOf(currentYear));

        monthViews.add(binding.tvMonth1);
        monthViews.add(binding.tvMonth2);
        monthViews.add(binding.tvMonth3);
        monthViews.add(binding.tvMonth4);
        monthViews.add(binding.tvMonth5);
        monthViews.add(binding.tvMonth6);
        monthViews.add(binding.tvMonth7);
        monthViews.add(binding.tvMonth8);
        monthViews.add(binding.tvMonth9);
        monthViews.add(binding.tvMonth10);
        monthViews.add(binding.tvMonth11);
        monthViews.add(binding.tvMonth12);

        for (int i = 0; i < monthViews.size(); i++) {
            final int month = i + 1;
            monthViews.get(i).setOnClickListener(v -> selectMonth(month));
        }

        binding.ivPrev.setOnClickListener(v -> {
            currentYear--;
            binding.tvYear.setText(String.valueOf(currentYear));
        });

        binding.ivNext.setOnClickListener(v -> {
            currentYear++;
            binding.tvYear.setText(String.valueOf(currentYear));
        });

        binding.tvCancel.setOnClickListener(v -> {
            close();
        });

        binding.tvConfirm.setOnClickListener(v -> {
            String dateStr = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    .format(Calendar.getInstance().getTime())
                    .replace(Calendar.getInstance().get(Calendar.YEAR) + "", String.valueOf(currentYear));
            String[] parts = dateStr.split("-");
            parts[0] = String.valueOf(currentYear);
            dateStr = String.format("%s-%02d", parts[0], selectedMonth);

                LiveDataBus.getInstance().with(BusKey.FILTER).setValue(dateStr);

            close();
        });
        
    }

    private void selectMonth(int month) {
        selectedMonth = month;
        for (int i = 0; i < monthViews.size(); i++) {
            monthViews.get(i).setSelected(i == month - 1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LiveDataBus.getInstance().with(BusKey.FILTER).setValue("");
    }

    @Override
    public void initData() {

    }
}
