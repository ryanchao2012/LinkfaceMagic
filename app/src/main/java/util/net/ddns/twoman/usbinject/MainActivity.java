package util.net.ddns.twoman.usbinject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import util.net.ddns.twoman.usbinject.starter.TouchPanelStarter;


public class MainActivity extends Activity {
    private static String TAG="MainActivity";
    MainActivity self;


    TextView tvInfo = null;
    TextView tvPos = null;
    //========================================
    private void setupView(){
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        setContentView(panel);
        Button b = new Button(this);
        b.setText("Exit");
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(self != null) {
                    self.finish();
                }
                else{
                    finish();
                }
            }
        });
        panel.addView(b);
        tvPos = new TextView(this);
        tvInfo = new TextView(this);
        panel.addView(tvInfo);
        panel.addView(tvPos);
    }
    Thread.UncaughtExceptionHandler MainThreadUncaughtExceptionHandler= new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, final Throwable ex) {
            self.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(self, ex.toString(), Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(self);
                    alertBuilder.setTitle("Exception");
                    alertBuilder.setMessage(ex.toString());
                    alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertBuilder.setCancelable(true);
                    alertBuilder.create().show();
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(self == null){
            self= this;
        }
        setupView();

    }

    public void onStart() {
        super.onStart();
        if (self == null) {
            self = this;
        }
        TouchPanelStarter.New(self).Start();
        Thread.currentThread().setUncaughtExceptionHandler(MainThreadUncaughtExceptionHandler);
    }

    // 避免切換方向時crash
    protected void onPause(){
        super.onPause();

    }
    protected void onStop(){
        super.onStop();
    }
    protected void onResume(){
        super.onResume();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }




}