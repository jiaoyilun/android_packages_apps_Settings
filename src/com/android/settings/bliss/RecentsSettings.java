/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.settings.bliss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cyanogenmod.PackageListAdapter;
import com.android.settings.cyanogenmod.PackageListAdapter.PackageItem;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

public class RecentsSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable,AdapterView.OnItemLongClickListener, OnPreferenceClickListener {

	static final String TAG = "RecentsSettings";
    private static final String SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents";
    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";
	private static final String PREF_CLEAR_ALL_BG_COLOR =
            "android_recents_clear_all_bg_color";
//    private static final String PREF_CLEAR_ALL_ICON_COLOR =
//            "android_recents_clear_all_icon_color";

    private static final int RED = 0xffDC4C3C;
    private static final int WHITE = 0xffffffff;
    private static final int HOLO_BLUE_LIGHT = 0xff33b5e5;

    private SwitchPreference mRecentsClearAll;
    private ListPreference mRecentsClearAllLocation;
//    private ColorPickerPreference mClearAllIconColor;
    private ColorPickerPreference mClearAllBgColor;
    
    private static final int DIALOG_APPS = 0;
    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;
    private PreferenceGroup mDndPrefList;
    private Preference mAddDndPref;
    private String mDndPackageList;
    private Map<String, Package> mDndPackages;
    

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.recents_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        int intvalue;
        int intColor;
        String hexColor;

        mRecentsClearAll = (SwitchPreference) prefSet.findPreference(SHOW_CLEAR_ALL_RECENTS);
        mRecentsClearAll.setChecked(Settings.System.getIntForUser(resolver,
            Settings.System.SHOW_CLEAR_ALL_RECENTS, 1, UserHandle.USER_CURRENT) == 1);
        mRecentsClearAll.setOnPreferenceChangeListener(this);

        mRecentsClearAllLocation = (ListPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_LOCATION);
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
        mRecentsClearAllLocation.setValue(String.valueOf(location));
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);
        updateRecentsLocation(location);

        mClearAllBgColor =
        (ColorPickerPreference) findPreference(PREF_CLEAR_ALL_BG_COLOR);
        intColor = Settings.System.getInt(resolver,
            Settings.System.RECENT_APPS_CLEAR_ALL_BG_COLOR, RED); 
        mClearAllBgColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mClearAllBgColor.setSummary(hexColor);
        mClearAllBgColor.setDefaultColors(RED, RED);
        mClearAllBgColor.setOnPreferenceChangeListener(this);

//        mClearAllIconColor =
//		      (ColorPickerPreference) findPreference(PREF_CLEAR_ALL_ICON_COLOR);
//        intColor = Settings.System.getInt(resolver,
//           Settings.System.RECENT_APPS_CLEAR_ALL_ICON_COLOR, WHITE); 
//        mClearAllIconColor.setNewPreviewColor(intColor);
//        hexColor = String.format("#%08x", (0xffffffff & intColor));
//        mClearAllIconColor.setSummary(hexColor);
//        mClearAllIconColor.setDefaultColors(WHITE, WHITE);
//        mClearAllIconColor.setOnPreferenceChangeListener(this);
        
        mPackageManager = getPackageManager();
        mPackageAdapter = new PackageListAdapter(getActivity());
        
        mDndPrefList = (PreferenceGroup) findPreference("exclude_applications_list");
        mDndPrefList.setOrderingAsAdded(false);
        mDndPackages = new HashMap<String, Package>();
        mAddDndPref = findPreference("add_exclude_packages");
        mAddDndPref.setOnPreferenceClickListener(this);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value;
        int intvalue;
        int index;
        String hex;
        int intHex;
        if (preference == mRecentsClearAll) {
            value = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.SHOW_CLEAR_ALL_RECENTS, value ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mRecentsClearAllLocation) {
            int location = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
            updateRecentsLocation(location);
            return true;
        } else if (preference == mClearAllBgColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.RECENT_APPS_CLEAR_ALL_BG_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } 
