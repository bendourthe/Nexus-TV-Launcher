package com.cleantv.launcher;

import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class TvInputHelper {

    public static class InputItem {
        public final String id;
        public final String label;
        public final int type;

        public InputItem(String id, String label, int type) {
            this.id = id;
            this.label = label;
            this.type = type;
        }
    }

    public static List<InputItem> getAvailableInputs(Context context) {
        List<InputItem> result = new ArrayList<>();
        try {
            TvInputManager tim = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
            if (tim != null) {
                List<TvInputInfo> list = tim.getTvInputList();
                for (TvInputInfo info : list) {
                    if (info.isPassthroughInput()) {
                        CharSequence label = info.loadLabel(context);
                        String displayLabel = label != null ? label.toString() : "Video Input";
                        result.add(new InputItem(info.getId(), displayLabel, info.getType()));
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        // Add standard hardware HDMI and Tuner defaults if system list was empty
        if (result.isEmpty()) {
            result.add(new InputItem("HDMI 1", "HDMI 1", TvInputInfo.TYPE_HDMI));
            result.add(new InputItem("HDMI 2", "HDMI 2 (eARC)", TvInputInfo.TYPE_HDMI));
            result.add(new InputItem("HDMI 3", "HDMI 3", TvInputInfo.TYPE_HDMI));
            result.add(new InputItem("Live TV", "Live TV (Antenna/Cable)", TvInputInfo.TYPE_TUNER));
            result.add(new InputItem("AV", "Composite / AV", TvInputInfo.TYPE_COMPOSITE));
        }
        return result;
    }

    public static void switchToInput(Context context, InputItem item) {
        try {
            if (item.id.contains(".")) {
                Uri uri = TvContract.buildChannelUriForPassthroughInput(item.id);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }
        } catch (Throwable ignored) {
        }

        // Fallback: Launch native TV input selector or Live TV app
        try {
            Intent liveTv = context.getPackageManager().getLaunchIntentForPackage("com.mediatek.wwtv.tvcenter");
            if (liveTv != null) {
                liveTv.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(liveTv);
                return;
            }
            Intent hisenseInput = context.getPackageManager().getLaunchIntentForPackage("com.vt.tv.input");
            if (hisenseInput != null) {
                hisenseInput.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(hisenseInput);
            }
        } catch (Throwable ignored) {
        }
    }
}
