package com.zxj.moblielogcat.weight;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zxj.moblielogcat.HandleLogCatService;
import com.zxj.moblielogcat.R;
import com.zxj.moblielogcat.utils.WindowUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxj on 2019-10-09.
 */
public class LogcatView extends FrameLayout implements Handler.Callback {

    private String title = "Console";

    private static final int WHAT_NEXT_LOG = 778;
    private static final int WHAT_STATUS_LOG = 779;

    private TextView tvLog;

    private TextView tvStatus;

    private RadioGroup rgGrade;

    private TextView tvTitle;

    private List<String> contentList = new ArrayList<>();

    /*是否自动拉取到最底部*/
    private boolean isAutoFullScroll = true;

    private String searchContent = "";
    private String noContentHint = "没有log信息";
    private String searchHint = "正在获取log信息";

    /*显示级别，0 所有，1 系统，2 警告,3 错误*/
    private int showGrade = 0;

    private boolean isRuining = true;
    private ImageView ivDown;
    private SearchEditText etContent;
    public String searchTag = "";//过滤tag

    private Context mContext;
    private WindowManager windowManager;
    private int statusBarHeight;
    private float preX, preY, x, y;

    private float y1 = 0;
    private float y2 = 0;


    private Handler mHandler;

    public static class MyHandler extends Handler {

        WeakReference<LogcatView> weakReference;

        private MyHandler(LogcatView adapter) {
            weakReference = new WeakReference<>(adapter);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (null == weakReference || null == weakReference.get()) {
                return;
            }
            weakReference.get().handleMessage(msg);
        }
    }


