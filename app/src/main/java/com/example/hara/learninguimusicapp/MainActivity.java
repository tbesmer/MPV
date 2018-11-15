package com.example.hara.learninguimusicapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        HomeFragment.onHomeFragment,
        MusicFragment.onMusicFragment,
        PhotosFragment.onPhotoFragment,
        AlbumPicturesFragment.onAlbumPicturesFragment,
        VideoFragment.onVideoFragment {

    private DrawerLayout drawer;
    NavigationView navigationView;

    int container = R.id.fragment_container;
    public static String albumNameKey = "album name";
    public static String albumListKey = "album list";
    public static String galleryPathKey = "path";

    static final int REQUEST_PERMISSION_KEY = 1;
    LoadAlbum loadAlbumTask;
    GridView galleryGridView;
    ArrayList<HashMap<String, String>> albumList = new ArrayList<>();

    ImageButton play, pause, play_main, pause_main;
    ImageButton repeat, shuffle;
    private SlidingUpPanelLayout mLayout;
    boolean onRepeat = false;
    boolean onShuffle = false;
    SeekBar songTimeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!Function.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }

        play = findViewById(R.id.play_button);
        pause = findViewById(R.id.pause_button);
        play_main = findViewById(R.id.play_button_main);
        pause_main = findViewById(R.id.pause_button_main);
        repeat = findViewById(R.id.repeat_button);
        shuffle = findViewById(R.id.shuffle_button);
        songTimeBar = findViewById(R.id.song_time_seekbar);
        mLayout = findViewById(R.id.slidingPanel);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Song Is now Playing", Toast.LENGTH_SHORT).show();
                if (play_main.getVisibility() == View.VISIBLE) {
                    play_main.setVisibility(View.GONE);
                    pause_main.setVisibility(View.VISIBLE);
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Song is Pause", Toast.LENGTH_SHORT).show();
                if (pause_main.getVisibility() == View.VISIBLE) {
                    pause_main.setVisibility(View.GONE);
                    play_main.setVisibility(View.VISIBLE);
                }
            }
        });

        play_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play_main.setVisibility(View.GONE);
                pause_main.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Song Is now Playing", Toast.LENGTH_SHORT).show();
                if (play.getVisibility() == View.VISIBLE) {
                    play.setVisibility(View.GONE);
                    pause.setVisibility(View.VISIBLE);
                }
            }
        });

        pause_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause_main.setVisibility(View.GONE);
                play_main.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Song is Pause", Toast.LENGTH_SHORT).show();
                if (pause.getVisibility() == View.VISIBLE) {
                    pause.setVisibility(View.GONE);
                    play.setVisibility(View.VISIBLE);
                }
            }
        });

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!onRepeat) {
                    repeat.setImageResource(R.drawable.repeat_black);
                    onRepeat = true;
                } else {
                    repeat.setImageResource(R.drawable.repeat_white);
                    onRepeat = false;
                }
            }
        });

        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!onShuffle) {
                    shuffle.setImageResource(R.drawable.shuffle_black);
                    onShuffle = true;
                } else {
                    shuffle.setImageResource(R.drawable.shuffle_white);
                    onShuffle = false;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Log.d("demo", "pop stack: " + getSupportFragmentManager().getBackStackEntryCount());
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            // if music bar is open
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            // if music bar is closed
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                // if nav menu is open
                drawer.closeDrawer(GravityCompat.START);
            } else {
                // if nav is closed
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    Log.d("demo", "pop");
                    getSupportFragmentManager().popBackStack();
                } else {
                    Log.d("demo", "no more pop");
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.d("demo", "main menu " + menuItem);
        clearBackStack();
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                Log.d("demo", "menu clicked: nav_home");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, new HomeFragment())
                        .commit();
                break;
            case R.id.nav_music:
                Log.d("demo", "menu clicked: nav_music");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, new MusicFragment())
                        .commit();
                break;
            case R.id.nav_videos:
                Log.d("demo", "menu clicked: nav_videos");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, new VideoFragment())
                        .commit();
                break;
            case R.id.nav_photos:
                Log.d("demo", "menu clicked: nav_photos");
                PhotosFragment photosFragment = new PhotosFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, photosFragment)
                        .commit();
                Bundle bundle = new Bundle();
                bundle.putSerializable(albumListKey, albumList);
                photosFragment.setArguments(bundle);
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                break;
            default:
                navigationView.setCheckedItem(menuItem.getItemId());
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void fromHomeToOther(int num) {
        Log.d("demo", "in main.fromHomeToOther" + num);
        switch (num) {
            case 0: // music
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, new MusicFragment())
                        .addToBackStack(null)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_music);
                break;
            case 1: // video
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, new VideoFragment())
                        .addToBackStack(null)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_videos);
                break;
            case 2: // photos
                PhotosFragment photosFragment = new PhotosFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, photosFragment, "photo frag")
                        .addToBackStack(null)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_photos);
                Bundle bundle = new Bundle();
                bundle.putSerializable(albumListKey, albumList);
                photosFragment.setArguments(bundle);
                break;
            default:
                break;
        }
    }

    @Override
    public void fromAlbumToPictures(String title) {
        AlbumPicturesFragment albumPicturesFragment = new AlbumPicturesFragment();
//        Log.d("demo", "in main fromAlbumToPictures " + title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(container, albumPicturesFragment)
                .addToBackStack(null)
                .commit();
        Bundle bundle = new Bundle();
        bundle.putString(albumNameKey, title);
        albumPicturesFragment.setArguments(bundle);
    }

    @Override
    public void onClick(View v) {
//        Log.d("demo", "in main onClick");
        clearBackStack();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(container, new HomeFragment())
                .commit();
        navigationView.setCheckedItem(R.id.nav_home);
        setTitle("Home");
    }

    @Override
    public void fromPictureToGallery(String path) {
        Intent intent = new Intent(MainActivity.this, GalleryPreview.class);
        intent.putExtra(galleryPathKey, path);
        startActivity(intent);
    }

    @Override
    public void getBackButton() {
        Log.d("demo", "in main.getBackButton");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d("demo", "in main.onSupportNavigateUp");
        onBackPressed();
        return true;
    }

    @Override
    public void setFragmentTitle(String title) {
        setTitle(title);
    }

    public class LoadAlbum extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            albumList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            String countPhoto = null;
            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;


            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.MediaColumns.DATE_MODIFIED};
            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                    null, null);
            Cursor cursorInternal = getContentResolver().
                    query(uriInternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                            null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});

            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                countPhoto = Function.getCount(getApplicationContext(), album);

                albumList.add(Function.mappingInbox(album, path, timestamp, Function.convertToTime(timestamp), countPhoto));
            }
            cursor.close();
            Collections.sort(albumList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_KEY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("demo", "onRequestPermissionsResult Yes");
                    loadAlbumTask = new LoadAlbum();
                    loadAlbumTask.execute();
                } else {
                    Log.d("demo", "Noooo");
                    Toast.makeText(MainActivity.this, "You must accept permissions.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!Function.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        } else {
            loadAlbumTask = new LoadAlbum();
            loadAlbumTask.execute();
        }
    }

    private void clearBackStack() {
        Log.d("demo" , "in main.clearBackStack");
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(0);
            getSupportFragmentManager().popBackStack(entry.getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().executePendingTransactions();
        }
    }
}
