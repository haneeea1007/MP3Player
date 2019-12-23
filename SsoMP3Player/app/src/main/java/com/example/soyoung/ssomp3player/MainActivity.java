package com.example.soyoung.ssomp3player;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView listView;
    private TextView txtMusic, txtTime, txtFile;
    private Spinner spGenre, spScore;
    private ImageButton btnLike, btnPlay, btnPause, btnStop, btnList;
    private ImageView imgAlbum;
    private SeekBar seekBar;
    private EditText edtSinger;
    private String[] genre = {"발라드", "hip-hop", "R&B", "팝송", "인디"};
    private String[] score = {"♥", "♥♥", "♥♥♥", "♥♥♥♥", "♥♥♥♥♥"};

    private MyDBHelper myDBHelper;
    private SQLiteDatabase sqLiteDatabase;
    private ArrayList<MusicData> arrayList = new ArrayList<MusicData>();

    private ArrayList<String> list = new ArrayList<String>();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String selectMP3;
    static final String MP3_PATH = Environment.getExternalStorageDirectory().getPath() + "/mp3/";
    private final int GET_GALLERY_IMAGE = 200;
    private boolean isPause = false; // 재생중인지 확인할 변수
    private long backButtonTime = 0;

    public class MyThread extends Thread {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        @Override
        public void run() {

            if (mediaPlayer == null) {
                return;
            }
            seekBar.setMax(mediaPlayer.getDuration());

            while (mediaPlayer.isPlaying()) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        txtTime.setText(simpleDateFormat.format(mediaPlayer.getCurrentPosition()));
                    }
                });
                SystemClock.sleep(200);
            } // end of while
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("냥플레이어");

        listView = findViewById(R.id.listView);
        txtMusic = findViewById(R.id.mTxtMusic);
        txtTime = findViewById(R.id.txtTime);
        btnLike = findViewById(R.id.btnLike);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.mBtnStop);
        btnList = findViewById(R.id.btnList);
        seekBar = findViewById(R.id.seekBar);

        myDBHelper = new MyDBHelper(this);
        sqLiteDatabase = myDBHelper.getReadableDatabase();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);

        File[] files = new File(MP3_PATH).listFiles();

        for (File file : files) {
            String fileName = file.getName();
            String extendName = fileName.substring(fileName.length() - 3);

            if (extendName.equals("mp3")) {
                list.add(fileName);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);
        listView.setItemChecked(0, true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectMP3 = list.get(position);
                txtMusic.setText(selectMP3);
            }
        });

        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnList.setOnClickListener(this);
        btnLike.setOnClickListener(this);

        btnPlay.setEnabled(true);
        btnPause.setEnabled(false);
        btnStop.setEnabled(false);
        btnList.setEnabled(true);
        btnLike.setEnabled(true);

        txtTime.setText("00:00");
        selectMP3 = list.get(0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }
        });

//        sqLiteDatabase = myDBHelper.getReadableDatabase();
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS musicTBL");
//        sqLiteDatabase.close();
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        long gapTime = currentTime - backButtonTime;

        if (gapTime >= 0 && gapTime <= 2000) {
            super.onBackPressed();
        } else {
            backButtonTime = currentTime;
            Toast.makeText(this, "한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        MyThread myThread = new MyThread();
        switch (view.getId()) {
            case R.id.btnPlay:
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(MP3_PATH + selectMP3);

                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    txtMusic.setText(selectMP3);

                    myThread.start();

                    isPause = true; // 시크바 스레드 반복

                    btnPlay.setEnabled(false);
                    btnPause.setEnabled(true);
                    btnStop.setEnabled(true);
                    btnList.setEnabled(true);
                    btnLike.setEnabled(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            case R.id.btnPause:

                if (isPause) {
                    mediaPlayer.start();
                    myThread.start();
                    isPause = false;
                } else {
                    mediaPlayer.pause();
                    isPause = true;
                }

                btnPlay.setEnabled(false);
                btnPause.setEnabled(true);
                btnStop.setEnabled(true);
                btnList.setEnabled(true);
                btnLike.setEnabled(true);

                break;

            case R.id.mBtnStop:

                isPause = false; // 쓰레드 종료

                mediaPlayer.stop();
                mediaPlayer.reset();

                txtMusic.setText("재생 중인 음악이 없습니다.");
                seekBar.setProgress(0);
                txtTime.setText("00:00");

                btnPlay.setEnabled(true);
                btnPause.setEnabled(false);
                btnStop.setEnabled(false);
                btnList.setEnabled(true);
                btnLike.setEnabled(true);

                break;

            case R.id.btnList:

                Intent intent = new Intent(this, MyListActivity.class);
                startActivity(intent);

                finish();

                break;

            case R.id.btnLike:

                try {
                    final View dialogView = View.inflate(MainActivity.this, R.layout.dialog, null);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

                    edtSinger = dialogView.findViewById(R.id.edtSinger);
                    spGenre = dialogView.findViewById(R.id.spGenre);
                    spScore = dialogView.findViewById(R.id.spScore);
                    imgAlbum = dialogView.findViewById(R.id.imgAlbum);

                    ArrayAdapter<String> adapterGenre = new ArrayAdapter<String>(
                            this, R.layout.support_simple_spinner_dropdown_item, genre);
                    spGenre.setAdapter(adapterGenre);

                    ArrayAdapter<String> adapterScore = new ArrayAdapter<String>(
                            this, R.layout.support_simple_spinner_dropdown_item, score);
                    spScore.setAdapter(adapterScore);

                    txtFile = dialogView.findViewById(R.id.txtFile);
                    txtFile.setText(selectMP3);

                    imgAlbum.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                            startActivityForResult(intent, GET_GALLERY_IMAGE);
                        }
                    });

                    dialog.setTitle("음악 상세설정");
                    dialog.setView(dialogView);
                    dialog.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_LONG).show();
                            sqLiteDatabase = myDBHelper.getWritableDatabase();

                            sqLiteDatabase.execSQL("INSERT INTO musicTBL values('" +
                                    selectMP3 + "','" +
                                    edtSinger.getText().toString().trim() + "','" +
                                    spGenre.getSelectedItem().toString() + "','" +
                                    spScore.getSelectedItem().toString() + "','" +
                                    imgAlbum.getResources().toString() + "');");

                            sqLiteDatabase.close();                     // 데이터베이스 닫기
                        }
                    });

                    dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_LONG).show();
                        }
                    });
                    dialog.show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "이미 등록된 음악입니다.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri selectedImageUri = data.getData();
            imgAlbum.setImageURI(selectedImageUri);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // MediaPlayer 해지
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}