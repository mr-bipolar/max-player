package com.aaronmaxlab.maxplayer.subclass;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.aaronmaxlab.maxplayer.R;

public class TvFocusHelper {

    public static void applyFocus(View... views) {

        View.OnFocusChangeListener listener = (v, hasFocus) -> {

            if (hasFocus) {

                v.animate()
                        .scaleX(1.08f)
                        .scaleY(1.08f)
                        .setDuration(150)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();

                // Add glow background
                v.setForeground(v.getContext().getDrawable(R.drawable.bg_glass_focus));

            } else {

                v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();

                v.setForeground(null);
            }
        };

        for (View view : views) {
            if (view != null) {
                view.setOnFocusChangeListener(listener);
            }
        }
    }

}
