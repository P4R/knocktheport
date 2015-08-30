package com.albujer.pere.knocktheport;

import android.animation.LayoutTransition;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private ListView lv;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getResources().getString(R.string.main_act_name));
        setupUI();
        setupLV();
    }

    public void saveJSONArray(JSONArray list){
        editor.putString("Servers", list.toString());
        editor.commit();
    }

    public JSONArray readJSONArray(){
        JSONArray list = new JSONArray();
        try{
            list = new JSONArray(pref.getString("Servers", ""));
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public void addServer(View v){
        Intent intent = new Intent(getApplicationContext(), AddServersActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupLV();
    }

    private void connect(int position){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Boolean res=true;
        try {
            JSONObject jsonObject = readJSONArray().getJSONObject(position);
            String server = jsonObject.getString("name");
            InetAddress address = InetAddress.getByName(server);
            JSONArray list = new JSONArray(jsonObject.getString("ports"));
            PortKnocking knock = new PortKnocking();
            for (int i = 0; i < list.length(); i++) {
                if(res) {
                    int port = list.getJSONArray(i).getInt(0);
                    String type = list.getJSONArray(i).getString(1);
                    if (type.equals("TCP")) res = knock.knockTCP(address, port);
                    if (type.equals("UDP")) res = knock.knockUDP(address, port);
                }else{
                    break; //Si res es false hay un error dejamos de enviar las peticiones restantes
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            res=false;
        }
        String msg = res ? getResources().getString(R.string.strSucces) : getResources().getString(R.string.strError);
        Toast.makeText(getApplicationContext(),msg , Toast.LENGTH_SHORT).show();
    }

    private void setupLV() {
        LinearLayout empty = (LinearLayout)findViewById(android.R.id.empty);
        listdata = new ArrayList();
        JSONArray jArray = readJSONArray();
        if (jArray != null) {
            try {
                for (int i = 0; i < jArray.length(); i++) {
                    listdata.add(jArray.get(i).toString());
                }
            }catch (Exception e){e.printStackTrace();}
        }
        arrayAdapter = new KnockArrayAdapter(this, listdata);
        if (!arrayAdapter.isEmpty()) {
            empty.setVisibility(View.INVISIBLE);
            lv.setAdapter(arrayAdapter);
        }else{
            lv.setAdapter(arrayAdapter);
            LayoutTransition transition = new LayoutTransition();
            transition.setStartDelay(LayoutTransition.APPEARING, 0);
            transition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
            transition.setDuration(LayoutTransition.APPEARING, 300);
            transition.setDuration(LayoutTransition.CHANGE_DISAPPEARING, 300);
            empty.setLayoutTransition(transition);
            lv.setEmptyView(empty);
        }

    }
    private void setupUI() {
        lv = (ListView) findViewById(R.id.lv);
        registerForContextMenu(lv);
        pref = getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connect(position);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.lv) {
            menu.add(Menu.NONE, 0, 0, getResources().getString(R.string.strConnect));
            menu.add(Menu.NONE, 1, 1, getResources().getString(R.string.strEdit));
            menu.add(Menu.NONE, 2, 2, getResources().getString(R.string.strDelete));

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int menuItemIndex = item.getItemId();
        int position = info.position;

        if (menuItemIndex == 0) connect(position);

        if (menuItemIndex == 1) {
            Intent intent = new Intent(getApplicationContext(), AddServersActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        }

        if (menuItemIndex == 2) {
            JSONArray jsonArray = readJSONArray();

            //jsonArray.remove(listPosition);
            //Se ha eliminado de forma manual por compatibilidad de la API
            JSONArray newJsonArray=new JSONArray();
            try{
                for(int i=0;i<jsonArray.length();i++){
                    if(i!=position)
                        newJsonArray.put(jsonArray.get(i));
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            int p = lv.getFirstVisiblePosition();
            saveJSONArray(newJsonArray);
            setupLV();
            lv.setSelection(p);
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(getResources().getString(R.string.strAbout)+" "+ getResources().getString(R.string.app_name));
            alertDialog.setMessage(
                    getResources().getString(R.string.strVersion)+": "+ BuildConfig.VERSION_NAME+"\n"+
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

        return super.onOptionsItemSelected(item);
    }
}
