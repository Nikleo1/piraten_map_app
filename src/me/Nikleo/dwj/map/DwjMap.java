package me.Nikleo.dwj.map;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

public class DwjMap extends Activity {

    private MapView mMapView;
    private CurrentPositionOverlay mMyPosOverlay;
    static int REQUEST_EDIT_PLAKAT = 1;
    static int INITIAL_ZOOM = 16;
    public static String BASEURL = "http://1.1.1.8/pm/";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Resources res = getResources();
        PlakatOverlayItem.InitResources(res.getDrawable(R.drawable.plakat_default),
                res.getDrawable(R.drawable.plakat_ok),
                res.getDrawable(R.drawable.plakat_dieb),
                res.getDrawable(R.drawable.plakat_niceplace),
                res.getDrawable(R.drawable.wand),
                res.getDrawable(R.drawable.wand_ok),
                res.getDrawable(R.drawable.plakat_wrecked),
                res.getDrawable(R.drawable.plakat_a0),
                res.getDrawable(R.drawable.ic_menu_add));

        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);

        boolean hasSyncedBefore = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.KEY_HAS_SYNCED, false);
        if (!hasSyncedBefore) {
            AlertDialog wd = new AlertDialog.Builder(this).create();
            wd.setTitle(getString(R.string.welkome));
            wd.setMessage(getString(R.string.welkome_message));
            wd.setButton(getString(R.string.welkome_login), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(DwjMap.this, SettingsActivity.class));
                }
            });
            wd.setButton2(getString(R.string.welkome_register), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    showRegister();
                }
            });
            wd.show();

        }

    }

    public void showRegister() {
        AlertDialog rd = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        final View v = inflater.inflate(R.layout.register, null);
        rd.setView(v);

        boolean safed = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.REG_SAFED, false);

        if (safed) {
            EditText username = (EditText) v.findViewById(R.id.reg_username);
            EditText pw1 = (EditText) v.findViewById(R.id.password);
            EditText pw2 = (EditText) v.findViewById(R.id.passwordwd);
            EditText mail = (EditText) v.findViewById(R.id.mail);

            // TODO Speicherstände zurückspielen
            username.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.REG_USERNAME, ""));
            pw1.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.REG_PW1, ""));
            pw2.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.REG_PW2, ""));
            mail.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.REG_EMAIL, ""));
        }
        rd.setButton(getString(R.string.register), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                EditText username = (EditText) v.findViewById(R.id.reg_username);
                EditText pw1 = (EditText) v.findViewById(R.id.password);
                EditText pw2 = (EditText) v.findViewById(R.id.passwordwd);
                EditText mail = (EditText) v.findViewById(R.id.mail);

                String user = username.getText().toString();
                String pass1 = pw1.getText().toString();
                String pass2 = pw2.getText().toString();
                String email = mail.getText().toString();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DwjMap.this);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString(SettingsActivity.REG_USERNAME, user);
                editor.putString(SettingsActivity.REG_PW1, pass1);
                editor.putString(SettingsActivity.REG_PW2, pass2);
                editor.putString(SettingsActivity.REG_EMAIL, email);

                editor.putBoolean(SettingsActivity.REG_SAFED, true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    editor.apply();
                } else {
                    editor.commit();
                }

                if (!user.equals("") && !pass1.equals("") && !pass2.equals("") && !email.equals("")) {
                    if (pass1.equals(pass2)) {
                        if (pass1.length() > 2) {
                            if (email.matches("^[\\w\\.=-]+@[\\w\\.-]+\\.[\\w]{2,4}$")) {

                                try {
                                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                                    StrictMode.setThreadPolicy(policy);

                                    String user1 = URLEncoder.encode(user.trim(), "UTF-8");

                                    URL u = new URL(DwjMap.BASEURL + "checkuser.php?name=" + user1);
                                    String r = new Scanner(u.openStream()).useDelimiter("\\Z").next();

                                    if (r.equalsIgnoreCase("false")) {

                                        email = URLEncoder.encode(email.trim(), "UTF-8");
                                        String pass12 = URLEncoder.encode(pass1.trim(), "UTF-8");

                                        u = new URL(DwjMap.BASEURL + "registernew.php?name=" + user1 + "&mail=" + email + "&pw=" + pass12);
                                        r = new Scanner(u.openStream()).useDelimiter("\\Z").next();
                                        if (r.equalsIgnoreCase("Registered")) {
                                            editor.putString(SettingsActivity.KEY_USERNAME, user);
                                            editor.putString(SettingsActivity.KEY_PASSWORD, pass1);
                                            
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                                                editor.apply();
                                            } else {
                                                editor.commit();
                                            }
                                            
                                            registrationerfolgreich();
                                        } else {
                                            fehler("Unbekannter Fehler");
                                        }
                                    } else {
                                        fehler("Der angegeben Nutzer existiert bereits");
                                    }

                                } catch (Exception ex) {
                                    fehler("Message : " + ex.getMessage() + " to strib " + ex.toString() + ex.getLocalizedMessage());
                                }

                            } else {
                                fehler("Die angegebene Mailadresse ist ungültig");
                            }

                        } else {
                            fehler("Das Passwort muss mindestens 3 Zeichen lang seien !");
                        }

                    } else {
                        fehler("Die Passwörter stimmen nicht überein !");
                    }
                } else {
                    fehler("Bitte alle Felder ausfüllen");
                }
            }
        });

        rd.show();
    }

    public void registrationerfolgreich() {
        AlertDialog wd = new AlertDialog.Builder(this).create();
        wd.setTitle("Erfolgreich Registriert");
        wd.setMessage("Wilkommen und vielen Dank für deine Registration. Du kannst dieses Fenster nun schließen !");
        wd.setButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                StartSync();
            }
        });
        wd.show();
    }

    public void fehler(String s) {
        AlertDialog wd = new AlertDialog.Builder(this).create();
        wd.setTitle("Fehler");
        wd.setMessage(s);
        wd.setButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                showRegister();
            }
        });
        wd.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_PLAKAT && resultCode == RESULT_OK) {
            BuildMap(); // Something changed so reload!
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sync:
                StartSync();
                return true;
            case R.id.menu_add:
                AddMarker();
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(DwjMap.this, SettingsActivity.class));
                return true;
            case android.R.id.home:
                if (mMapView != null && mMyPosOverlay != null) {
                    GeoPoint location = mMyPosOverlay.getMyLocation();
                    if (location != null) {
                        mMapView.getController().animateTo(location);
                    }
                    return true;
                }
                return false;
            case R.id.regButton:
                showRegister();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.plakate_map, menu);

        return true;
    }

    private void BuildMap() {
        final List<Overlay> overlays = mMapView.getOverlays();
        overlays.clear();

        new Thread(new Runnable() {

            public void run() {
                DBAdapter dba = new DBAdapter(DwjMap.this);
                try {
                    dba.open();
                    overlays.add(dba.getMapOverlay());
                } finally {
                    dba.close();
                }

            }
        }).start();

        if (mMyPosOverlay == null) {
            mMyPosOverlay = new CurrentPositionOverlay(this, mMapView);

            mMyPosOverlay.runOnFirstFix(new Runnable() {
                public void run() {
                    DwjMap.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mMapView.getZoomLevel() < INITIAL_ZOOM) {
                                mMapView.getController().setZoom(INITIAL_ZOOM);
                            }
                            mMapView.getController().animateTo(mMyPosOverlay.getMyLocation());
                        }
                    });
                }
            });
            mMyPosOverlay.enable();
        }
        overlays.add(mMyPosOverlay);
        mMapView.invalidate();
    }

    @Override
    protected void onResume() {
        if (mMyPosOverlay == null) {
            BuildMap();
        }
        if (mMyPosOverlay != null) {
            mMyPosOverlay.enable();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mMyPosOverlay != null) {
            mMyPosOverlay.disable();
        }
        super.onPause();
    }

    private void AddMarker() {

        boolean hasSyncedBefore = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SettingsActivity.KEY_HAS_SYNCED, false);
        if (!hasSyncedBefore) {
            new AlertDialog.Builder(this)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.warn_sync_first)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            startActivityForResult(
                    new Intent(DwjMap.this, PlakatDetailsActivity.class)
                    .putExtra(PlakatDetailsActivity.EXTRA_NEW_PLAKAT, true),
                    DwjMap.REQUEST_EDIT_PLAKAT);
        }
    }

    private void StartSync() {
        SyncController sc = new SyncController(this);

        sc.setOnCompleteListener(new Runnable() {
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DwjMap.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(SettingsActivity.KEY_HAS_SYNCED, true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    editor.apply();
                } else {
                    editor.commit();
                }
                BuildMap();
            }
        });
        sc.synchronize();
    }
}
