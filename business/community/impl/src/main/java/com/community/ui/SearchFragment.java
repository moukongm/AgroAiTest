package com.community.ui;


import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.community.R;
import com.community.databinding.FragmentSearchBinding;
import com.community.ui.adapter.SearchHistoryAdapter;
import com.community.ui.adapter.SearchSuggestionAdapter;
import com.community.viewmodel.SearchViewModel;

import java.util.Collections;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;


public class SearchFragment extends BaseFragment<FragmentSearchBinding> {

    private SearchListener searchListener;
    private VoiceSearchListener voiceSearchListener;
    private SearchViewModel viewModel;
    private SearchHistoryAdapter historyAdapter;
    private SearchSuggestionAdapter suggestionAdapter;

    public interface SearchListener {
        void onSearch(String keyword);
        void onClose();
        void onShowSearchResult(String keyword);
    }

    public interface VoiceSearchListener {
        void onOpenVoiceSearch();
    }

    public void setSearchListener(SearchListener listener) {
        this.searchListener = listener;
    }

    public void setVoiceSearchListener(VoiceSearchListener listener) {
        this.voiceSearchListener = listener;
    }

    @Override
    public FragmentSearchBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentSearchBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        if (!isAdded() || getActivity() == null) return;
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        viewModel.init(requireActivity().getApplication());

        setupRecyclerViews();
        setupSearchInput();
        setupBlurView();
        showHistoryView();
        getBinding().ivClear.setOnClickListener(v -> viewModel.clearHistory());

    }

    @Override
    public void initData() {
        viewModel.getHistoryLiveData().observe(this, history -> {
            historyAdapter.setList(history != null ? history : Collections.emptyList());
        });
        viewModel.getSuggestionsLiveData().observe(this, suggestions -> {
            suggestionAdapter.setList(suggestions != null ? suggestions : Collections.emptyList());
        });
    }

    private void setupRecyclerViews() {
        LinearLayoutManager historyLayoutManager = new LinearLayoutManager(requireContext());
        historyLayoutManager.setAutoMeasureEnabled(false);
        getBinding().rvHistory.setLayoutManager(historyLayoutManager);
        LinearLayoutManager suggestionLayoutManager = new LinearLayoutManager(requireContext());
        suggestionLayoutManager.setAutoMeasureEnabled(false);
        getBinding().rvSuggestions.setLayoutManager(suggestionLayoutManager);
        historyAdapter = new SearchHistoryAdapter();
        historyAdapter.setOnItemClickListener((keyword, position) -> {
            getBinding().etSearch.setText(keyword);
            getBinding().etSearch.setSelection(keyword.length());
            performSearch(keyword);
        });
        historyAdapter.setOnDeleteClickListener((keyword, position) -> {
            viewModel.deleteHistoryItem(keyword);
        });
        getBinding().rvHistory.setAdapter(historyAdapter);
        suggestionAdapter = new SearchSuggestionAdapter();
        suggestionAdapter.setOnItemClickListener((keyword, position) -> {
            getBinding().etSearch.setText(keyword);
            getBinding().etSearch.setSelection(keyword.length());
            performSearch(keyword);
        });
        getBinding().rvSuggestions.setAdapter(suggestionAdapter);
    }
    private void setupSearchInput() {
        getBinding().etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showHistoryView();
                } else {
                    showSuggestionView();
                    viewModel.onQueryChanged(query);
                }
            }
        });
        getBinding().etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = getSearchKeyword();
                if (!keyword.isEmpty()) {
                    performSearch(keyword);
                }
                return true;
            }
            return false;
        });
        getBinding().btnCancel.setOnClickListener(v -> close());

        getBinding().btnInputMic.setOnClickListener(v -> openVoiceSearch());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded() || getContext() == null) return;
            FragmentSearchBinding b = getBindingSafe();
            if (b == null) return;
            EditText et = b.etSearch;
            et.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 150);
    }
    private void showHistoryView() {
        getBinding().rvHistory.setVisibility(View.VISIBLE);
        getBinding().ivWantSearch.setVisibility(View.VISIBLE);
        getBinding().rvSuggestions.setVisibility(View.GONE);
    }

    private void showSuggestionView() {
        getBinding().rvHistory.setVisibility(View.VISIBLE);
        getBinding().ivWantSearch.setVisibility(View.VISIBLE);
        getBinding().rvSuggestions.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard() {
        Context ctx = getContext();
        if (ctx == null) return;
        FragmentSearchBinding b = getBindingSafe();
        if (b == null) return;
        InputMethodManager imm = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && b.etSearch != null) {
            imm.hideSoftInputFromWindow(b.etSearch.getWindowToken(), 0);
        }
    }
    public void close() {
        hideKeyboard();
        FragmentSearchBinding b = getBindingSafe();
        if (b != null) {
            b.etSearch.setText("");
        }
        if (searchListener != null) {
            searchListener.onClose();
        }
    }
    public String getSearchKeyword() {
        FragmentSearchBinding b = getBindingSafe();
        if (b == null) return "";
        return b.etSearch.getText().toString().trim();
    }

    private void performSearch(String keyword) {
        hideKeyboard();
        viewModel.saveToHistory(keyword);
        if (searchListener != null) {
            searchListener.onSearch(keyword);
        }
    }
    private void setupBlurView() {
        if (!isAdded() || getActivity() == null) return;
        BlurView blurView = getBinding().blurMask;
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);

        float radius = 15f;

        blurView.setupWith(rootView, new RenderScriptBlur(requireContext()))
                .setBlurRadius(radius)
                .setOverlayColor(0x40000000);
    }


    public void openVoiceSearch() {
        if (voiceSearchListener != null) {
            voiceSearchListener.onOpenVoiceSearch();
            hideKeyboard();
        }
    }
    @Override
    public void onDestroyView() {
        FragmentSearchBinding b = getBindingSafe();
        if (b != null) {
            if (b.rvHistory != null) {
                b.rvHistory.setAdapter(null);
            }
            if (b.rvSuggestions != null) {
                b.rvSuggestions.setAdapter(null);
            }
        }
        historyAdapter = null;
        suggestionAdapter = null;
        super.onDestroyView();
    }
}