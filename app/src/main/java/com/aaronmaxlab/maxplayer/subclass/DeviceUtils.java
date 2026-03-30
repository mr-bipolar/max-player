package com.aaronmaxlab.maxplayer.subclass;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;

public class DeviceUtils {

    public static boolean isTv(Context context) {
        return (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_TYPE_MASK)
                == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static void applyBlur(View targetView, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetView != null) {

            RenderEffect blurEffect = RenderEffect.createBlurEffect(
                    radius,
                    radius,
                    Shader.TileMode.CLAMP
            );

            targetView.setRenderEffect(blurEffect);
        }
    }
}
