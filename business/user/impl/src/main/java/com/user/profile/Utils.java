package com.user.profile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.common.utils.ThreadUtils;
import com.user.R;

public class Utils {
    private static Dialog currentDialog;

    public static void showDialog(Activity activity, String mes) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        
        // 如果已有 Dialog 在显示，先 dismiss
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        
        Dialog dialog = new Dialog(activity);
        currentDialog = dialog;
        
        dialog.setContentView(R.layout.dialog_edit_profile);
        TextView tv = dialog.findViewById(R.id.dialog_setOk);
        tv.setText(mes);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        
        // 使用 Lifecycle 监听 Activity 销毁，自动 dismiss Dialog
        if (activity instanceof LifecycleOwner) {
            ((LifecycleOwner) activity).getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (currentDialog == dialog) {
                        currentDialog = null;
                    }
                }
            });
        }
        
        // 延迟 dismiss 也需要检查 Activity 状态
        ThreadUtils.INSTANCE.runOnUiThreadDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing() && !activity.isFinishing() && !activity.isDestroyed()) {
                    dialog.dismiss();
                }
                if (currentDialog == dialog) {
                    currentDialog = null;
                }
            }
        }, 1000);
    }
}
