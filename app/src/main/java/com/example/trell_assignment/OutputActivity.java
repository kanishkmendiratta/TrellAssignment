package com.example.trell_assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class OutputActivity extends AppCompatActivity {

    private VideoView videoViewl;
    private MediaController mediaController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);

        Bundle extras=getIntent().getExtras();
        Uri videoUri = Uri.parse(extras.getString("VideoUri"));

        init();
        setMediaController();

        videoViewl.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        if(videoUri !=null){
            videoViewl.setVideoURI(videoUri);
            videoViewl.start();
        }

    }

    private void init(){
        this.videoViewl = findViewById(R.id.videoView2);
        this.mediaController=new MediaController(this);
    }

    private void setMediaController(){
        mediaController.setMediaPlayer(videoViewl);
        mediaController.setAnchorView(videoViewl);
        videoViewl.setMediaController(mediaController);
    }
}