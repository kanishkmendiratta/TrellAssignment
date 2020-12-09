package com.example.trell_assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class VideoCompressionActivity extends AppCompatActivity {

    private VideoView videoViewl;
    private Button comressionBtn;
    private FFmpeg ffmpeg;
    private String outputFileAbsolutePath;
    private String inputFileAbsolutePath;
    private EditText bitrate;
    private MediaController mediaController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_compression);

        Bundle extras=getIntent().getExtras();
        Uri videoUri = Uri.parse(extras.getString("VideoUri"));

        init();
        setMediaController();
        loadFFMpeg();

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

        //File inputFile= new File(videoUri.getPath());
        inputFileAbsolutePath = getRealPathFromURI(getApplicationContext(),videoUri);
        File folder = new File(Environment.getExternalStorageDirectory() + "/compressed");
        if(!folder.exists()){
            folder.mkdir();
        }
        File outputFile = new File(folder,"output.mp4");
        outputFileAbsolutePath=outputFile.getAbsolutePath();
        System.out.println("input"+inputFileAbsolutePath);
        System.out.println("output"+outputFileAbsolutePath);

        comressionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bitrate.getText().toString().isEmpty()){
                    String[] command = {"-y", "-i", inputFileAbsolutePath, "-s", "160x120", "-r", "25", "-vcodec", "mpeg4", "-b:v", bitrate.getText().toString()+"k", "-b:a", "48000", "-ac", "2", "-ar", "22050", outputFileAbsolutePath};

                    execFFmpegBinary(command);
                }
                else{
                    Toast.makeText(VideoCompressionActivity.this, "Please enter the bitrate", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    private void init(){
        this.bitrate=findViewById(R.id.ET_bitrate);
        this.comressionBtn=findViewById(R.id.compressBtn);
        this.videoViewl = findViewById(R.id.videoView1);
        this.mediaController=new MediaController(this);
    }

    private void setMediaController(){
        mediaController.setMediaPlayer(videoViewl);
        mediaController.setAnchorView(videoViewl);
        videoViewl.setMediaController(mediaController);
    }

    public void loadFFMpeg(){
        try {
            if (ffmpeg == null) {
                ffmpeg = FFmpeg.getInstance(this);
            }

            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Toast.makeText(VideoCompressionActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess() {
                    Toast.makeText(VideoCompressionActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinish() {

                }

                @Override
                public void onStart() {

                }
            });
        }
        catch (FFmpegNotSupportedException e){
            System.out.println("Upp"+e.getMessage());
        }
    }
    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {

                @Override
                public void onFailure(String message) {
                    System.out.println("Fail inside "+message);
                    Toast.makeText(VideoCompressionActivity.this, "Please check your Permissions", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String message) {
                    System.out.println("Done "+message);
                }

                @Override
                public void onProgress(String message) {
                    System.out.println("progress "+message);
                }

                @Override
                public void onStart() {
                    System.out.println("start");
                    comressionBtn.setVisibility(View.GONE);
                    findViewById(R.id.progress).setVisibility(View.VISIBLE);
                }

                @Override
                public void onFinish() {
                    System.out.println("Finish");
                    comressionBtn.setVisibility(View.VISIBLE);
                    findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                    Uri outputUri=Uri.fromFile(new File(outputFileAbsolutePath));
                    Intent i1 = new Intent(VideoCompressionActivity.this, OutputActivity.class);
                    i1.putExtra("VideoUri", outputUri.toString());
                    startActivity(i1);
                }
            });
        }
        catch (FFmpegCommandAlreadyRunningException e){
            System.out.println("Fail outside "+e.getMessage());
        }

    }
}