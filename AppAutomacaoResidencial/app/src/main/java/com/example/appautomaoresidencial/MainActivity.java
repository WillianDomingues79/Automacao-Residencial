package com.example.appautomaoresidencial;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter meuBluetooth = null;

    private static final int SOLICITA_ATIVACAO = 1;
    private static final int SOLICITA_CONEXÃO = 2;

    Button btnConexao, btnLed1;
    boolean conexao = false;

    private static String MAC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConexao = (Button)findViewById(R.id.btnConexao);
        btnLed1 = (Button)findViewById(R.id.btnLed1);

        meuBluetooth = BluetoothAdapter.getDefaultAdapter();


        if (meuBluetooth == null){
            Toast.makeText(getApplicationContext(),"Seu Dispositivo não possui Bluetooth", Toast.LENGTH_LONG).show();
        } else if (!meuBluetooth.isEnabled()){
            Intent ativaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBluetooth,SOLICITA_ATIVACAO);
        }

        btnConexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conexao){
                    //conectar
                } else {
                    Intent abreLista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abreLista, SOLICITA_CONEXÃO);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){

            case SOLICITA_ATIVACAO:
                if (resultCode == Activity.RESULT_OK){
                    Toast.makeText(getApplicationContext(),"O Bluetooth foi Ativado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),"O Bluetooth não foi Ativado, o App será encerrado", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case SOLICITA_CONEXÃO:
                if (resultCode == Activity.RESULT_OK){
                    MAC = data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);

                    Toast.makeText(getApplicationContext(),"MacFinal: " + MAC, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),"Falha ao Obter o MAC", Toast.LENGTH_LONG).show();

                }
        }
    }
}
