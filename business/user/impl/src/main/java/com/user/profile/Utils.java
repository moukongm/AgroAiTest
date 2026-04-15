package com.user.profile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.common.utils.ThreadUtils;
import com.user.R;

public class Utils {
    public static void showDialog(Activity activity, String mes) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_edit_profile);
        TextView tv = dialog.findViewById(R.id.dialog_setOk);
        tv.setText(mes);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        ThreadUtils.INSTANCE.runOnUiThreadDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, 1000);
    }
}
