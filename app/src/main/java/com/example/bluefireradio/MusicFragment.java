package com.example.bluefireradio;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Random;

public class MusicFragment extends Fragment {

    SeekBar seekBar;
    TextView songArtist, songName;
    ImageView imageViewPlayPause;
    ImageView imageViewPrevious;
    ImageView imageViewNext;

    private MediaPlayer mMediaPlayer;

    int lastSongNumber;
    int currentSongNumber;
    boolean hasGottenFirstSong;

    public MusicFragment() {}

    public static MusicFragment newInstance() {
        return new MusicFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_music, container, false);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        fetchAndPlaySong(false);

        seekBar = (SeekBar) v.findViewById(R.id.musicProgressBar);

        songName = (TextView) v.findViewById(R.id.songName);
        songArtist = (TextView) v.findViewById(R.id.songArtist);

        imageViewPlayPause = (ImageView) v.findViewById(R.id.image_view_play_pause);
        imageViewNext = (ImageView) v.findViewById(R.id.image_view_next);
        imageViewPrevious = (ImageView) v.findViewById(R.id.image_view_previous);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mMediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        imageViewPlayPause.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    imageViewPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);

                } else {
                    mMediaPlayer.start();
                    imageViewPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);

                }
            }
        });

        imageViewPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAndPlaySong(false);
            }
        });

        imageViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAndPlaySong(true);
            }
        });

        return v;
    }

    /*public void getPreviousSong(){

        imageViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int startTime = mMediaPlayer.getCurrentPosition();
                int previousTime = 10000;
                startTime -= previousTime;

                if(startTime >= 0){
                    mMediaPlayer.seekTo(startTime);
                }
                else{
                    mMediaPlayer.seekTo(0);
                    seekBar.setMax(mMediaPlayer.getDuration());
                    mMediaPlayer.start();
                }
            }
        });
    }*/



    // If true, play next song, else play previous song
    private void fetchAndPlaySong(final boolean shouldPlayNextSong) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("music");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot snapshot;
                if (hasGottenFirstSong) {
                    if (shouldPlayNextSong) {
                        lastSongNumber = currentSongNumber;
                        currentSongNumber = randomInt(0, 9);
                    } else {
                        currentSongNumber = lastSongNumber;
                    }
                } else {
                    currentSongNumber = randomInt(0, 9);
                }
                snapshot = dataSnapshot.child(String.valueOf(currentSongNumber));
                StorageReference storageRef = storage.getReferenceFromUrl(snapshot.child("url").getValue().toString());
                songName.setText(snapshot.child("name").getValue().toString());
                songArtist.setText(snapshot.child("artist").getValue().toString());
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            // Download url of file
                            String songUrl = uri.toString();
                            if (mMediaPlayer.isPlaying()) {
                                mMediaPlayer.stop();
                                mMediaPlayer.release();
                                seekBar.setProgress(0);
                                mMediaPlayer = new MediaPlayer();
                                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            }
                            mMediaPlayer.setDataSource(songUrl);
                            // wait for media player to get prepare
                            mMediaPlayer.prepare();
                            seekBar.setMax(mMediaPlayer.getDuration());
                            mMediaPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("TAG", e.getMessage());
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static int randomInt(int min, int max) {
        Random rand = new Random();

        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }


    /*foward button listener and onclick
    songTwo = songOne

    songOne=give new value
    attach media player to songOn
    imageViewPlayPause song one


    */
    /*backbutton listener and onclick

    attach media player to songTwo
         imageViewPlayPause song Two
     */


}