//        else if (preference == mClearAllIconColor) {
//            hex = ColorPickerPreference.convertToARGB(
//                    Integer.valueOf(String.valueOf(newValue)));
//            intHex = ColorPickerPreference.convertToColorInt(hex);
//            Settings.System.putInt(getActivity().getContentResolver(),
//                    Settings.System.RECENT_APPS_CLEAR_ALL_ICON_COLOR, intHex);
//            preference.setSummary(hex);
//            return true;
//        }
        return false;
    }

    private void updateRecentsLocation(int value) {
        ContentResolver resolver = getContentResolver();
        Resources res = getResources();
        int summary = -1;

        Settings.System.putInt(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION, value);

        if (value == 0) {
            Settings.System.putInt(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION, 0);
            summary = R.string.recents_clear_all_location_top_right;
        } else if (value == 1) {
            Settings.System.putInt(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION, 1);
            summary = R.string.recents_clear_all_location_top_left;
        } else if (value == 2) {
            Settings.System.putInt(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION, 2);
            summary = R.string.recents_clear_all_location_top_center;            
        } else if (value == 3) {
            Settings.System.putInt(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3);
            summary = R.string.recents_clear_all_location_bottom_right;
        } else if (value == 4) {
            Settings.System.putInt(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION, 4);
            summary = R.string.recents_clear_all_location_bottom_left;
        } else if (value == 5) {
            Settings.System.putInt(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION, 5);
            summary = R.string.recents_clear_all_location_bottom_center;            
        }
        if (mRecentsClearAllLocation != null && summary != -1) {
            mRecentsClearAllLocation.setSummary(res.getString(summary));
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                    boolean enabled) {
            ArrayList<SearchIndexableResource> result =
                new ArrayList<SearchIndexableResource>();

            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.recents_settings;
            result.add(sir);

            return result;
        }

        @Override
        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList<String>();
            return result;
        }
    };


	@Override
	public void onResume() {
		super.onResume();
		
	    refreshCustomApplicationPrefs();
        getListView().setOnItemLongClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		 if (preference == mAddDndPref) {
	            showDialog(DIALOG_APPS);
	        }
		 return true;
	}
	
	 /**
     * Application class
     */
    private static class Package {
        public String name;
        /**
         * Stores all the application values in one call
         * @param name
         */
        public Package(String name) {
            this.name = name;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(name);
            return builder.toString();
        }

        public static Package fromString(String value) {
            if (TextUtils.isEmpty(value)) {
                return null;
            }
            try {
                Package item = new Package(value);
                return item;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
    
    /**
     * Utility classes and supporting methods
     */
    @Override
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Dialog dialog;
        final ListView list = new ListView(getActivity());
        list.setAdapter(mPackageAdapter);

        builder.setTitle(R.string.profile_choose_app);
        builder.setView(list);
        dialog = builder.create();

        switch (id) {
            case DIALOG_APPS:
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                            View view, int position, long id) {
                        PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                        addCustomApplicationPref(info.packageName, mDndPackages);
                        dialog.cancel();
                    }
                });
        }
        return dialog;
    }

    private void addCustomApplicationPref(String packageName, Map<String,Package> map) {
        Package pkg = map.get(packageName);
        if (pkg == null) {
            pkg = new Package(packageName);
            map.put(packageName, pkg);
            savePackageList(false, map);
            refreshCustomApplicationPrefs();
        }
    }
    
    private void savePackageList(boolean preferencesUpdated, Map<String,Package> map) {
        String setting = Settings.System.RECENTS_WHITE_VALUES;

        List<String> settings = new ArrayList<String>();
        for (Package app : map.values()) {
            settings.add(app.toString());
        }
        final String value = TextUtils.join("|", settings);
        if (preferencesUpdated) {
                mDndPackageList = value;
        }
        Settings.System.putString(getContentResolver(), setting, value);
    }
    
    private void refreshCustomApplicationPrefs() {
        if (!parsePackageList()) {
            return;
        }
        // Add the Application Preferences
        if (mDndPrefList != null ) {
            mDndPrefList.removeAll();
            for (Package pkg : mDndPackages.values()) {
                try {
                    Preference pref = createPreferenceFromInfo(pkg);
                    mDndPrefList.addPreference(pref);
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
        }
        mAddDndPref.setOrder(0);
        mDndPrefList.addPreference(mAddDndPref);
    }
    
    private boolean parsePackageList() {
        boolean parsed = false;
        final String dndString = Settings.System.getString(getContentResolver(),
                Settings.System.RECENTS_WHITE_VALUES);
        if (!TextUtils.equals(mDndPackageList, dndString)) {
            mDndPackageList = dndString;
            mDndPackages.clear();
            parseAndAddToMap(dndString, mDndPackages);
            parsed = true;
        }
        return parsed;
    }
    
    private void parseAndAddToMap(String baseString, Map<String,Package> map) {
        if (baseString == null) {
            return;
        }
        final String[] array = TextUtils.split(baseString, "\\|");
        for (String item : array) {
            if (TextUtils.isEmpty(item)) {
                continue;
            }
            Package pkg = Package.fromString(item);
            map.put(pkg.name, pkg);
        }
    }
    
    private Preference createPreferenceFromInfo(Package pkg)
            throws PackageManager.NameNotFoundException {
        PackageInfo info = mPackageManager.getPackageInfo(pkg.name,
                PackageManager.GET_META_DATA);
        Preference pref = new Preference(getActivity());

        pref.setKey(pkg.name);
        pref.setTitle(info.applicationInfo.loadLabel(mPackageManager));
        pref.setIcon(info.applicationInfo.loadIcon(mPackageManager));
        pref.setPersistent(false);
        return pref;
    }

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		final Preference pref =
                (Preference) getPreferenceScreen().getRootAdapter().getItem(position);

        if (mDndPrefList.findPreference(pref.getKey()) != pref) {
            return false;
        }

        if (mAddDndPref == pref) {
            return false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            removeApplicationPref(pref.getKey(), mDndPackages);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        builder.show();
        return true;
	}
    
	private void removeApplicationPref(String packageName, Map<String,Package> map) {
        if (map.remove(packageName) != null) {
            savePackageList(false, map);
            refreshCustomApplicationPrefs();
        }
    }
	
}