package me.Nikleo.dwj.map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;
import me.Nikleo.dwj.map.R;

import java.util.List;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {

	public static final String KEY_USERNAME = "username";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_ACCURACY = "accuracy";
	public static final String KEY_SYNC_RANGE = "sync_range";
        public static final  String KEY_HAS_SYNCED = "has_synced_before";
	

    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			if (item.getItemId() == android.R.id.home) {
				this.finish();
				return true;
			}
	    }
		return super.onOptionsItemSelected(item);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
	       
		return false;
	}


	
	public boolean onPreferenceClick(Preference preference) {
		
		return false;
	}
}
