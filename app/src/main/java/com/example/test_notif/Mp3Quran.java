package com.example.test_notif;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class Mp3Quran extends AppCompatActivity  implements MediaPlayer.OnBufferingUpdateListener,MediaPlayer.OnCompletionListener {

    private TextView testView;
    private ImageButton btn_play_pause;
    private SeekBar seekBar;

    private MediaPlayer mediaPlayer;
    private int mediaFileLength;
    private int realTimeLength;
    final Handler handler = new Handler();

    String url = "https://server8.mp3quran.net/afs/001.mp3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_quran);

        final String[] links = getResources().getStringArray(R.array.links);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.surah));
        ListView listView = (ListView) findViewById(R.id.window_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                url=links[position];
            }
        });

        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(99);
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mediaPlayer.isPlaying())
                {
                    SeekBar seekBar = (SeekBar)v;
                    int playPosition = (mediaFileLength/100)*seekBar.getProgress();
                    mediaPlayer.seekTo(playPosition);
                }
                return false;
            }
        });

        testView = (TextView)findViewById(R.id.textTimer);

        btn_play_pause = (ImageButton) findViewById(R.id.btn_play_pause);
        btn_play_pause.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick (View v){

                final ProgressDialog mDialog = new ProgressDialog(Mp3Quran.this);

                AsyncTask<String,String,String> mp3Play = new AsyncTask<String, String, String>() {

                    @Override
                    protected void onPreExecute(){
                        mDialog.setMessage("Please Wait");
                        mDialog.show();
                    }

                    @Override
                    protected String doInBackground(String... params) {
                        try{
                            mediaPlayer.setDataSource(params[0]);
                            mediaPlayer.prepare();
                        }
                        catch (Exception ex){

                        }
                        return "";
                    }

                    @Override
                    protected void onPostExecute(String s){
                        mediaFileLength = mediaPlayer.getDuration();
                        realTimeLength = mediaFileLength;
                        if(!mediaPlayer.isPlaying()){
                            mediaPlayer.start();
                            btn_play_pause.setImageResource(R.drawable.ic_pause);
                        }
                        else{
                            mediaPlayer.pause();
                            btn_play_pause.setImageResource(R.drawable.ic_play);
                        }

                        updateSeekBar();
                        mDialog.dismiss();
                    }
                };

                mp3Play.execute(url);

            }
        });

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);

    }

    private void updateSeekBar() {
        seekBar.setProgress((int)(((float)mediaPlayer.getCurrentPosition() / mediaFileLength)*100));
        if(mediaPlayer.isPlaying())
        {
            Runnable updater = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                    realTimeLength-=1000; // declare 1 second
                    testView.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(realTimeLength),
                            TimeUnit.MILLISECONDS.toSeconds(realTimeLength) -
                                    TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(realTimeLength))));

                }

            };
            handler.postDelayed(updater,1000); // 1 second
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekBar.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        btn_play_pause.setImageResource(R.drawable.ic_play);

    }
}
