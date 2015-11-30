package me.next.clipboardlinkdetect;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ClipboardLinkDetect";

    private TextView tvUrl;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvUrl = (TextView) findViewById(R.id.tv_url);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String textStr = tvUrl.getText().toString().trim();
                if (checkUrl(textStr)) {
                    showUrlSnackBar(textStr);
                } else {
                    Snackbar.make(view, "未检测到 URL", Snackbar.LENGTH_LONG).show();
                }
            }
        });
        checkClipboard();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkClipboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkClipboard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private void checkClipboard() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null) {
                // ClipData { text/plain {T:http://m.shihuo.cn/tuangou#qk=entrance&order=3} }
                // ClipData { text/plain {T:好饿} }
                ClipData.Item item = clip.getItemAt(0);
//                Clipboard text : http://m.shihuo.cn/tuangou#qk=entrance&order=3
                final CharSequence text = item.getText();
                if (text != null) {
                    String clipboardText = text.toString();
                    if (checkUrl(clipboardText)) {
                        Log.d(TAG, "isHttpUrl true");
                        tvUrl.setText(clipboardText);
                        showUrlSnackBar(text);
                    } else {
                        tvUrl.setText(clipboardText);
                        Log.d(TAG, "isHttpUrl false");
                    }
                }
            } else {
                Log.d(TAG, "Clipboard text null");

            }
        }
    }

    private void showUrlSnackBar(final CharSequence text) {
        Snackbar.make(fab, "检测到网址：" + text, Snackbar.LENGTH_LONG)
                .setAction("查看网页标题", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new GetTitleTask().execute(text.toString());
                    }
                }).show();
    }

    private boolean checkUrl(String clipboardText) {
        return URLUtil.isHttpsUrl(clipboardText) ||
                URLUtil.isHttpUrl(clipboardText);
    }

    private class GetTitleTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                return TitleExtractor.getPageTitle(strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("")) {
                Snackbar.make(fab, "未解析到网页标题", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(fab, "网页标题是：「" + s+"」", Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
