package com.example.soyoung.ssomp3player;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MyListActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private TextView mTxtMusic, mTxtTime;
    private ImageButton mBtnHome, mBtnPlay, mBtnPause, mBtnStop;
    private SeekBar mSeekBar;

    private MyDBHelper myDBHelper;
    private SQLiteDatabase sqLiteDatabase;
    private LinearLayoutManager linearLayoutManager;

    private ArrayList<MusicData> musicList = new ArrayList<MusicData>();
    private MyAdapter myAdapter;
    private MusicData musicData;

    private ArrayList<String> list = new ArrayList<String>();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    static public String selectMP3 = null;
    private boolean isPause = false; // 재생중인지 확인할 변수
    private int pos; // 재생 멈춘 시점

    private String MP3_PATH = Environment.getExternalStorageDirectory().getPath() + "/mp3/";

    public class MyThread extends Thread {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        @Override
        public void run() {

            if (mediaPlayer == null) {
                return;
            }
            mSeekBar.setMax(mediaPlayer.getDuration());

            while (mediaPlayer.isPlaying()) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                        mTxtTime.setText(simpleDateFormat.format(mediaPlayer.getCurrentPosition()));
                    }
                });
                SystemClock.sleep(200);
            } // end of while
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);
        setTitle("냥플레이어");

//        finish();

        recyclerView = findViewById(R.id.recyclerView);
        mTxtMusic = findViewById(R.id.mTxtMusic);
        mTxtTime = findViewById(R.id.mTxtTime);
        mBtnHome = findViewById(R.id.mBtnHome);
        mBtnPlay = findViewById(R.id.mBtnPlay);
        mBtnPause = findViewById(R.id.mBtnPause);
        mBtnStop = findViewById(R.id.mBtnStop);
        mSeekBar = findViewById(R.id.mSeekBar);

        myDBHelper = new MyDBHelper(this);

        mBtnHome.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);
        mBtnPause.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);

        // recyclerView 에 데이터 뿌리기
        sqLiteDatabase = myDBHelper.getReadableDatabase();      // 데이터 읽기 기능 받기
        Cursor cursor;                                          // 커서 만들기 (만들어진 행들을 담는 객체 - recordSet)
        cursor = sqLiteDatabase.rawQuery("SELECT * FROM musicTBL;", null);      // sql 커서에 넣기

        while (cursor.moveToNext()) {                          // 커서에서 가장 위에 있는 행
            musicData = new MusicData(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
            musicList.add(musicData);
        }

        cursor.close();                             // 커서 닫기
        sqLiteDatabase.close();                     // 데이터베이스 닫기.

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        myAdapter = new MyAdapter(R.layout.list_item, musicList);
        recyclerView.setAdapter(myAdapter);

        mBtnHome.setEnabled(true);
        mBtnPlay.setEnabled(true);
        mBtnPause.setEnabled(false);
        mBtnStop.setEnabled(false);

        mTxtTime.setText("00:00");
//        selectMP3 = musicList.get(0);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        MyThread myThread = new MyThread();
        switch (view.getId()) {

            case R.id.mBtnHome:

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

                break;

            case R.id.mBtnPlay:
                try {

                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(MP3_PATH + selectMP3);

                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    mTxtMusic.setText(selectMP3);

                    myThread.start();

                    isPause = true; // 시크바 스레드 반복

                    mBtnPlay.setEnabled(false);
                    mBtnPause.setEnabled(true);
                    mBtnStop.setEnabled(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            case R.id.mBtnPause:

                if (isPause) {
                    mediaPlayer.start();
                    myThread.start();
                    isPause = false;
                } else {
                    mediaPlayer.pause();
                    isPause = true;
                }

                mBtnPlay.setEnabled(false);
                mBtnPause.setEnabled(true);
                mBtnStop.setEnabled(true);

                break;

            case R.id.mBtnStop:
                isPause = false; // 쓰레드 종료

                mediaPlayer.stop();
                mediaPlayer.reset();

                mTxtMusic.setText("재생 중인 음악이 없습니다.");
                mSeekBar.setProgress(0);
                mTxtTime.setText("00:00");

                mBtnPlay.setEnabled(true);
                mBtnPause.setEnabled(false);
                mBtnStop.setEnabled(false);

                break;
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.CustomViewHolder> {

        private int layout;
        private ArrayList<MusicData> list;
        private MyDBHelper myDBHelper;
        private SQLiteDatabase sqLiteDatabase;

        public TextView tvTitle;
        public TextView tvSinger;
        public TextView tvGenre;
        public TextView tvScore;
        public ImageView ivAlbum;

        LinearLayout linearLayout;

        public MyAdapter(int layout, ArrayList<MusicData> list) {
            this.layout = layout;
            this.list = list;
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            myDBHelper = new MyDBHelper(viewGroup.getContext());
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false);

            // 해당된 뷰홀더 아이디 찾기 --> onBindViewHolder 메소드
            CustomViewHolder viewHolder = new CustomViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final CustomViewHolder customViewHolder, final int position) {
            tvTitle.setText(list.get(position).getTitle());
            tvSinger.setText(list.get(position).getSinger());
            tvGenre.setText(list.get(position).getGenre());
            tvScore.setText(list.get(position).getScore());

            customViewHolder.itemView.setTag(position); // 한 칸

            customViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());

                    dialog.setTitle("삭제 확인");
                    dialog.setMessage("정말 삭제하시겠습니까?");

                    dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sqLiteDatabase = myDBHelper.getWritableDatabase();
                            sqLiteDatabase.execSQL("DELETE FROM musicTBL WHERE title = '"
                                    + list.get(position).getTitle() + "';");

                            list.remove(position);
                            notifyItemChanged(position);
                            notifyItemRemoved(position);

                            sqLiteDatabase.close();

                            Toast.makeText(view.getContext(), "삭제가 완료되었습니다.", Toast.LENGTH_LONG).show();
                        }
                    });

                    dialog.setNegativeButton("취소", null);

                    dialog.show();

                    return true;
                }
            });

            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMP3 = list.get(position).getTitle();
                    mTxtMusic.setText(selectMP3);
                }
            });

        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {


            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvSinger = itemView.findViewById(R.id.tvSinger);
                tvGenre = itemView.findViewById(R.id.tvGenre);
                tvScore = itemView.findViewById(R.id.tvScore);
                ivAlbum = itemView.findViewById(R.id.ivAlbum);
                linearLayout = itemView.findViewById(R.id.linearLayout);

            }
        }
    }
}
