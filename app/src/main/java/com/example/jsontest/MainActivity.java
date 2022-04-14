package com.example.jsontest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";
    ArrayList<AddressData> addressDatas = new ArrayList<>();
    ArrayList<String> datas = new ArrayList<>();

    String[] permissions = new String[] {
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //確認權限，讀寫檔案要權限
        if(checkPermissionAllGranted(permissions)){
            showDialog();
        } else {
            ActivityCompat.requestPermissions(this, permissions,100);
        }


    }

    //檢查權限
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            boolean isAllGranted = true;

            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                showDialog();

            } else {
                Toast.makeText(this, "權限被拒絕",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("網路還讀檔?");
        builder.setPositiveButton("網路", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                initData(0);
            }
        });
        builder.setNegativeButton("讀檔", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                initData(1);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void initData(int mode){
        ListView lv1 = findViewById(R.id.lv_1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String jsonString = "";
                switch (mode){
                    case 0:
                        //getApi拿json
                        try {
                            jsonString = JsonReader.getJSON("http://mysql2.im.ukn.edu.tw/~ttchen/api.php",9000);
                            Log.i(TAG, "JsonReader.getJSON: " + jsonString);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        //懶的手動放檔案，先寫再讀
                        File jsonFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Address.json");
                        writeInfo(jsonFile,"[{\"city\":\"彭湖縣\",\"cn\":\"彭湖重光漁港\",\"addr\":\"彭湖縣吉林路23號\",\"tel\":\"0933123456\"},{\"city\":\"彭湖縣\",\"cn\":\"彭湖吉貝漁港\",\"addr\":\"彭湖縣吉貝路66號\",\"tel\":\"0933564532\"},{\"city\":\"新北市\",\"cn\":\"淡水客船碼頭\",\"addr\":\"新北市淡水路1234號\",\"tel\":\"09965646\"},{\"city\":\"屏東縣\",\"cn\":\"小琉球大福漁港\",\"addr\":\"屏東縣大福路37號\",\"tel\":\"0968123456\"}]");
                        jsonString = readInfo(jsonFile);
                        break;
                }

                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(jsonString);
                    for (int i = 0;i < jsonArray.length();i++){
                        JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                        AddressData addressData = new AddressData(
                                jsonObject.optString("city"),
                                jsonObject.optString("cn"),
                                jsonObject.optString("addr"),
                                jsonObject.optString("tel")
                        );
                        datas.add(addressData.cn);
                        addressDatas.add(addressData);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException: " + e);
                }

                ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,
                        android.R.layout.simple_list_item_1,
                        datas);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv1.setAdapter(adapter);
                    }
                });

                lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(MainActivity.this,"city: "+ addressDatas.get(position).city + "\n地址: " + addressDatas.get(position).addr + "\n電話: " + addressDatas.get(position).tel, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).start();
    }

    public void writeInfo(File file, String strWrite) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(strWrite);

            bw.close();

        } catch (IOException e) {
            Log.e(TAG, "writeInfo: " + e);
        }
    }

    public String readInfo(File file){
        BufferedReader br = null;
        String response = null;

        try {
            StringBuffer output = new StringBuffer();
            br = new BufferedReader(new FileReader(file.getPath()));
            String line = "";
            while ((line = br.readLine()) != null) {
                output.append(line +"\n");
            }
            response = output.toString();
            br.close();

        } catch(FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e);

            return null;
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e);
            return null;

        }
        return response;
    }
}