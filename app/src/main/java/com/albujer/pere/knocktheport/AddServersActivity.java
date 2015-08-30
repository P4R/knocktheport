package com.albujer.pere.knocktheport;

import android.animation.LayoutTransition;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class AddServersActivity extends ActionBarActivity {

    private EditText etServer;
    private LinearLayout vlPorts;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_servers);
        setupUI();
    }

    public void addPort(View v) {
        View layout = getLayoutInflater().inflate(R.layout.port, null);

        if (v == null) {
            vlPorts.addView(layout);
        } else {
            int index = vlPorts.indexOfChild((View) v.getParent());
            vlPorts.addView(layout, index + 1);
        }
    }

    public void removePort(View v) {
        vlPorts.removeView((View) v.getParent());
    }

    public void save() {
        if (check()) {
            try {
                LinearLayout ll;
                ArrayList ports = new ArrayList();
                for (int i = 0; i < vlPorts.getChildCount(); i++) {
                    ll = (LinearLayout) vlPorts.getChildAt(i);

                    EditText et = (EditText) ll.getChildAt(1);
                    Spinner sp = (Spinner) ll.getChildAt(2);
                    ArrayList port = new ArrayList();
                    port.add(Integer.valueOf(et.getText().toString()));
                    port.add(sp.getSelectedItem().toString());
                    ports.add(port);
                }
                JSONArray list = readJSONArray();
                JSONObject server = new JSONObject();
                server.put("name", etServer.getText().toString());
                server.put("ports", ports);
                if (position == -1) {
                    list.put(server);
                    saveJSONArray(list);
                } else {
                    //jsonArray.remove(listPosition);
                    //Se ha eliminado de forma manual por compatibilidad de la API
                    JSONArray newJsonArray = new JSONArray();
                    for (int i = 0; i < list.length(); i++) {
                        if (i != position)
                            newJsonArray.put(list.get(i));
                    }
                    newJsonArray.put(position, server);
                    saveJSONArray(newJsonArray);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            finish();
        }
    }

    public void saveJSONArray(JSONArray list) {
        editor.putString("Servers", list.toString());
        editor.commit();
    }

    public JSONArray readJSONArray() {
        JSONArray list = new JSONArray();
        try {
            list = new JSONArray(pref.getString("Servers", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean check() {
        LinearLayout ll;
        boolean res = true;
        for (int i = vlPorts.getChildCount()-1; i >=0; i--) {
            ll = (LinearLayout) vlPorts.getChildAt(i);
            int port;
            EditText et = (EditText) ll.getChildAt(1);

            if (et.getText().toString().equals("")) {
                et.setError(getResources().getString(R.string.strRequieredField));
                res = false;
            } else {
                try {
                    port = Integer.valueOf(et.getText().toString());
                } catch (NumberFormatException e) {
                    port = -1;
                }

                if (port < 0 || port >= 65535) {
                    et.setError(getResources().getString(R.string.strInvalidValue));
                    res = false;
                }
            }
            et.requestFocus();
        }
        if (etServer.getText().toString().equals("")) {
            etServer.setError(getResources().getString(R.string.strRequieredField));
            etServer.requestFocus();
            res = false;
        }
        return res;
    }

    private void setupUI() {
        etServer = (EditText) findViewById(R.id.etServer);
        vlPorts = (LinearLayout) findViewById(R.id.vlPorts);
        LayoutTransition transition = new LayoutTransition();
        transition.setStartDelay(LayoutTransition.APPEARING, 0);
        transition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        transition.setDuration(LayoutTransition.APPEARING, 300);
        transition.setDuration(LayoutTransition.CHANGE_DISAPPEARING, 300);
        vlPorts.setLayoutTransition(transition);
        pref = getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        if (position != -1) {
            setTitle(getResources().getString(R.string.strEdit));
            try {
                JSONArray jsonArray = readJSONArray();
                JSONObject jsonObject = jsonArray.getJSONObject(position);
                etServer.setText(jsonObject.getString("name"));
                JSONArray list = new JSONArray(jsonObject.getString("ports"));
                LinearLayout layout = null;
                for (int i = 0; i < list.length(); i++) {
                    addPort(null);
                    JSONArray l = (JSONArray) list.get(i);
                    layout = (LinearLayout) vlPorts.getChildAt(i);
                    EditText et = (EditText) layout.getChildAt(1);
                    Spinner sp = (Spinner) layout.getChildAt(2);
                    et.setText(l.get(0).toString());
                    int spValue = 0;
                    if (l.get(1).toString().equals("TCP")) spValue = 0;
                    if (l.get(1).toString().equals("UDP")) spValue = 1;
                    sp.setSelection(spValue);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            addPort(null);
        }
        etServer.requestFocus();
        LinearLayout ll = (LinearLayout) vlPorts.getChildAt(0);
        ll.removeViewAt(4);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_acerca) {
            final AlertDialog alertDialog = new AlertDialog.Builder(AddServersActivity.this).create();
            alertDialog.setTitle(getResources().getString(R.string.strAbout)+" "+ getResources().getString(R.string.app_name))
            ;
            alertDialog.setMessage(
                    getResources().getString(R.string.strVersion) + ": " + BuildConfig.VERSION_NAME + "\n" +
                            getResources().getString(R.string.strAuthor)+ ": "+ getResources().getString(R.string.Iam)
            );
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.strClose),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.md_blue_500));
                }
            });

            alertDialog.show();
            return true;
        }
        if (id == R.id.action_save) {
            save();
        }


        return super.onOptionsItemSelected(item);
    }
}
