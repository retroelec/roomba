package ch.retroelec.roomba.roombaapp;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    public static final String IPI = "192.168.1.199";
    public static final String IPE = "11.11.11.11";

    private TextView mTextResult;
    private static String ip = IPI;

    public static String getIp() {
        return ip;
    }

    private static boolean onTouch(MotionEvent event, Button button, TCPCmd cmd, final TextView res) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            button.setBackgroundColor(Color.RED);
            try {
                res.setText(new TCPClient().execute(cmd).get());
                final Animation anim = new AlphaAnimation(1.0f, 0.0f);
                anim.setDuration(3000);
                res.startAnimation(anim);
                res.postDelayed(new Runnable() {
                    public void run() {
                        res.setVisibility(View.INVISIBLE);
                    }
                }, 3000);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            button.setBackgroundColor(Color.TRANSPARENT);
        }
        return true;
    }

    private static boolean onTouchDriveBtn(MotionEvent event, ImageButton imageButton, TCPCmd cmd) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            imageButton.setBackgroundColor(Color.RED);
            new TCPClient().execute(cmd);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            new TCPClient().execute(TCPCmd.DRIVESTOP);
            imageButton.setBackgroundColor(Color.TRANSPARENT);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // result field
        setContentView(R.layout.activity_main);
        mTextResult = findViewById(R.id.textresult);

        // set screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // cmd buttons
        final Button startButton = (Button) findViewById(R.id.startbutton);
        startButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.onTouch(event, startButton, TCPCmd.START, mTextResult);
            }
        });
        final Button stopButton = (Button) findViewById(R.id.stopbutton);
        stopButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.onTouch(event, stopButton, TCPCmd.STOP, mTextResult);
            }
        });
        final Button cleanButton = (Button) findViewById(R.id.cleanbutton);
        cleanButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.onTouch(event, cleanButton, TCPCmd.CLEAN, mTextResult);
            }
        });
        final Button spotButton = (Button) findViewById(R.id.spotbutton);
        spotButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.onTouch(event, spotButton, TCPCmd.SPOT, mTextResult);
            }
        });
        final Button manualButton = (Button) findViewById(R.id.manualbutton);
        manualButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.onTouch(event, manualButton, TCPCmd.MANUAL, mTextResult);
            }
        });
        final Button broomButton = (Button) findViewById(R.id.broombutton);
        broomButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.onTouch(event, broomButton, TCPCmd.BROOM, mTextResult);
            }
        });
        final Button irsensorButton = (Button) findViewById(R.id.irsensorbutton);
        irsensorButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.onTouch(event, irsensorButton, TCPCmd.IRSENSOR, mTextResult);
            }
        });
        final Button temperatureButton = (Button) findViewById(R.id.temperaturebutton);
        temperatureButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.onTouch(event, temperatureButton, TCPCmd.TEMPERATURE, mTextResult);
            }
        });
        final Button batteryButton = (Button) findViewById(R.id.batterybutton);
        batteryButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.onTouch(event, batteryButton, TCPCmd.BATTERY, mTextResult);
            }
        });
        final Button xmasButton = (Button) findViewById(R.id.xmasbutton);
        xmasButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.onTouch(event, xmasButton, TCPCmd.XMAS, mTextResult);
            }
        });

        // arrow buttons
        final ImageButton arrowupButton = (ImageButton) findViewById(R.id.arrowup);
        arrowupButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.this.onTouchDriveBtn(event, arrowupButton, TCPCmd.DRIVEFWD);
            }
        });
        final ImageButton arrowdownButton = (ImageButton) findViewById(R.id.arrowdown);
        arrowdownButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.this.onTouchDriveBtn(event, arrowdownButton, TCPCmd.DRIVEBCK);
            }
        });
        final ImageButton arrowleftButton = (ImageButton) findViewById(R.id.arrowleft);
        arrowleftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.this.onTouchDriveBtn(event, arrowleftButton, TCPCmd.DRIVELEFT);
            }
        });
        final ImageButton arrowrightButton = (ImageButton) findViewById(R.id.arrowright);
        arrowrightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.this.onTouchDriveBtn(event, arrowrightButton, TCPCmd.DRIVERIGHT);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.internalIP:
                ip = IPI;
                return true;
            case R.id.externalIP:
                ip = IPE;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
