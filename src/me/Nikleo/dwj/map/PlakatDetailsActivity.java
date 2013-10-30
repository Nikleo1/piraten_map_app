package me.Nikleo.dwj.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.boombuler.piraten.map.R;

public class PlakatDetailsActivity extends Activity 
		implements OnClickListener {
	
	class MarkerTypeAdapter implements SpinnerAdapter {
		class ViewHolder {
			public TextView textView;
			public ImageView imageView;
		}
		
		private String[] mTitles;
		private Drawable[] mIcons;
		private LayoutInflater mInflater;
		
		public MarkerTypeAdapter() {
			Resources res = PlakatDetailsActivity.this.getResources();
			mInflater = PlakatDetailsActivity.this.getLayoutInflater();
			mTitles = res.getStringArray(R.array.markertypes);
			mIcons = new Drawable[] {
					res.getDrawable(R.drawable.plakat_default), 
	        		res.getDrawable(R.drawable.plakat_ok), 
	        		res.getDrawable(R.drawable.plakat_dieb), 
	        		res.getDrawable(R.drawable.plakat_niceplace), 
	        		res.getDrawable(R.drawable.wand),
	        		res.getDrawable(R.drawable.wand_ok),
	        		res.getDrawable(R.drawable.plakat_wrecked),
	        		res.getDrawable(R.drawable.plakat_a0)};
		}
		
		public int getCount() {
			return mTitles.length;
		}

		public Object getItem(int position) {
			return mTitles[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public int getItemViewType(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.textandicon, parent, false);
				ViewHolder vh = new ViewHolder();
				vh.textView = (TextView)convertView.findViewById(R.id.textView);
				vh.imageView = (ImageView)convertView.findViewById(R.id.imageView);
				convertView.setTag(vh);
			}
			ViewHolder holder = (ViewHolder)convertView.getTag();
			holder.textView.setText(mTitles[position]);
			holder.imageView.setImageDrawable(mIcons[position]);
			return convertView;
		}

		public int getViewTypeCount() {
			return 1;
		}

		public boolean hasStableIds() {
			return true;
		}

		public boolean isEmpty() {			
			return false;
		}

		public void registerDataSetObserver(DataSetObserver observer) {			
		}

		public void unregisterDataSetObserver(DataSetObserver observer) {			
		}

		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return getView(position, convertView, parent);
		}
		
	}
	
	
	public static final String EXTRA_PLAKAT_ID = "com.boombuler.piraten.map.EXTRA_PLAKAT_ID";
	public static final String EXTRA_NEW_PLAKAT = "com.boombuler.piraten.map.EXTRA_NEW_PLAKAT";
	
	private Button mSaveButton;
	private MenuItem mSaveItem;
	private Spinner mMarkerTypeSpinner;
	private EditText mComment;
	
	private boolean mIsNew;
	private int mId;
	
	private float mMinAccuracy;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		Intent intent = getIntent();
		mId = intent.getIntExtra(EXTRA_PLAKAT_ID, -1);
		mIsNew = intent.getBooleanExtra(EXTRA_NEW_PLAKAT, false);
		if (mId < 0 && !mIsNew)
			finish();
		
		setContentView(R.layout.details);
		
		mSaveButton = (Button)findViewById(R.id.btSave);
		if (mSaveButton != null)
			mSaveButton.setOnClickListener(this);
		
		mMarkerTypeSpinner = (Spinner)findViewById(R.id.spMarkerType);
		mMarkerTypeSpinner.setAdapter(new MarkerTypeAdapter());
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		mComment = (EditText)findViewById(R.id.tvComment);
		
		if (!mIsNew) {
			PlakatOverlayItem item = null;
			DBAdapter adapter = new DBAdapter(this);
			try {
				adapter.open();
				item = adapter.getOverlayItem(mId);
			} finally {
				adapter.close();
			}
			
			if (item != null) {
				mMarkerTypeSpinner.setSelection(item.getType());
				mComment.setText(item.getComment());
			}
		}else {
			mMinAccuracy = ((float)PreferenceManager.getDefaultSharedPreferences(this).getInt(SettingsActivity.KEY_ACCURACY, 70)) / 10f;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.plakate_details, menu);
		menu.findItem(R.id.menu_delete).setVisible(!mIsNew);
		
		mSaveItem = menu.findItem(R.id.menu_accept).setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			case R.id.menu_delete:
				Delete();
				return true;
			case R.id.menu_accept:
				Save();
				return true;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (item.getItemId() == android.R.id.home)
				this.finish();
		}
		
		return super.onOptionsItemSelected(item);
	}

	private void Delete() {
		final AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.menu_delete);
		ab.setMessage(R.string.ask_marker_delete);
		ab.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				DBAdapter adapter = new DBAdapter(PlakatDetailsActivity.this);
				try {
					adapter.open();
					adapter.delete(mId);
				} finally {
					adapter.close();
				}
				
				PlakatDetailsActivity.this.setResult(RESULT_OK);
				PlakatDetailsActivity.this.finish();
			}
		});
		ab.setNegativeButton(android.R.string.no, null);
		ab.show();
	}
	
	private void Save() {
		if (mSaveButton != null)
			mSaveButton.setEnabled(false);
		if (mSaveItem != null)
			mSaveItem.setEnabled(false);
		if (mIsNew)
			Insert();
		else
			Update();
	}
	
	public void onClick(View v) {
		if (v == null)
			return;
		if (v == mSaveButton) {
			Save();
		}
	}

	private void Update() {
		DBAdapter adapter = new DBAdapter(this);
		try {			
			adapter.open();

            String comment = mComment.getText().toString();

			adapter.Update(mId, mMarkerTypeSpinner.getSelectedItemPosition(), comment);
		} finally {
			adapter.close();
		}
		setResult(RESULT_OK);
		finish();
	}

	private ProgressDialog mProgressDlg = null;
	
	private void Insert() {
		final LocationListener ll = new LocationListener() {
			
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
			
			public void onProviderEnabled(String provider) {
			}
			
			public void onProviderDisabled(String provider) {
			}
			
			public void onLocationChanged(Location location) {
				CompleteInsert(location);
			}
		}; 
		final LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		mProgressDlg = new ProgressDialog(this);
		mProgressDlg.setOwnerActivity(this);
		mProgressDlg.setCancelable(true);
		mProgressDlg.setCanceledOnTouchOutside(false);
		mProgressDlg.setIndeterminate(true);
		mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDlg.setMessage(getString(R.string.get_position));
		mProgressDlg.setButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mProgressDlg.cancel();
			}
		});
		mProgressDlg.setOnCancelListener(new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				lm.removeUpdates(ll);
				if (mSaveButton != null)
					mSaveButton.setEnabled(true);
				if (mSaveItem != null)
					mSaveItem.setEnabled(true);
			}
		});
		mProgressDlg.setOnDismissListener(new OnDismissListener() {			
			public void onDismiss(DialogInterface dialog) {
				lm.removeUpdates(ll);
			}
		});
		mProgressDlg.show();
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
		    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
	}
	
	private void CompleteInsert(Location loc) {
		if (loc.getAccuracy() <= mMinAccuracy) {
			mProgressDlg.dismiss();
			DBAdapter adapter = new DBAdapter(this);
			try {
				adapter.open();
				adapter.InsertNew( 
						(int)(loc.getLatitude() * 1E6), 
						(int)(loc.getLongitude() * 1E6), 
						mMarkerTypeSpinner.getSelectedItemPosition(),
                        mComment.getText().toString());
			} finally {
				adapter.close();
			}
			
			setResult(RESULT_OK);
			finish();
		} else {
			String msg = getString(R.string.get_position) + "\n";
			msg += getString(R.string.current_accuracy, loc.getAccuracy());
			
			mProgressDlg.setMessage(msg);
		}

	}
}
