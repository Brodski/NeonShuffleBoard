package com.example.aloom.neonshuffleboardv2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class  GameSettingsActivity extends AppCompatActivity {

    SeekBar weightSpeed;
    double speed = 0;
    TextView weightSpeedTxt;
    Spinner colorThemes;
    TextView colorThemesText;
    String TAG = "Debug:";
    ArrayAdapter spinnerAdapter;
    String[]neonColors = {"lightNeonBlue","neonRed","neonGreen","neonYellow","lightNeonGreen","neonOrange","neonBlue","neonPink","neonBright","paleNeonGreen"
            ,"darkNeonOrange","darkNeonRed","neonTeal","neonOlive","paleNeonPink","paleNeonPink2"};
    HashMap<String,String>colorHexValues = new HashMap<>();
    String userWeightSetting ="userWeightSetting";




    //Initialize settings menu components
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_settings);
        weightSpeed = (SeekBar)findViewById(R.id.weightSpeed);
        weightSpeedTxt = (TextView)findViewById(R.id.weightSpeedText);
        colorThemes = (Spinner)findViewById(R.id.colorThemes);
        colorThemesText = (TextView)findViewById(R.id.colorThemesText);
        createColorList();
        createColorHashMap();
        setColorPreview();
        onWeightSpeedChanged();
        getSavedSettings();


    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getSharedPreferences("settings",  MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("userWeightSetting", (int) speed);
        editor.putInt("userColorSetting", colorThemes.getSelectedItemPosition());
        editor.commit();

    }

    private void setColorPreview() {
        colorThemes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                colorThemesText.setTextColor(hexToRGB(colorThemes.getSelectedItem().toString()));
                weightSpeedTxt.setTextColor(hexToRGB(colorThemes.getSelectedItem().toString()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    //Fill color spinner with color schemes
    private void createColorList(){
        spinnerAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,neonColors);
        colorThemes.setAdapter(spinnerAdapter);
    }

    public void createColorHashMap(){
        colorHexValues.put("lightNeonBlue","#00f6fe");
        colorHexValues.put("neonRed","#f92b2f");
        colorHexValues.put("neonGreen","#83f52c");
        colorHexValues.put("neonYellow","#f9fe44");
        colorHexValues.put("lightNeonGreen","#a1fc38");
        colorHexValues.put("neonOrange","#ff891f");
        colorHexValues.put("neonBlue","#1bbee3");
        colorHexValues.put("neonPink","#ff38d1");
        colorHexValues.put("neonBright","#e1fde9");
        colorHexValues.put("paleNeonGreen","#ccff6a");
        colorHexValues.put("darkNeonOrange","#ff6d35");
        colorHexValues.put("darkNeonRed","#ff0049");
        colorHexValues.put("neonTeal","#00ebb3");
        colorHexValues.put("neonOlive","#d6cd16");
        colorHexValues.put("paleNeonPink","#ff4171");
        colorHexValues.put("paleNeonPink2","#ff006b");
    }
    public int hexToRGB(String selectedColor){
        String retrievedColor = colorHexValues.get(selectedColor);
        int red = Integer.valueOf( retrievedColor.substring( 1, 3 ), 16 );
        int green = Integer.valueOf( retrievedColor.substring( 3, 5 ), 16 );
        int blue = Integer.valueOf( retrievedColor.substring( 5, 7 ), 16 );
        int convertedColor = Color.rgb(red,green,blue);
        return convertedColor;
    }


    private void getSavedSettings() {
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE );
        int savedWeightValue = settings.getInt("userWeightSetting", 0);
        int colorSelection = settings.getInt("userColorSetting", 0);
        colorThemes.setSelection(colorSelection);
        weightSpeed.setProgress(savedWeightValue);
    }


    public double getWeightSpeed(){
        return this.speed;
    }

    public void setWeightSpeed(double speed){
        this.speed = speed;
    }

    public void onWeightSpeedChanged(){
        weightSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed = weightSpeed.getProgress();
                weightSpeedTxt.setText(String.valueOf(speed));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //research
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //research
            }
        });
    }
}
