package com.iflytek.speech.setting;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Window;

import androidx.annotation.Nullable;
import com.epichust.voice.R;

/**
 * @author hjyu
 * @date 2018/5/31.
 * @see <a href="http://www.xfyun.cn">讯飞开放平台</a>
 */

public class UrlSettings extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    public static final String PREFER_NAME = "com.iflytek.setting";
    private ListPreference url_preference;
    private EditTextPreference url_edit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PREFER_NAME);
        addPreferencesFromResource(R.xml.url_setting);

        url_preference = (ListPreference)findPreference("url_preference");
        url_preference.setSummary("当前：" + url_preference.getEntry());
        url_preference.setOnPreferenceChangeListener(this);


        url_edit = (EditTextPreference) findPreference("url_edit");
        url_edit.setSummary("当前域名：" + url_edit.getText());
        url_edit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                url_edit.setSummary("当前机房：" + newValue.toString());
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int newValueIndex = url_preference.findIndexOfValue(newValue.toString());
        String newEntry = (String) url_preference.getEntries()[newValueIndex];
        url_preference.setSummary("当前：" + newEntry);
        return true;
    }
}
