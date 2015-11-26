/*
 * Copyright (C) 2013 SlimRoms
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

package com.android.settings.bliss;

import android.app.admin.DevicePolicyManager;
import android.app.ActivityManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.provider.SearchIndexableResource;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LockScreenSettings extends SettingsPreferenceFragment
        implements OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "LockScreenSettings";
//    private static final String KEY_SHOW_VISUALIZER = "lockscreen_visualizer";
//    private static final String GENERAL_CATEGORY = "general_category";
//    private static final String SHORTCUTS_CATEGORY = "shortcuts_category";
//    private static final String CUSTOMIZE_CATEGORY = "customize_category";
//
//    private static final String VISUALIZER_CATEGORY = "visualizer";
//
//    private static final String KEY_LONG_PRESS_LOCK_ICON_TORCH = "long_press_lock_icon_torch";
//
//    private PreferenceScreen mLockScreen;
//    private SwitchPreference mLongClickTorch;

//    private Context mContext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.bliss_lockscreen_settings);

//        PreferenceScreen prefSet = getPreferenceScreen();
//        ContentResolver resolver = getActivity().getContentResolver();
//        PackageManager pm = getPackageManager();
//        Resources res = getResources();
//        mContext = getActivity();

//        mLockScreen = (PreferenceScreen) findPreference("lock_screen");
//        PreferenceCategory visualizerCategory = (PreferenceCategory)
//                getPreferenceScreen().findPreference(VISUALIZER_CATEGORY);
//
//        PreferenceCategory generalCategory = (PreferenceCategory)
//                getPreferenceScreen().findPreference(GENERAL_CATEGORY);
//
//        PreferenceCategory shortcutsCategory = (PreferenceCategory)
//                getPreferenceScreen().findPreference(SHORTCUTS_CATEGORY);
//
//        PreferenceCategory customizeCategory = (PreferenceCategory)
//                getPreferenceScreen().findPreference(CUSTOMIZE_CATEGORY);

        // remove lockscreen visualizer option on low end gfx devices
//        if (!ActivityManager.isHighEndGfx() && visualizerCategory != null) {
//            SwitchPreference displayVisualizer = (SwitchPreference)
//                    visualizerCategory.findPreference(KEY_SHOW_VISUALIZER);
//            if (displayVisualizer != null) {
//                visualizerCategory.removePreference(displayVisualizer);
//            }
//        }

        // Remove Long Press Lock Icon for Torch option for non-flash devices
//        if(!DeviceUtils.deviceSupportsFlashLight(getActivity()) && generalCategory != null) {
//            mLongClickTorch = (SwitchPreference) generalCategory
//                    .findPreference(KEY_LONG_PRESS_LOCK_ICON_TORCH);
//            if (mLongClickTorch != null) {
//                generalCategory.removePreference(mLongClickTorch);
//            }
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return false;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.bliss_lockscreen_settings;
                    result.add(sir);

                    return result;
                }

        @Override
        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList<String>();
            return result;
        }
    };	
}