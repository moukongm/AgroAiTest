package com.main.ui.page;

import android.app.AlertDialog;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.common.utils.ImageLoader;
import com.common.utils.ImagePickerUtil;
import com.common.utils.LogUtils;
import com.google.android.material.button.MaterialButton;
import com.main.impl.R;
import com.main.impl.databinding.ActivityPlantManageBinding;
import com.main.ui.widget.PlantCalendarView;
import com.main.viewmodel.PlantManageViewModel;

import java.time.LocalDate;
import java.util.Calendar;

@Route(path = RouterPath.PLANT_MANAGE_ACTIVITY)
public class PlantManageActivity extends BaseActivity<ActivityPlantManageBinding> {

    private PlantCalendarView calendarView;
    private int currentTabPosition = 0;
    private PlantManageViewModel viewModel;
    private ImagePickerUtil imagePicker;
    private Uri selectedImageUri;
    private long plantId = -1;
    private String originalPlantName;
    private String originalStatus;
    private LocalDate originalPlantingDate;
    private LocalDate originalMaturityDate;
    private String originalImageUrl;
    private boolean isEditMode = false;

    private static final int STATE_COMPLETE = PlantCalendarView.STATE_COMPLETE;
    private static final int STATE_PENDING = PlantCalendarView.STATE_PENDING;
    private static final int TYPE_WATER = PlantCalendarView.TYPE_WATER;
    private static final int TYPE_FERTILIZE = PlantCalendarView.TYPE_FERTILIZE;
    private static final int TYPE_MEDICINE = PlantCalendarView.TYPE_MEDICINE;
    private static final int TYPE_NOTE = PlantCalendarView.TYPE_NOTE;
    private int selectedType = -1;
    private int selectedState = -1;

    private static final int[] TAB_IDS = { R.id.tab_water, R.id.tab_fertilize, R.id.tab_medicine, R.id.tab_note };
    private int toolbarBaseHeight = -1;

    @Override
    public ActivityPlantManageBinding getViewBinding() {
        return ActivityPlantManageBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        if (getIntent() != null) {
            plantId = getIntent().getLongExtra("plantId", -1);
        }
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(PlantManageViewModel.class);
        setupToolbar();
        setupHealthSpinner();
        setupTabs();
        setupCalendar();
        setupButtons();
        observeViewModel();
        selectTab(0);
        resetAllButtonStyles();
    }

    private void setupHealthSpinner() {
        binding.cdHealthStatus.setOnClickListener(v -> {
            showHealthStatusDialog();
            enterEditMode();
        });
    }

    private void showHealthStatusDialog() {
        String[] healthOptions = {"良好", "中等", "差"};
        int currentSelection = getHealthSpinnerSelection();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择健康状况")
               .setSingleChoiceItems(healthOptions, currentSelection, (dialog, which) -> {
                   binding.tvHealthStatus.setText(healthOptions[which]);
                   dialog.dismiss();
               })
               .setNegativeButton("取消", null)
               .show();
    }