    @Override
    public boolean handleMessage(@NonNull Message msg) {

        switch (msg.what) {
            case WHAT_NEXT_LOG:
                String line = (String) msg.obj;
                if (!TextUtils.isEmpty(searchTag)) {
                    if (line.contains(searchTag)) {
                        if (!TextUtils.isEmpty(searchContent)) {
                            if (line.contains(searchContent)) {//同时搜索
                                contentList.add(line);
                                append(line);
                            }
                        } else {
                            contentList.add(line);//只搜索tag
                            append(line);
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(searchContent)) {
                        if (line.contains(searchContent)) {//只搜索内容
                            contentList.add(line);
                            append(line);
                        }
                    } else {
                        contentList.add(line);//所有
                        append(line);
                    }
                }
                break;
            case WHAT_STATUS_LOG:
                if (contentList.size() > 0) {
                    tvStatus.setVisibility(View.GONE);
                    tvLog.setVisibility(View.VISIBLE);
                    if (isAutoFullScroll) {
                        refreshLogView();
                    }
                } else {
                    tvStatus.setText(noContentHint);
                    tvStatus.setVisibility(View.VISIBLE);
                    tvLog.setVisibility(View.GONE);
                }
                break;
        }


        return false;
    }


    public LogcatView(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    public LogcatView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {

        mHandler = new MyHandler(this);


        initView();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Process logcatProcess = null;
                BufferedReader bufferedReader = null;
                StringBuilder log = new StringBuilder();
                String line;
                try {
                    while (isRuining) {
                        logcatProcess = Runtime.getRuntime().exec("logcat");
                        bufferedReader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));
                        while ((line = bufferedReader.readLine()) != null) {
                            log.append(line);
                            Message message = mHandler.obtainMessage();
                            message.what = WHAT_NEXT_LOG;
                            message.obj = line;
                            mHandler.sendMessage(message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 设置搜索的内容(默认没有)
     *
     * @param searchContent
     */
    public void setSearchContent(String searchContent) {
        this.searchContent = searchContent;
    }

    /**
     * 设置目标tag（默认没有）
     *
     * @param searchTag
     */
    public void setSearchTag(String searchTag) {
        this.searchTag = searchTag;
    }

    /**
     * 设置显示级别
     *
     * @param showGrade 0 所有，1 系统，2 警告,3 错误
     */
    public void setShowGrade(int showGrade) {
        this.showGrade = showGrade;
    }

    /**
     * 设置是否显示级别过滤
     *
     * @param isShowGrade
     */
    public void setShowGrade(boolean isShowGrade) {
        if (isShowGrade) {
            rgGrade.setVisibility(View.VISIBLE);
        } else {
            rgGrade.setVisibility(View.GONE);
        }
    }

    /**
     * 追加内容
     *
     * @param line
     */
    private void append(String line) {
        if (showGrade == 0 || showGrade == 1) {
            if (line.contains(" E ")) {
                tvLog.append("\n\n");
                showError(line);
            } else if (line.contains(" W ")) {
                tvLog.append("\n\n");
                showWarning(line);
            } else {
                tvLog.append("\n\n" + line);
            }
        } else if (showGrade == 2) {
            if (line.contains(" W ")) {
                tvLog.append("\n\n");
                showWarning(line);
            }
        } else if (showGrade == 3) {
            if (line.contains(" E ")) {
                tvLog.append("\n\n");
                showError(line);
            }
        }
        mHandler.sendEmptyMessageDelayed(WHAT_STATUS_LOG, 1000);

    }

    /**
     * 显示警告级别的日志
     *
     * @param line
     */
    private void showWarning(String line) {
        showLine(line, "#ba8a27");
    }

    /**
     * 显示错误级别的信息
     *
     * @param line
     */
    private void showError(String line) {
        showLine(line, "red");
    }

    private void showLine(String line, String color) {
        if (line.contains("http://") || line.contains("https://")) {
            String url = line.substring(line.indexOf("http"));
            tvLog.append(Html.fromHtml("<font color='" + color + "'>" + line.substring(0, line.indexOf("http")) + "</font>"));
            tvLog.append(Html.fromHtml("<a href='" + url + "'>" + url + "</a>"));
        } else {
            tvLog.append(Html.fromHtml("<font color='" + color + "'>" + line + "</font>"));
        }
    }

    /**
     * 关闭任务
     */
    public void closeTask() {
        isRuining = false;
    }


    void refreshLogView() {
        int offset = tvLog.getLineCount() * tvLog.getLineHeight();
        if (offset > tvLog.getHeight()) {
            tvLog.scrollTo(0, offset - tvLog.getHeight());
        }
    }


    private void initView() {

        statusBarHeight = WindowUtil.statusBarHeight;
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        //a view inflate itself, that's funny
        inflate(mContext, R.layout.logcat_dialog, this);


        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(title);
        //日志级别
        rgGrade = findViewById(R.id.rg_grade);
        rgGrade.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.rb_all) {
                    showGrade = 0;
                    searchContent("");
                } else if (checkedId == R.id.rb_system_out) {
                    showGrade = 1;
                    searchContent("System.out");
                } else if (checkedId == R.id.rb_warming) {
                    showGrade = 2;
                    searchContent("");
                } else if (checkedId == R.id.rb_error) {
                    showGrade = 3;
                    searchContent("");
                }
            }
        });
        tvStatus = findViewById(R.id.tv_status);
        etContent = findViewById(R.id.et_content);
        etContent.setOnClickOkListener(new SearchEditText.OnClickOkListener() {
            @Override
            public void onOk(String content) {
                tvStatus.setText(searchHint);
                tvStatus.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "正在搜索[" + content + "]", Toast.LENGTH_SHORT).show();
                searchContent(content);
            }
        });
        tvLog = findViewById(R.id.tv_consol);
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvLog.setMovementMethod(LinkMovementMethod.getInstance());
        tvLog.setOnTouchListener(TvOnTouchListener);

        findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.stopService(new Intent(mContext, HandleLogCatService.class));
            }
        });


        ivDown = findViewById(R.id.iv_down);
        ivDown.setVisibility(View.GONE);
        ivDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAutoFullScroll = true;
                ivDown.setVisibility(View.GONE);
            }
        });

        setDefault();

    }

    void setDefault() {
        if (!TextUtils.isEmpty(searchContent)) {
            etContent.setText(searchContent);
            searchContent(searchContent);
        }
        switch (showGrade) {
            case 0:
                rgGrade.check(R.id.rb_all);
                break;
            case 1:
                rgGrade.check(R.id.rb_system_out);
                break;
            case 2:
                rgGrade.check(R.id.rb_warming);
                break;
            case 3:
                rgGrade.check(R.id.rb_error);
                break;

        }
    }


    /**
     * 设置title
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }


    /**
     * 搜索内容
     *
     * @param content
     */
    private void searchContent(String content) {
        searchContent = content;
        tvLog.setText("--------------search info------------\n");
        for (String item : contentList) {
            if (item.contains(content)) {
                append(item);
            }
        }

    }

    View.OnTouchListener TvOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //继承了Activity的onTouchEvent方法，直接监听点击事件
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //当手指按下的时候
                y1 = event.getY();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                //当手指离开的时候
                y2 = event.getY();
                if (y2 - y1 > 50) {
                    isAutoFullScroll = false;
                    ivDown.setVisibility(View.VISIBLE);
                }
            }
            return false;
        }
    };


    // 移动待优化
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = event.getRawX();
                preY = event.getRawY() - statusBarHeight;
                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getRawX();
                y = event.getRawY() - statusBarHeight;
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();
                params.x += x - preX;
                params.y += y - preY;
                windowManager.updateViewLayout(this, params);
                preX = x;
                preY = y;
                return true;
            default:
                break;

        }
        return super.onTouchEvent(event);
    }


}
