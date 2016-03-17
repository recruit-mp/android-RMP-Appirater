/*
 * Copyright (C) 2015-2016 Recruit Marketing Partners Co.,Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.recruit_mp.android.rmp_appirater;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;

public class RmpAppirater {
    private static final String TAG = "RmpAppirater";

    // Pref keys
    private static final String PREF_KEY_APP_LAUNCH_COUNT = "PREF_KEY_APP_LAUNCH_COUNT";
    private static final String PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT = "PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT";
    private static final String PREF_KEY_APP_FIRST_LAUNCHED_DATE = "PREF_KEY_APP_FIRST_LAUNCHED_DATE";
    private static final String PREF_KEY_APP_VERSION_CODE = "PREF_KEY_APP_VERSION_CODE";
    private static final String PREF_KEY_RATE_CLICK_DATE = "PREF_KEY_RATE_CLICK_DATE";
    private static final String PREF_KEY_REMINDER_CLICK_DATE = "PREF_KEY_REMINDER_CLICK_DATE";
    private static final String PREF_KEY_DO_NOT_SHOW_AGAIN = "PREF_KEY_DO_NOT_SHOW_AGAIN";

    private static final String PREFS_PACKAGE_NAME_SUFFIX = ".RmpAppirater";

    /**
     * Tells RMP-Appirater that the app has launched.
     * <p/>
     * Show rating dialog if user isn't rating yet and don't select "Not show again".
     *
     * @param context Context
     */
    public static void appLaunched(Context context) {
        appLaunched(context, null, null, null);
    }

    /**
     * Tells RMP-Appirater that the app has launched.
     * <p/>
     * Rating dialog is shown after calling this method.
     *
     * @param context                 Context
     * @param showRateDialogCondition Showing rate dialog condition.
     */
    public static void appLaunched(Context context, ShowRateDialogCondition showRateDialogCondition) {
        appLaunched(context, showRateDialogCondition, null, null);
    }

    /**
     * Tells RMP-Appirater that the app has launched.
     * <p/>
     * Show rating dialog if user isn't rating yet and don't select "Not show again".
     *
     * @param context Context
     * @param options RMP-Appirater options.
     */
    public static void appLaunched(Context context, Options options) {
        appLaunched(context, null, options, null);
    }

    /**
     * Tells RMP-Appirater that the app has launched.
     * <p/>
     * Rating dialog is shown after calling this method.
     *
     * @param context                 Context
     * @param showRateDialogCondition Showing rate dialog condition.
     * @param options                 RMP-Appirater options.
     */
    public static void appLaunched(Context context, ShowRateDialogCondition showRateDialogCondition, Options options) {
        appLaunched(context, showRateDialogCondition, options, null);
    }

    /**
     * Tells RMP-Appirater that the app has launched.
     * <p/>
     * Rating dialog is shown after calling this method.
     *
     * @param context                 Context
     * @param showRateDialogCondition Showing rate dialog condition.
     * @param options                 RMP-Appirater options.
     * @param onCompleteListener      Listener which be called after process of review dialog finished.
     */
    public static void appLaunched(Context context, ShowRateDialogCondition showRateDialogCondition, Options options, OnCompleteListener onCompleteListener) {
        // Set default show rate dialog condition.
        if (showRateDialogCondition == null) {
            showRateDialogCondition = new ShowRateDialogCondition() {
                @Override
                public boolean isShowRateDialog(long appLaunchCount, long appThisVersionCodeLaunchCount,
                                                long firstLaunchDate, int appVersionCode, int previousAppVersionCode,
                                                Date rateClickDate, Date reminderClickDate, boolean doNotShowAgain) {
                    // Show rating dialog if user isn't rating yet and don't select "Not show again".
                    return (rateClickDate == null && !doNotShowAgain);
                }
            };
        }

        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        // Load appThisVersionCodeLaunchCount
        long appLaunchCount = prefs.getLong(PREF_KEY_APP_LAUNCH_COUNT, 0);
        // Load appThisVersionCodeLaunchCount
        long appThisVersionCodeLaunchCount = prefs.getLong(PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT, 0);
        // Load firstLaunchDate
        long firstLaunchDate = prefs.getLong(PREF_KEY_APP_FIRST_LAUNCHED_DATE, 0);
        // Load appVersionCode and prefsAppVersionCode
        int appVersionCode = Integer.MIN_VALUE;
        final int previousAppVersionCode = prefs.getInt(PREF_KEY_APP_VERSION_CODE, Integer.MIN_VALUE);
        try {
            appVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            if (previousAppVersionCode != appVersionCode) {
                // Reset appThisVersionCodeLaunchCount
                appThisVersionCodeLaunchCount = 0;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Occurred PackageManager.NameNotFoundException", e);
        }
        // Load rateClickDate
        final long rateClickDateMills = prefs.getLong(PREF_KEY_RATE_CLICK_DATE, 0);
        final Date rateClickDate = (rateClickDateMills > 0) ? new Date(rateClickDateMills) : null;
        // Load reminderClickDate
        final long reminderClickDateMills = prefs.getLong(PREF_KEY_REMINDER_CLICK_DATE, 0);
        final Date reminderClickDate = (reminderClickDateMills > 0) ? new Date(reminderClickDateMills) : null;
        // Load doNotShowAgain
        final boolean doNotShowAgain = prefs.getBoolean(PREF_KEY_DO_NOT_SHOW_AGAIN, false);

        // Increment appLaunchCount
        ++appLaunchCount;
        prefsEditor.putLong(PREF_KEY_APP_LAUNCH_COUNT, appLaunchCount);

        // Increment appThisVersionCodeLaunchCount
        ++appThisVersionCodeLaunchCount;
        prefsEditor.putLong(PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT, appThisVersionCodeLaunchCount);

        // Set app first launch date.
        if (firstLaunchDate == 0) {
            firstLaunchDate = System.currentTimeMillis();
            prefsEditor.putLong(PREF_KEY_APP_FIRST_LAUNCHED_DATE, firstLaunchDate);
        }

        // Set app version code
        if (appVersionCode != Integer.MIN_VALUE) {
            prefsEditor.putInt(PREF_KEY_APP_VERSION_CODE, appVersionCode);
        }

        prefsEditor.commit();

        if (showRateDialogCondition.isShowRateDialog(appLaunchCount, appThisVersionCodeLaunchCount, firstLaunchDate,
                appVersionCode, previousAppVersionCode, rateClickDate, reminderClickDate, doNotShowAgain)) {
            showRateDialog(context, options, onCompleteListener);
        } else {
            if (onCompleteListener != null) {
                onCompleteListener.onNotShownDialog();
            }
        }
    }

    /**
     * Show rating dialog.
     * The dialog will be showed if the user hasn't declined to rate or hasn't rated current version.
     *
     * @param context            Context
     * @param onCompleteListener Listener which be called after process of review dialog finished.
     */
    public static void tryToShowPrompt(Context context, OnCompleteListener onCompleteListener) {
        tryToShowPrompt(context, null, null, onCompleteListener);
    }

    /**
     * Show rating dialog.
     * <p/>
     * The dialog will be showed if the user hasn't declined to rate or hasn't rated current version.
     *
     * @param context            Context
     * @param options            RMP-Appirater options.
     * @param onCompleteListener Listener which be called after process of review dialog finished.
     */
    public static void tryToShowPrompt(Context context, Options options, OnCompleteListener onCompleteListener) {
        tryToShowPrompt(context, null, options, onCompleteListener);
    }

    /**
     * Show rating dialog.
     *
     * @param context                 Context
     * @param showRateDialogCondition Showing rate dialog condition.
     * @param options                 RMP-Appirater options.
     * @param onCompleteListener      Listener which be called after process of review dialog finished.
     */
    public static void tryToShowPrompt(Context context, ShowRateDialogCondition showRateDialogCondition, Options options, OnCompleteListener onCompleteListener) {
        // Set default show rate dialog condition.
        if (showRateDialogCondition == null) {
            showRateDialogCondition = new ShowRateDialogCondition() {
                @Override
                public boolean isShowRateDialog(long appLaunchCount, long appThisVersionCodeLaunchCount,
                                                long firstLaunchDate, int appVersionCode, int previousAppVersionCode,
                                                Date rateClickDate, Date reminderClickDate, boolean doNotShowAgain) {
                    // Show rating dialog if user isn't rating yet and don't select "Not show again".
                    return (rateClickDate == null && !doNotShowAgain);
                }
            };
        }

        SharedPreferences prefs = getSharedPreferences(context);

        // Load appThisVersionCodeLaunchCount
        long appLaunchCount = prefs.getLong(PREF_KEY_APP_LAUNCH_COUNT, 0);
        // Load appThisVersionCodeLaunchCount
        long appThisVersionCodeLaunchCount = prefs.getLong(PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT, 0);
        // Load firstLaunchDate
        long firstLaunchDate = prefs.getLong(PREF_KEY_APP_FIRST_LAUNCHED_DATE, 0);
        // Load appVersionCode and prefsAppVersionCode
        int appVersionCode = Integer.MIN_VALUE;
        final int previousAppVersionCode = prefs.getInt(PREF_KEY_APP_VERSION_CODE, Integer.MIN_VALUE);
        try {
            appVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            if (previousAppVersionCode != appVersionCode) {
                // Reset appThisVersionCodeLaunchCount
                appThisVersionCodeLaunchCount = 0;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Occurred PackageManager.NameNotFoundException", e);
        }
        // Load rateClickDate
        final long rateClickDateMills = prefs.getLong(PREF_KEY_RATE_CLICK_DATE, 0);
        final Date rateClickDate = (rateClickDateMills > 0) ? new Date(rateClickDateMills) : null;
        // Load reminderClickDate
        final long reminderClickDateMills = prefs.getLong(PREF_KEY_REMINDER_CLICK_DATE, 0);
        final Date reminderClickDate = (reminderClickDateMills > 0) ? new Date(reminderClickDateMills) : null;
        // Load doNotShowAgain
        final boolean doNotShowAgain = prefs.getBoolean(PREF_KEY_DO_NOT_SHOW_AGAIN, false);

        if (showRateDialogCondition.isShowRateDialog(appLaunchCount, appThisVersionCodeLaunchCount, firstLaunchDate,
                appVersionCode, previousAppVersionCode, rateClickDate, reminderClickDate, doNotShowAgain)) {
            showRateDialog(context, options, onCompleteListener);
        } else {
            if (onCompleteListener != null) {
                onCompleteListener.onNotShownDialog();
            }
        }
    }

    /**
     * Reset saved conditions if app version changed.
     *
     * @param context Context
     */
    public static void resetIfAppVersionChanged(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);

        int appVersionCode = Integer.MIN_VALUE;
        final int previousAppVersionCode = prefs.getInt(PREF_KEY_APP_VERSION_CODE, Integer.MIN_VALUE);
        try {
            appVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Occurred PackageManager.NameNotFoundException", e);
        }

        if (previousAppVersionCode != appVersionCode) {
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putLong(PREF_KEY_APP_LAUNCH_COUNT, 0);
            prefsEditor.putLong(PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT, 0);
            prefsEditor.putLong(PREF_KEY_APP_FIRST_LAUNCHED_DATE, 0);
            prefsEditor.putInt(PREF_KEY_APP_VERSION_CODE, Integer.MIN_VALUE);
            prefsEditor.putLong(PREF_KEY_RATE_CLICK_DATE, 0);
            prefsEditor.putLong(PREF_KEY_REMINDER_CLICK_DATE, 0);
            prefsEditor.putBoolean(PREF_KEY_DO_NOT_SHOW_AGAIN, false);
            prefsEditor.commit();
        }
    }

    /**
     * Modify internal value.
     * <p/>
     * If you use this method, you might need to have a good understanding of this class code.
     *
     * @param context        Context
     * @param appLaunchCount Launch count of This application.
     */
    public static void setAppLaunchCount(Context context, long appLaunchCount) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putLong(PREF_KEY_APP_LAUNCH_COUNT, appLaunchCount);

        prefsEditor.commit();
    }

    /**
     * Modify internal value.
     * <p/>
     * If you use this method, you might need to have a good understanding of this class code.
     *
     * @param context                       Context
     * @param appThisVersionCodeLaunchCount Launch count of This application current version.
     */
    public static void setAppThisVersionCodeLaunchCount(Context context, long appThisVersionCodeLaunchCount) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putLong(PREF_KEY_APP_LAUNCH_COUNT, appThisVersionCodeLaunchCount);

        prefsEditor.commit();
    }

    /**
     * Modify internal value.
     * <p/>
     * If you use this method, you might need to have a good understanding of this class code.
     *
     * @param context         Context
     * @param firstLaunchDate First launch date.
     */
    public static void setFirstLaunchDate(Context context, long firstLaunchDate) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putLong(PREF_KEY_APP_FIRST_LAUNCHED_DATE, firstLaunchDate);

        prefsEditor.commit();
    }

    /**
     * Modify internal value.
     * <p/>
     * If you use this method, you might need to have a good understanding of this class code.
     *
     * @param context       Context
     * @param rateClickDate Date of "Rate" button clicked.
     */
    public static void setRateClickDate(Context context, Date rateClickDate) {
        final long rateClickDateMills = ((rateClickDate != null) ? rateClickDate.getTime() : 0);

        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putLong(PREF_KEY_RATE_CLICK_DATE, rateClickDateMills);

        prefsEditor.commit();
    }

    /**
     * Modify internal value.
     * <p/>
     * If you use this method, you might need to have a good understanding of this class code.
     *
     * @param context           Context
     * @param reminderClickDate Date of "Remind me later" button clicked.
     */
    public static void setReminderClickDate(Context context, Date reminderClickDate) {
        final long reminderClickDateMills = ((reminderClickDate != null) ? reminderClickDate.getTime() : 0);

        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putLong(PREF_KEY_REMINDER_CLICK_DATE, reminderClickDateMills);

        prefsEditor.commit();
    }

    /**
     * Modify internal value.
     * <p/>
     * If you use this method, you might need to have a good understanding of this class code.
     *
     * @param context        Context
     * @param doNotShowAgain Clicked "No, Thanks" if true.
     */
    public static void setDoNotShowAgain(Context context, boolean doNotShowAgain) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putBoolean(PREF_KEY_DO_NOT_SHOW_AGAIN, doNotShowAgain);

        prefsEditor.commit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getPackageName() + PREFS_PACKAGE_NAME_SUFFIX, Context.MODE_PRIVATE);
    }

    @SuppressLint("NewApi")
    private static void showRateDialog(final Context context, Options options, final OnCompleteListener onCompleteListener) {
        final int applicationNameResId = context.getApplicationInfo().labelRes;
        final String applicationName = context.getString(applicationNameResId);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.rmp_appirater_dialog, null);
        TextView messageView = (TextView) layout.findViewById(R.id.message);
        if (options != null && !TextUtils.isEmpty(options.getDialogMessage())) {
            messageView.setText(options.getDialogMessage());
        } else {
            messageView.setText(context.getString(R.string.rmp_appirater_rate_message, applicationName));
        }

        Button rateButton = (Button) layout.findViewById(R.id.rate);
        if (options != null && !TextUtils.isEmpty(options.getDialogRateButtonText())) {
            rateButton.setText(options.getDialogRateButtonText());
        } else {
            rateButton.setText(context.getString(R.string.rmp_appirater_rate, applicationName));
        }

        Button rateLaterButton = (Button) layout.findViewById(R.id.rate_later);
        if (options != null && !TextUtils.isEmpty(options.getDialogRateLaterButtonText())) {
            rateLaterButton.setText(options.getDialogRateLaterButtonText());
        } else {
            rateLaterButton.setText(context.getString(R.string.rmp_appirater_rate_later, applicationName));
        }

        Button rateCancelButton = (Button) layout.findViewById(R.id.rate_cancel);
        if (options != null && !TextUtils.isEmpty(options.getDialogRateCancelButtonText())) {
            rateCancelButton.setText(options.getDialogRateCancelButtonText());
        } else {
            rateCancelButton.setText(context.getString(R.string.rmp_appirater_rate_cancel, applicationName));
        }

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                remindApp(context);
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete();
                }
            }
        });

        builder.setCancelable(true);
        if (options != null && !TextUtils.isEmpty(options.getDialogTitle())) {
            builder.setTitle(options.getDialogTitle());
        } else {
            builder.setTitle(context.getString(R.string.rmp_appirater_rate_title, applicationName));
        }
        builder.setView(layout);

        final AlertDialog dialog = builder.create();

        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateApp(context);
                dialog.dismiss();
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete();
                }
            }
        });

        rateLaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remindApp(context);
                dialog.dismiss();
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete();
                }
            }
        });

        rateCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRateApp(context);
                dialog.dismiss();
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete();
                }
            }
        });

        dialog.show();
    }

    private static void rateApp(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Occurred ActivityNotFoundException.", e);
        }

        SharedPreferences.Editor prefsEditor = getSharedPreferences(context).edit();
        prefsEditor.putLong(PREF_KEY_RATE_CLICK_DATE, System.currentTimeMillis());
        prefsEditor.commit();
    }

    private static void remindApp(Context context) {
        SharedPreferences.Editor prefsEditor = getSharedPreferences(context).edit();
        prefsEditor.putLong(PREF_KEY_REMINDER_CLICK_DATE, System.currentTimeMillis());
        prefsEditor.commit();
    }

    private static void cancelRateApp(Context context) {
        SharedPreferences.Editor prefsEditor = getSharedPreferences(context).edit();
        prefsEditor.putBoolean(PREF_KEY_DO_NOT_SHOW_AGAIN, true);
        prefsEditor.commit();
    }

    /**
     * RMP-Appirater options.
     */
    public static class Options {

        private CharSequence mDialogTitle;

        private CharSequence mDialogMessage;

        private CharSequence mDialogRateButtonText;

        private CharSequence mDialogRateLaterButtonText;

        private CharSequence mDialogRateCancelButtonText;

        /**
         * Constructor.
         */
        public Options() {
        }

        /**
         * Constructor.
         *
         * @param dialogTitle                Dialog title
         * @param dialogMessage              Dialog message
         * @param dialogRateButtonText       Dialog rate button text
         * @param dialogRateLaterButtonText  Dialog rate later button text
         * @param dialogRateCancelButtonText Dialog rate cancel button text
         */
        public Options(CharSequence dialogTitle, CharSequence dialogMessage, CharSequence dialogRateButtonText,
                       CharSequence dialogRateLaterButtonText, CharSequence dialogRateCancelButtonText) {
            this.mDialogTitle = dialogTitle;
            this.mDialogMessage = dialogMessage;
            this.mDialogRateButtonText = dialogRateButtonText;
            this.mDialogRateLaterButtonText = dialogRateLaterButtonText;
            this.mDialogRateCancelButtonText = dialogRateCancelButtonText;
        }

        /**
         * Gets dialog title.
         *
         * @return Dialog title
         */
        public CharSequence getDialogTitle() {
            return mDialogTitle;
        }

        /**
         * Sets dialog title.
         *
         * @param dialogTitle Dialog title
         */
        public void setDialogTitle(CharSequence dialogTitle) {
            this.mDialogTitle = dialogTitle;
        }

        /**
         * Gets dialog message.
         *
         * @return Dialog message
         */
        public CharSequence getDialogMessage() {
            return mDialogMessage;
        }

        /**
         * Sets dialog message.
         *
         * @param dialogMessage Dialog message
         */
        public void setDialogMessage(CharSequence dialogMessage) {
            this.mDialogMessage = dialogMessage;
        }

        /**
         * Gets dialog rate button text.
         *
         * @return Dialog rate button text
         */
        public CharSequence getDialogRateButtonText() {
            return mDialogRateButtonText;
        }

        /**
         * Sets dialog rate button text.
         *
         * @param dialogRateButtonText dialog rate button text
         */
        public void setDialogRateButtonText(CharSequence dialogRateButtonText) {
            this.mDialogRateButtonText = dialogRateButtonText;
        }

        /**
         * Gets dialog rate later button text.
         *
         * @return Dialog rate later button text
         */
        public CharSequence getDialogRateLaterButtonText() {
            return mDialogRateLaterButtonText;
        }

        /**
         * Sets dialog rate later button text.
         *
         * @param dialogRateLaterButtonText Dialog rate later button text
         */
        public void setDialogRateLaterButtonText(CharSequence dialogRateLaterButtonText) {
            this.mDialogRateLaterButtonText = dialogRateLaterButtonText;
        }

        /**
         * Gets dialog rate cancel button text.
         *
         * @return dialog rate cancel button text
         */
        public CharSequence getDialogRateCancelButtonText() {
            return mDialogRateCancelButtonText;
        }

        /**
         * Sets dialog rate cancel button text.
         *
         * @param dialogRateCancelButtonText Dialog rate cancel button text
         */
        public void setDialogRateCancelButtonText(CharSequence dialogRateCancelButtonText) {
            this.mDialogRateCancelButtonText = dialogRateCancelButtonText;
        }

    }

    /**
     * Rate Dialog showing condition interface.
     */
    public interface ShowRateDialogCondition {
        /**
         * Show rate dialog if returned true.
         *
         * @param appLaunchCount                Launch count of This application.
         * @param appThisVersionCodeLaunchCount Launch count of This application current version.
         * @param firstLaunchDate               First launch date.
         * @param appVersionCode                This application version code.
         * @param previousAppVersionCode        The application version code of when it's launched last.
         * @param rateClickDate                 Date of "Rate" button clicked.
         * @param reminderClickDate             Date of "Remind me later" button clicked.
         * @param doNotShowAgain                Clicked "No, Thanks" if true.
         * @return Show rate dialog if returned true.
         */
        boolean isShowRateDialog(long appLaunchCount, long appThisVersionCodeLaunchCount,
                                 long firstLaunchDate, int appVersionCode, int previousAppVersionCode,
                                 Date rateClickDate, Date reminderClickDate, boolean doNotShowAgain);
    }

    public interface OnCompleteListener {
        /**
         * A rating dialog is closed.
         */
        void onComplete();

        /**
         * A rating dialog is not shown because rating dialog showing condition is not true.
         */
        void onNotShownDialog();
    }

}
