package com.mehedihasan.languagetranslator;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // Creating All Variable
    private Spinner fromSpinner,toSpinner;
    private TextInputEditText sourceTV;
    private ImageView micView;
    private MaterialButton translateBTN;
    private TextView translateTV;

    //Create Array for FromLanguage
    String[] fromLanguage={"From","English","Afrikaans","Arabic","Belarusian","Bulgarian","Bengali","Catalan","Czech","Welsh","Hindi","Urdu"};
    //Creating Array for ToLanguage
    String[] toLanguage={"To","English","Afrikaans","Arabic","Belarusian","Bulgarian","Bengali","Catalan","Czech","Welsh","Hindi","Urdu"};

    //REQUESTED CODE
    private static final int REQUEST_PERMISSION_CODE =1;
    int languageCode,fromLanguageCode,toLanguageCode=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Connection to All XML ID
        connection();

        //Set OnItemListner to fromSpinner
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // GetLanguageCode is Method
                fromLanguageCode=getLanguageCode(fromLanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //Create Adapter For Spinner
        ArrayAdapter fromAdapter=new ArrayAdapter(this,R.layout.spinner_item,fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                toLanguageCode=getLanguageCode(toLanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //Create Adapter for To Spinner
        ArrayAdapter toAdapter=new ArrayAdapter(this,R.layout.spinner_item,toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);


        //OnClick Listner To Translate Button
        translateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translateTV.setText("");
                if(sourceTV.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter Your Text To Translate", Toast.LENGTH_SHORT).show();
                }else if(fromLanguageCode==0){
                    Toast.makeText(MainActivity.this, "Please Enter Your Source Language", Toast.LENGTH_SHORT).show();
                }else if(toLanguageCode==0){
                    Toast.makeText(MainActivity.this, "Select Translated Language", Toast.LENGTH_SHORT).show();
                }else{
                   translateText(fromLanguageCode,toLanguageCode,sourceTV.getText().toString());
                }
            }
        });

        //OnClick Listner To Mic
        micView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speck To Convert Into Text");
                //Try Catch Method
                try {
                    startActivityForResult(intent,REQUEST_PERMISSION_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    //OnActivity Result Method


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_CODE){
            if(resultCode==RESULT_OK && data!=null){
                ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceTV.setText(result.get(0));
            }
        }
    }

    //Method for Translated language
    private void translateText(int fromLanguageCode,int toLanguageCode,String source){

        translateTV.setText("Downloading....");
        //Firebase Translator
        FirebaseTranslatorOptions options=new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();
        FirebaseTranslator translator= FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions=new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //After Downloading Model then Translating
                translateTV.setText("Translating.....");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translateTV.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail To Translate"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Fail To Download Language Model"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    //GetLanguageCode Method
    public int getLanguageCode(String language) {
        int languageCode=0;
        switch (language){
            case "English":
                languageCode= FirebaseTranslateLanguage.EN;
                break;
            case "Afrikaans":
                languageCode= FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                languageCode= FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                languageCode= FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                languageCode= FirebaseTranslateLanguage.BG;
                break;
            case "Bengali":
                languageCode= FirebaseTranslateLanguage.BN;
                break;
            case "Catalan":
                languageCode= FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languageCode= FirebaseTranslateLanguage.CS;
                break;
            case "Welsh":
                languageCode= FirebaseTranslateLanguage.CY;
                break;
            case "Hindi":
                languageCode= FirebaseTranslateLanguage.HI;
                break;
            case "Urdu":
                languageCode= FirebaseTranslateLanguage.UR;
                break;
            default:
                languageCode=0;
        }
        return languageCode;
    }

    //Connection to All XML ID
    private void connection() {
        fromSpinner=findViewById(R.id.fromSpinner);
        toSpinner=findViewById(R.id.toSpinner);
        sourceTV=findViewById(R.id.sourceTV);
        micView=findViewById(R.id.micView);
        translateBTN=findViewById(R.id.translateBTN);
        translateTV=findViewById(R.id.translateTV);
    }
}