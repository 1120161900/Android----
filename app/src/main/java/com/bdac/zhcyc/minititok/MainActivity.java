package com.bdac.zhcyc.minititok;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bdac.zhcyc.minititok.UI.FeedsFragment;
import com.bdac.zhcyc.minititok.UI.MeFragment;
import com.bdac.zhcyc.minititok.Utilities.DatabaseUtils;
import com.bdac.zhcyc.minititok.Network.beans.Item;
import com.bdac.zhcyc.minititok.Utilities.NetworkUtils;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private final static int REQUST_CODE_CAMERA_AUDIO_STORAGE = 101;
    private final static String TAG = "MainActivity";

    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

    private FloatingActionButton fab;


    private static FeedsFragment feedsFragment;
    private static MeFragment meFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedsFragment = new FeedsFragment();
        meFragment = new MeFragment();
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_main);

        DatabaseUtils.dbInit(this);

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        ||
                        ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        ||
                        ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO
                            },
                            REQUST_CODE_CAMERA_AUDIO_STORAGE);
                }else{
                    startCamera();
                }
            }
        });

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(1);
        mPager.setPageTransformer(true, new PageTransformer());
    }


    private void startCamera () {
        startActivity(new Intent(MainActivity.this,CustomCameraActivtiy.class));
    }

    public static void refreshItems () {
        //TODO 刷新
        try {
            feedsFragment.refreshFeeds();
            meFragment.refreshData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseUtils.dbDestory();
    }

    public void onBackPressed() {
        if (mPager.getCurrentItem() == 1) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                return feedsFragment;
            }
            return meFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public class PageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(@NonNull View page, float position) {
            if (page == mPager.getChildAt(1)) {
                if (position == 0) {
                    Log.d(TAG, "transformPage: 1 pos 0");
                    //bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
                    TransitionManager.beginDelayedTransition((ViewGroup) fab.getParent());
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    fab.setLayoutParams(layoutParams);
                }
                //Log.d(TAG, "transformPage: " + position);
            }else {
                if (position == 0) {
                    Log.d(TAG, "transformPage: 0 pos 0");
                    //bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
                    TransitionManager.beginDelayedTransition((ViewGroup) fab.getParent());
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
                    fab.setLayoutParams(layoutParams);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length == 0 || grantResults.length == 0) {
            return;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera();
        }

        switch (requestCode) {
            case REQUST_CODE_CAMERA_AUDIO_STORAGE: {
                for (int i = 0; i < grantResults.length - 1; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "granted");
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "denied");
                    }
                }
                break;
            }
        }
    }

}
