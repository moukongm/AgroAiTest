package com.user.profile;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.common.utils.ThreadUtils;
import com.user.R;

public class Utils {
    public static void showDialog(Context context, String mes) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_edit_profile);
        TextView tv = dialog.findViewById(R.id.dialog_setOk);
        tv.setText(mes);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        ThreadUtils.INSTANCE.runOnUiThreadDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 1000);
    }
}
