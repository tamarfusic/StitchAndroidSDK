package com.fusit.stitchexample;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.fusit.stitchutils.StitchActivity;


public class MainActivity extends AppCompatActivity {
    public static final String CHARACTER_ID = "CHARACTER_ID";
    private static final int STITCH_REQ_CODE = 147;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStitchCalled(View view) {
        Intent intent = new Intent(MainActivity.this, StitchActivity.class);
        intent.putExtra(StitchActivity.CHARACTER_ID, "919364953797"); //horizontal
//        intent.putExtra(StitchActivity.CHARACTER_ID, "412364653798"); //vertical
        intent.putExtra(StitchActivity.SHOW_SHARE_SCREEN, true);
        intent.putExtra(StitchActivity.SHARE_TO_INSTAGRAM, true);
        intent.putExtra(StitchActivity.SHARE_TO_FACEBOOK, true);
        intent.putExtra(StitchActivity.SHARE_TO_MORE, true);
        intent.putExtra(StitchActivity.SHARE_TO_TIKTOK, false);
        intent.putExtra(StitchActivity.TIKTOK_SHARE_KEY, "aw6c5a71e2751ba4");
        startActivityForResult(intent, STITCH_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("tamar", "in onActivityResult requestCode="+requestCode+"   resultCode="+resultCode);
        if(requestCode==STITCH_REQ_CODE) {
            if (requestCode == RESULT_OK && data.hasExtra(StitchActivity.STITCHED_FILE_PATH)) {
                //do what you want with the stitched file
                Log.e("tamar", "stitched path=="+data.getStringExtra(StitchActivity.STITCHED_FILE_PATH));
            }
        }

    }
}
