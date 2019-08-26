package com.example.appautomaoresidencial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter meuAdapter = null;
    BluetoothDevice meuDevice = null;
    BluetoothSocket meuSocket = null;

    UUID meuUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private static final int SOLICITA_ATIVACAO = 1;
    private static final int SOLICITA_CONEXÃO = 2;
    private static final int MESSAGE_READ = 3;

    ConnectedThread connectedThread;

    Handler mHandler;

    StringBuilder dadosBluetooth = new StringBuilder();

    Button btnConexao, btnLed1;
    boolean conexao = false;

    private static String MAC = null;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConexao = (Button)findViewById(R.id.btnConexao);
        btnLed1 = (Button)findViewById(R.id.btnLed1);

        meuAdapter = BluetoothAdapter.getDefaultAdapter();


        if (meuAdapter == null){
            Toast.makeText(getApplicationContext(),"Seu Dispositivo não possui Bluetooth", Toast.LENGTH_LONG).show();
            finish();
        } else if (!meuAdapter.isEnabled()){
            Intent ativaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBluetooth,SOLICITA_ATIVACAO);
        }

        btnConexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (conexao){
                    try{
                        //desconectar

                        meuSocket.close();
                        conexao = false;

                        btnConexao.setText("Conectar");


                        Toast.makeText(getApplicationContext(),"O Bluetooth foi desconectado", Toast.LENGTH_LONG).show();
                    }catch (IOException erro){
                        Toast.makeText(getApplicationContext(),"Ocorreu um Erro" + erro, Toast.LENGTH_LONG).show();
                    }
                } else {
                    //conectar
                    Intent abreLista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abreLista, SOLICITA_CONEXÃO);
                }
            }
        });

        btnLed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conexao) {
                    connectedThread.enviar("led1");

                }else{
                    Toast.makeText(getApplicationContext(),"O Bluetooth não está Conectado!", Toast.LENGTH_LONG).show();
                }
            }
        });

            //Verificar se mensagem está completa!
        mHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {

                if (msg.what == MESSAGE_READ){

                    String recebidos = (String) msg.obj;

                    dadosBluetooth.append(recebidos);

                    int fimInformacao = dadosBluetooth.indexOf("}");

                    if (fimInformacao > 0){

                        String dadosCompletos = dadosBluetooth.substring(0, fimInformacao);

                        int tamInformacao = dadosCompletos.length();

                        if (dadosBluetooth.charAt(0) == '{'){
                            String dadosFinais = dadosBluetooth.substring(1, tamInformacao);

                            Log.d(dadosFinais, "handleMessage: Recebidos");

                            if (dadosFinais.contains("l1on")){
                                btnLed1.setText("LED 1 Ligado");
                                Log.d("LED1", "Ligado");
                            } else if(dadosFinais.contains("l1of")){
                                btnLed1.setText("LED 1 Desligado");
                                Log.d("LED1", "Desligado");
                            }
                        }
                        dadosBluetooth.delete(0, dadosBluetooth.length());
                    }
                }
            }
        };
    }

    //INICIO CONEXÃO DO BLUETOOTH
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

                    //Toast.makeText(getApplicationContext(),"MacFinal: " + MAC, Toast.LENGTH_LONG).show();

                    meuDevice = meuAdapter.getRemoteDevice(MAC);

                    try {
                        meuSocket = meuDevice.createRfcommSocketToServiceRecord(meuUUID);
                        meuSocket.connect();
                        conexao = true;

                        connectedThread = new ConnectedThread(meuSocket);
                        connectedThread.start();

                        btnConexao.setText("Desconectar");

                        Toast.makeText(getApplicationContext(),"Você foi conectado com: " + MAC, Toast.LENGTH_LONG).show();

                    }catch (IOException erro){
                        conexao = false;

                        Toast.makeText(getApplicationContext(),"Ocorreu um Erro: " + erro, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Falha ao Obter o MAC", Toast.LENGTH_LONG).show();

                }
        }
    }
    //FINAL CONEXÃO DO BLUETOOTH

    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    String dadosBt = new String(buffer, 0, bytes);

                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, dadosBt).sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }

        public void enviar(String dadosEnviar) {
            byte[] msgBuffer = dadosEnviar.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) { }
        }


    }

}