    private int getHealthSpinnerSelection() {
        String current = binding.tvHealthStatus.getText().toString();
        String[] options = {"良好", "中等", "差"};
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(current)) {
                return i;
            }
        }
        return 0;
    }

    private String getHealthSpinnerValue() {
        return binding.tvHealthStatus.getText().toString();
    }

    private void setHealthSpinnerSelection(String status) {
        if (status != null) {
            binding.tvHealthStatus.setText(status);
        }
    }

    private void observeViewModel() {
        viewModel.getCropDetailLiveData().observe(this, cropDetail -> {
            if (cropDetail != null) {
                if (cropDetail.getPlantName() != null) {
                    binding.etPlantNameLabel.setText(cropDetail.getPlantName());
                }
                if (cropDetail.getImageUrl() != null) {
                    ImageLoader.INSTANCE.load(binding.ivHeaderBg, cropDetail.getImageUrl());
                }
                if (cropDetail.getStatus() != null) {
                    setHealthSpinnerSelection(cropDetail.getStatus());
                }
                if (cropDetail.getPlantingDate() != null) {
                    binding.etPlantDateValue.setText(formatDate(cropDetail.getPlantingDate()));
                }
//                if (cropDetail.getPestCount() != null) {
//                    binding.tvDiseaseValue.setText(cropDetail.getPestCount() + "次");
//                }
//                if (cropDetail.getMaturityDate() != null) {
//                    binding.etMatureValue.setText(formatDate(cropDetail.getMaturityDate()));
//                }
                saveOriginalData(cropDetail);
            }
        });

        viewModel.getErrorMessageLiveData().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoadingLiveData().observe(this, isLoading -> {});

        viewModel.getRecordsLiveData().observe(this, records -> {
            if (records != null) {
                loadRecordsToCalendar(records);
            }
        });

        viewModel.getTagOperationLiveData().observe(this, success -> {
            if (success != null && success) {
                viewModel.getCropDetail(plantId);
            }
        });

        viewModel.getUpdateCropLiveData().observe(this, success -> {
            if (success != null && success) {
                isEditMode = false;
                binding.btnSaveChanges.setVisibility(View.GONE);
                selectedImageUri = null;
                Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateStateLiveData().observe(this, state -> {
            switch (state) {
                case PlantManageViewModel.STATE_UPLOADING:
                    showLoading("上传图片中...");
                    binding.btnSaveChanges.setEnabled(false);
                    break;
                case PlantManageViewModel.STATE_UPDATING:
                    showLoading("保存中...");
                    binding.btnSaveChanges.setEnabled(false);
                    break;
                default:
                    hideLoading();
                    binding.btnSaveChanges.setEnabled(true);
                    break;
            }
        });
    }

    private void saveOriginalData(com.agri.pest.client.model.response.MyCropResponseDto cropDetail) {
        originalPlantName = cropDetail.getPlantName();
        originalStatus = cropDetail.getStatus();
        originalPlantingDate = cropDetail.getPlantingDate();
        originalMaturityDate = cropDetail.getMaturityDate();
        originalImageUrl = cropDetail.getImageUrl();
    }

    private void loadRecordsToCalendar(java.util.Map<String, java.util.List<com.agri.pest.client.model.response.CultivationRecordDto>> records) {
        if (records == null) return;
        calendarView.clearAllMarks();

        for (java.util.Map.Entry<String, java.util.List<com.agri.pest.client.model.response.CultivationRecordDto>> entry : records.entrySet()) {
            String tagType = entry.getKey();
            java.util.List<com.agri.pest.client.model.response.CultivationRecordDto> tagRecords = entry.getValue();
            if (tagRecords == null) continue;

            int type = getTypeFromTagType(tagType);
            if (type < 0) continue;

            for (com.agri.pest.client.model.response.CultivationRecordDto record : tagRecords) {
                if (record.getRecordDate() != null) {
                    Calendar recordDate = Calendar.getInstance();
                    recordDate.set(record.getRecordDate().getYear(),
                                   record.getRecordDate().getMonthValue() - 1,
                                   record.getRecordDate().getDayOfMonth());
                    int state = record.getStatus() != null && record.getStatus() == 1 ? STATE_COMPLETE : STATE_PENDING;
                    if (type == TYPE_NOTE && record.getContent() != null) {
                        calendarView.addMarkWithNote(recordDate, type, state, record.getContent());
                    } else {
                        calendarView.addMark(recordDate, type, state);
                    }
                }
            }
        }
    }

    private int getTypeFromTagType(String tagType) {
        if (tagType == null) return -1;
        switch (tagType) {
            case "WATERING": return TYPE_WATER;
            case "FERTILIZING": return TYPE_FERTILIZE;
            case "MEDICATION": return TYPE_MEDICINE;
            case "NOTE": return TYPE_NOTE;
            default: return -1;
        }
    }

    private void setupToolbar() {
        applyToolbarInsets();

        binding.ivBack.setOnClickListener(v -> {
            if (isEditMode) {
                cancelEditMode();
            }
            finish();
        });

        // 更换图片按钮
        binding.ivChangePhoto.setOnClickListener(v -> {
            if (imagePicker == null) {
                imagePicker = new ImagePickerUtil(this, uri -> {
                    selectedImageUri = uri;
                    ImageLoader.INSTANCE.load(binding.ivHeaderBg, uri.toString());
                    enterEditMode();
                    return null;
                });
            }
            imagePicker.showImageSourceDialog();
        });

        // 作物名称编辑 - 直接可编辑
        binding.etPlantNameLabel.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                enterEditMode();
            }
        });

        // 种植日期输入框焦点
        binding.etPlantDateValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                enterEditMode();
            }
        });


        // 保存按钮
        binding.btnSaveChanges.setOnClickListener(v -> saveChanges());

    }

    private void applyToolbarInsets() {
        if (toolbarBaseHeight <= 0) {
            toolbarBaseHeight = resolveActionBarSize();
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (view, windowInsets) -> {
            int statusBarInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            android.view.ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.height = toolbarBaseHeight + statusBarInset;
                view.setLayoutParams(layoutParams);
            }
            return windowInsets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.flBackContainer, (view, windowInsets) -> {
            int statusBarInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            CoordinatorLayout.LayoutParams layoutParams =
                    (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.topMargin = statusBarInset + dpToPx(8);
                view.setLayoutParams(layoutParams);
            }
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(binding.toolbar);
        ViewCompat.requestApplyInsets(binding.flBackContainer);
    }

    private int resolveActionBarSize() {
        TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(androidx.appcompat.R.attr.actionBarSize, typedValue, true)) {
            return TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());
        }
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                56,
                getResources().getDisplayMetrics()
        );
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void enterEditMode() {
        if (!isEditMode) {
            isEditMode = true;
            binding.btnSaveChanges.setVisibility(View.VISIBLE);
        }
    }

    private void cancelEditMode() {
        isEditMode = false;
        binding.btnSaveChanges.setVisibility(View.GONE);
        binding.etPlantNameLabel.setText(originalPlantName != null ? originalPlantName : "");
        setHealthSpinnerSelection(originalStatus);
        binding.etPlantDateValue.setText(formatDate(originalPlantingDate));
//        binding.etMatureValue.setText(formatDate(originalMaturityDate));
        if (originalImageUrl != null) {
            ImageLoader.INSTANCE.load(binding.ivHeaderBg, originalImageUrl);
        }
        selectedImageUri = null;
    }

    private void saveChanges() {
        String newName = binding.etPlantNameLabel.getText().toString().trim();
        String newStatus = getHealthSpinnerValue();
        String plantDateStr = binding.etPlantDateValue.getText().toString().trim();
//        String matureDateStr = binding.etMatureValue.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "请输入作物名称", Toast.LENGTH_SHORT).show();
            return;
        }

        // 解析日期（支持 yyyy.MM.dd 或 yyyy-MM-dd 格式）
        LocalDate plantingDate = parseDate(plantDateStr);

        // 判断是否有修改
        boolean hasChanges = !newName.equals(originalPlantName)
                || !newStatus.equals(originalStatus)
                || !java.util.Objects.equals(plantingDate, originalPlantingDate)
                || selectedImageUri != null;

        if (!hasChanges) {
            Toast.makeText(this, "没有修改内容", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.updateCropWithImage(this, plantId, selectedImageUri, newName, newStatus, plantingDate, originalMaturityDate);

        binding.etPlantNameLabel.clearFocus();
        binding.etPlantDateValue.clearFocus();
//        binding.etMatureValue.clearFocus();
    }


    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            String[] parts;
            if (dateStr.contains("-")) {
                parts = dateStr.split("-");
            } else if (dateStr.contains(".")) {
                parts = dateStr.split("\\.");
            } else {
                return null;
            }
            if (parts.length >= 3) {
                return LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
        } catch (Exception e) {
            LogUtils.INSTANCE.e("PlantManageActivity", e);
        }
        return null;
    }

    private void setupTabs() {
        // 为每个Tab设置点击监听
        binding.tabWater.setOnClickListener(v -> selectTab(0));
        binding.tabFertilize.setOnClickListener(v -> selectTab(1));
        binding.tabMedicine.setOnClickListener(v -> selectTab(2));
        binding.tabNote.setOnClickListener(v -> selectTab(3));
    }

    private void selectTab(int position) {
        if (position < 0 || position >= 4) return;
        currentTabPosition = position;

        // 更新Tab选中状态
        updateTabSelection(position);

        // 更新日历显示的标记类型（显示所有状态）
        calendarView.setMarkType(position);
        calendarView.setMarkState(-1);

        // 更新底部按钮显示
        updateButtons(position);

        // 重置状态选择（不选中任何按钮）
        selectedType = -1;
        selectedState = -1;
        resetAllButtonStyles();
    }


    private void updateTabSelection(int selectedPosition) {
        LinearLayout[] tabs = {
                binding.tabWater,
                binding.tabFertilize,
                binding.tabMedicine,
                binding.tabNote
        };

        TextView[] tabTexts = {
                binding.tvTabWater,
                binding.tvTabFertilize,
                binding.tvTabMedicine,
                binding.tvTabNote
        };

        for (int i = 0; i < tabs.length; i++) {
            boolean isSelected = (i == selectedPosition);
            tabs[i].setBackgroundResource(isSelected ? R.drawable.bg_tab_selected : android.R.color.transparent);
            tabTexts[i].setTextColor(ContextCompat.getColor(this,
                    isSelected ? R.color.tab_text_selected : R.color.tab_text_normal));
        }
    }

    private void updateButtons(int selectedPosition) {
        // 隐藏所有按钮组
        binding.llWaterButtons.setVisibility(View.GONE);
        binding.llFertilizeButtons.setVisibility(View.GONE);
        binding.llMedicineButtons.setVisibility(View.GONE);
        binding.llNoteButtons.setVisibility(View.GONE);

        // 显示对应按钮组
        switch (selectedPosition) {
            case TYPE_WATER:
                binding.llWaterButtons.setVisibility(View.VISIBLE);
                break;
            case TYPE_FERTILIZE:
                binding.llFertilizeButtons.setVisibility(View.VISIBLE);
                break;
            case TYPE_MEDICINE:
                binding.llMedicineButtons.setVisibility(View.VISIBLE);
                break;
            case TYPE_NOTE:
                binding.llNoteButtons.setVisibility(View.VISIBLE);
                break;
        }
    }


    private void setupCalendar() {
        calendarView = new PlantCalendarView(
                this,
                binding.calendarView,
                binding.tvCurrentMonth,
                binding.ivPrevMonth,
                binding.ivNextMonth
        );

        // 日期选中监听
        calendarView.setOnDateSelectedListener(date -> {
            int existingState = calendarView.getMarkState(date, currentTabPosition);
            if (existingState >= 0) {
                selectedType = currentTabPosition;
                selectedState = existingState;
                updateStateButtonSelection(currentTabPosition);
                if (currentTabPosition == TYPE_NOTE) {
                    LocalDate localDate = LocalDate.of(date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));
                    String noteContent = viewModel.getNoteContentByDate(localDate);
                    if (noteContent != null) binding.etNoteContent.setText(noteContent);
                }
            } else if (selectedType >= 0 && selectedState >= 0) {
                selectedType = -1;
                selectedState = -1;
                resetAllButtonStyles();
            }
        });
    }


    private void setupButtons() {
        // 浇水按钮
        binding.btnAddWaterDone.setOnClickListener(v -> {
            Calendar selectedDate = calendarView.getSelectedDate();
            int existingState = calendarView.getMarkState(selectedDate, TYPE_WATER);
            if (existingState >= 0) {
                Toast.makeText(this, "该日期已有浇水标签，请先取消", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedType == TYPE_WATER && selectedState == STATE_COMPLETE) {
                selectedType = -1;
                selectedState = -1;
            } else {
                calendarView.cancelPreparedMark();
                calendarView.clearSelectedDateMark();
                selectedType = TYPE_WATER;
                selectedState = STATE_COMPLETE;
            }
            updateStateButtonSelection(TYPE_WATER);
        });

        binding.btnAddWaterPending.setOnClickListener(v -> {
            Calendar selectedDate = calendarView.getSelectedDate();
            int existingState = calendarView.getMarkState(selectedDate, TYPE_WATER);
            if (existingState >= 0) {
                Toast.makeText(this, "该日期已有浇水标签，请先取消", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedType == TYPE_WATER && selectedState == STATE_PENDING) {
                selectedType = -1;
                selectedState = -1;
            } else {
                calendarView.cancelPreparedMark();
                calendarView.clearSelectedDateMark();
                selectedType = TYPE_WATER;
                selectedState = STATE_PENDING;
            }
            updateStateButtonSelection(TYPE_WATER);
        });

        binding.btnConfirmWater.setOnClickListener(v -> confirmTag(TYPE_WATER));
        binding.btnCancelWater.setOnClickListener(v -> cancelTag(TYPE_WATER));

        // 施肥按钮
        binding.btnAddFertilizeDone.setOnClickListener(v -> {
            Calendar selectedDate = calendarView.getSelectedDate();
            int existingState = calendarView.getMarkState(selectedDate, TYPE_FERTILIZE);
            if (existingState >= 0) {
                Toast.makeText(this, "该日期已有施肥标签，请先取消", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedType == TYPE_FERTILIZE && selectedState == STATE_COMPLETE) {
                selectedType = -1;
                selectedState = -1;
            } else {
                calendarView.cancelPreparedMark();
                calendarView.clearSelectedDateMark();
                selectedType = TYPE_FERTILIZE;
                selectedState = STATE_COMPLETE;
            }
            updateStateButtonSelection(TYPE_FERTILIZE);
        });

        binding.btnAddFertilizePending.setOnClickListener(v -> {
            Calendar selectedDate = calendarView.getSelectedDate();
            int existingState = calendarView.getMarkState(selectedDate, TYPE_FERTILIZE);
            if (existingState >= 0) {
                Toast.makeText(this, "该日期已有施肥标签，请先取消", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedType == TYPE_FERTILIZE && selectedState == STATE_PENDING) {
                selectedType = -1;
                selectedState = -1;
            } else {
                calendarView.cancelPreparedMark();
                calendarView.clearSelectedDateMark();
                selectedType = TYPE_FERTILIZE;
                selectedState = STATE_PENDING;
            }
            updateStateButtonSelection(TYPE_FERTILIZE);
        });

        binding.btnConfirmFertilize.setOnClickListener(v -> confirmTag(TYPE_FERTILIZE));
        binding.btnCancelFertilize.setOnClickListener(v -> cancelTag(TYPE_FERTILIZE));

        // 用药按钮
        binding.btnAddMedicineDone.setOnClickListener(v -> {
            Calendar selectedDate = calendarView.getSelectedDate();
            int existingState = calendarView.getMarkState(selectedDate, TYPE_MEDICINE);
            if (existingState >= 0) {
                Toast.makeText(this, "该日期已有用药标签，请先取消", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedType == TYPE_MEDICINE && selectedState == STATE_COMPLETE) {
                selectedType = -1;
                selectedState = -1;
            } else {
                calendarView.cancelPreparedMark();
                calendarView.clearSelectedDateMark();
                selectedType = TYPE_MEDICINE;
                selectedState = STATE_COMPLETE;
            }
            updateStateButtonSelection(TYPE_MEDICINE);
        });

        binding.btnAddMedicinePending.setOnClickListener(v -> {
            Calendar selectedDate = calendarView.getSelectedDate();
            int existingState = calendarView.getMarkState(selectedDate, TYPE_MEDICINE);
            if (existingState >= 0) {
                Toast.makeText(this, "该日期已有用药标签，请先取消", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedType == TYPE_MEDICINE && selectedState == STATE_PENDING) {
                selectedType = -1;
                selectedState = -1;
            } else {
                calendarView.cancelPreparedMark();
                calendarView.clearSelectedDateMark();
                selectedType = TYPE_MEDICINE;
                selectedState = STATE_PENDING;
            }
            updateStateButtonSelection(TYPE_MEDICINE);
        });

        binding.btnConfirmMedicine.setOnClickListener(v -> confirmTag(TYPE_MEDICINE));
        binding.btnCancelMedicine.setOnClickListener(v -> cancelTag(TYPE_MEDICINE));

        // 笔记按钮
        binding.btnAddNoteDone.setOnClickListener(v -> {
            Calendar selectedDate = calendarView.getSelectedDate();
            int existingState = calendarView.getMarkState(selectedDate, TYPE_NOTE);
            if (existingState >= 0) {
                Toast.makeText(this, "该日期已有笔记，请先取消", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedType == TYPE_NOTE) {
                selectedType = -1;
                selectedState = -1;
                updateNoteButtonSelection(false);
            } else {
                calendarView.cancelPreparedMark();
                calendarView.clearSelectedDateMark();
                selectedType = TYPE_NOTE;
                selectedState = STATE_COMPLETE;
                updateNoteButtonSelection(true);
            }
        });

        binding.btnConfirmNote.setOnClickListener(v -> {
            String content = binding.etNoteContent.getText().toString().trim();
            if (selectedType != TYPE_NOTE) {
                Toast.makeText(this, "请先选择笔记", Toast.LENGTH_SHORT).show();
                return;
            }
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入笔记内容", Toast.LENGTH_SHORT).show();
                return;
            }
            Calendar selectedDate = calendarView.getSelectedDate();
            LocalDate recordDate = LocalDate.of(selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH));
            viewModel.addTag(plantId, getTagTypeString(TYPE_NOTE), recordDate, content, 1);
            calendarView.setPreparedMark(selectedDate, TYPE_NOTE, STATE_COMPLETE);
            calendarView.confirmPreparedMark();
            Toast.makeText(this, "已打上【笔记】标签", Toast.LENGTH_SHORT).show();
            binding.etNoteContent.setText("");
        });

        binding.btnCancelNote.setOnClickListener(v -> cancelTag(TYPE_NOTE));
    }

    private void confirmTag(int type) {
        Calendar selectedDate = calendarView.getSelectedDate();
        if (selectedType == type && selectedState >= 0) {
            LocalDate recordDate = LocalDate.of(selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH));
            viewModel.addTag(plantId, getTagTypeString(selectedType), recordDate, null, getStatusValue(selectedState));
            calendarView.setPreparedMark(selectedDate, selectedType, selectedState);
            calendarView.confirmPreparedMark();
            calendarView.setMarkType(selectedType);
            Toast.makeText(this, "已打上标签", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "请先选择已或待", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelTag(int type) {
        Calendar selectedDate = calendarView.getSelectedDate();
        LocalDate recordDate = LocalDate.of(selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH));
        int cancelStatus = selectedState == STATE_COMPLETE ? 0 : 1;
        viewModel.cancelTag(plantId, getTagTypeString(type), recordDate, cancelStatus);
        calendarView.cancelPreparedMark();
        calendarView.removeMark(selectedDate, type, selectedState);
        String stateLabel = type == TYPE_WATER ? "浇水" : (type == TYPE_FERTILIZE ? "施肥" : (type == TYPE_MEDICINE ? "用药" : "笔记"));
        Toast.makeText(this, "已取消【" + stateLabel + "】标签", Toast.LENGTH_SHORT).show();
        selectedType = -1;
        selectedState = -1;
        resetAllButtonStyles();
    }

    private void updateStateButtonSelection(int type) {
        resetAllButtonStyles();
        if (selectedType != type || selectedState < 0) return;

        if (type == TYPE_WATER) {
            if (selectedState == STATE_COMPLETE) {
                setButtonSelected(binding.btnAddWaterDone, true, true);
            } else {
                setButtonSelected(binding.btnAddWaterPending, true, false);
            }
        } else if (type == TYPE_FERTILIZE) {
            if (selectedState == STATE_COMPLETE) {
                setButtonSelected(binding.btnAddFertilizeDone, true, true);
            } else {
                setButtonSelected(binding.btnAddFertilizePending, true, false);
            }
        } else if (type == TYPE_MEDICINE) {
            if (selectedState == STATE_COMPLETE) {
                setButtonSelected(binding.btnAddMedicineDone, true, true);
            } else {
                setButtonSelected(binding.btnAddMedicinePending, true, false);
            }
        } else if (type == TYPE_NOTE) {
            updateNoteButtonSelection(true);
        }
    }


    private void resetAllButtonStyles() {
        setButtonSelected(binding.btnAddWaterDone, false, true);
        setButtonSelected(binding.btnAddWaterPending, false, false);
        setButtonSelected(binding.btnAddFertilizeDone, false, true);
        setButtonSelected(binding.btnAddFertilizePending, false, false);
        setButtonSelected(binding.btnAddMedicineDone, false, true);
        setButtonSelected(binding.btnAddMedicinePending, false, false);
        updateNoteButtonSelection(false);
    }

    private void updateNoteButtonSelection(boolean selected) {
        if (binding.btnAddNoteDone instanceof MaterialButton) {
            MaterialButton mb = (MaterialButton) binding.btnAddNoteDone;
            if (selected) {
                mb.setBackgroundColor(getColor(R.color.tab_text_selected));
                mb.setTextColor(getColor(android.R.color.white));
                mb.setIconResource(R.drawable.ic_card_notebook);
            } else {
                mb.setBackgroundColor(getColor(R.color.btn_gray));
                mb.setTextColor(getColor(R.color.btn_text_dark));
                mb.setIconResource(R.drawable.ic_card_not_notebook);
            }
        }
    }


    private void setButtonSelected(android.widget.Button button, boolean selected, boolean isDone) {
        if (button instanceof MaterialButton) {
            MaterialButton mb = (MaterialButton) button;
            if (selected) {
                // 选中状态：根据类型显示不同颜色
                if (isDone) {
                    // 已类型按钮：主题色 #01CFAC
                    mb.setBackgroundColor(getColor(R.color.tab_text_selected));
                    mb.setTextColor(getColor(android.R.color.white));
                } else {
                    // 待类型按钮：橙色 #FFB05D
                    mb.setBackgroundColor(getColor(R.color.orange_selected));
                    mb.setTextColor(getColor(android.R.color.white));
                }
            } else {
                // 非选中状态：灰色背景，深色文字
                mb.setBackgroundColor(getColor(R.color.btn_gray));
                mb.setTextColor(getColor(R.color.btn_text_dark));
            }
            // 更新按钮图标
            updateButtonIcon(mb, button.getId(), selected, isDone);
        }
    }

    private void updateButtonIcon(MaterialButton mb, int buttonId, boolean selected, boolean isDone) {
        int iconRes;
        if (buttonId == R.id.btn_add_water_done) {
            iconRes = R.drawable.ic_card_water;
        } else if (buttonId == R.id.btn_add_water_pending) {
            iconRes = selected ? R.drawable.ic_card_not_water_select : R.drawable.ic_card_not_water;
        } else if (buttonId == R.id.btn_add_fertilize_done) {
            iconRes = R.drawable.ic_card_manure;
        } else if (buttonId == R.id.btn_add_fertilize_pending) {
            iconRes = R.drawable.ic_card_not_manure;
        } else if (buttonId == R.id.btn_add_medicine_done) {
            iconRes = R.drawable.ic_card_medicine;
        } else if (buttonId == R.id.btn_add_medicine_pending) {
            iconRes = R.drawable.ic_card_not_medicine;
        } else if (buttonId == R.id.btn_add_note_done) {
            iconRes = selected ? R.drawable.ic_card_notebook : R.drawable.ic_card_not_notebook;
        } else {
            return;
        }
        // 清除 tint 并设置新图标
        mb.setIconTint(null);
        mb.setIconResource(iconRes);
    }

    @Override
    public void initData() {
        viewModel.getCropDetail(plantId);
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return String.format("%d.%02d.%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    private String getTagTypeString(int type) {
        if (type == TYPE_WATER) {
            return "WATERING";
        } else if (type == TYPE_FERTILIZE) {
            return "FERTILIZING";
        } else if (type == TYPE_MEDICINE) {
            return "MEDICATION";
        } else if (type == TYPE_NOTE) {
            return "NOTE";
        }
        return "WATERING";
    }

    private int getStatusValue(int state) {
        return state == STATE_COMPLETE ? 1 : 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (calendarView != null) {
            calendarView.onDestroy();
        }
    }
}
