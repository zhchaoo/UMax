package com.example.laravelchen.toutiao.allfragment;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.laravelchen.toutiao.ArticleContentShow;
import com.example.laravelchen.toutiao.Constant;
import com.example.laravelchen.toutiao.Interface.OnAdClickListener;
import com.example.laravelchen.toutiao.Interface.OnItemClickListener;
import com.example.laravelchen.toutiao.PathRandom;
import com.example.laravelchen.toutiao.R;
import com.example.laravelchen.toutiao.alladapter.RecommendAdapter;
import com.example.laravelchen.toutiao.allbean.RecommendBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class PageFragment extends Fragment {


    public static final String ARGS_PAGE = "args_page";
    private TextView tv;
    private static String PATH = new PathRandom().getHomePath();

    private List<RecommendBean> newList;
    private RecommendAdapter adapter;
    private RecyclerView rv;
    private RecommendBean mainAdapterBean;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View view;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    public static PageFragment newInstance() {
        PageFragment fragment = new PageFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_page, container, false);
        initView();
        return view;
    }

    public void initView() {
        newList = new ArrayList<RecommendBean>();
        adapter = new RecommendAdapter(newList, getContext());
        rv = (RecyclerView) view.findViewById(R.id.rv);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        swipeRefreshLayout.setRefreshing(true);
        createThread();
        seeRefresh();
    }

    //设置刷新
    public void seeRefresh() {
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        //监听刷新
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PATH = new PathRandom().getHomePath();
                createThread();
            }
        });
    }


    //创建进程
    public void createThread() {
        new Thread() {
            public void run() {
                postJson();
            }
        }.start();
    }

    //获取数据
    private void postJson() {
        //申明给服务端传递一个json串
        //创建一个OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建一个RequestBody(参数1：数据类型 参数2传递的json串)
        RequestBody requestBody = RequestBody.create(JSON, "video");
        //创建一个请求对象
        Request request = new Request.Builder()
                .url(PATH)
                .header("User-Agent", Constant.USER_AGENT_MOBILE)
                .post(requestBody)
                .build();
        //发送请求获取响应
        try {
            Response response = okHttpClient.newCall(request).execute();
            //判断请求是否成功
            if (response.isSuccessful()) {
                String content = response.body().string();
                Message msg = new Message();
                msg.obj = content;
                handler.sendMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //数据处理
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String str = msg.obj + "";
            try {
                JSONObject root = new JSONObject(str);
                JSONArray ary = root.getJSONArray("data");
                for (int i = 0; i < ary.length() - 1; i++) {
                    if (i == ary.length() - 4) {
                        // the 4th position is ad
                        mainAdapterBean = new RecommendBean();
                        mainAdapterBean.setTitle("新品发布会");
                        mainAdapterBean.setImg("http://image.uc.cn/s/uae/g/01/omelette/public/dist/tmp/001.jpg");
                        mainAdapterBean.setAdType(101);
                        newList.add(0, mainAdapterBean);
                    }

                    JSONObject root1 = ary.getJSONObject(i);
                    mainAdapterBean = new RecommendBean();
                    mainAdapterBean.setTitle(root1.optString("title"));
                    mainAdapterBean.setComment_couont(root1.optString("comments_count"));
                    mainAdapterBean.setSource(root1.optString("source"));
                    mainAdapterBean.setShare_url(root1.optString("share_url"));
                    mainAdapterBean.setGroup_id(root1.optString("source_url"));
                    JSONArray img_url = root1.optJSONArray("image_list");
                    System.out.println(PATH);
                    if (img_url != null && img_url.length() > 0) {
                        mainAdapterBean.setImg(img_url.getJSONObject(0).optString("url"));
                        mainAdapterBean.setImg2(img_url.getJSONObject(1).optString("url"));
                        mainAdapterBean.setImg3(img_url.getJSONObject(2).optString("url"));
                        mainAdapterBean.setHasimg(true);
                    } else {
                        mainAdapterBean.setHasimg(false);
                    }
                    newList.add(0, mainAdapterBean);
                }
                adapter.add(newList);
                //设置点击事件
                adapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Intent i = new Intent(getContext(), ArticleContentShow.class);
                        i.putExtra("share_url", "http://www.toutiao.com" + newList.get(position).getGroup_id());
                        startActivity(i);
                    }
                });
                adapter.setOnAdClickListener(new OnAdClickListener() {
                    @Override
                    public void onClick(View view, Drawable drawable) {
                        zoomImageFromThumb(view, drawable);
                    }
                });
                rv.setLayoutManager(new LinearLayoutManager(getContext()) {
                    @Override
                    public boolean canScrollVertically() {
                        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
                        return isScrollEnabled && super.canScrollVertically();
                    }
                });
                rv.setAdapter(adapter);
                swipeRefreshLayout.setRefreshing(false);
                // load ad for webview cache
                mAdContainer =
                        (View) view.getRootView().findViewById(R.id.ad_expand);
                mAdWebView = (WebView) mAdContainer.findViewById(R.id.ad_webview);
                mAdWebView.getSettings().setJavaScriptEnabled(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Hold a reference to the current animator, so that it can be canceled mid-way.
     */
    private Animator mCurrentAnimator;

    /**
     * The system "short" animation time duration, in milliseconds. This duration is ideal for
     * subtle animations or animations that occur very frequently.
     */
    private int mShortAnimationDuration = 1000;
    /**
     * disable scroll.
     */
    private boolean isScrollEnabled = true;
    /*
     * for ads
     */
    private WebView mAdWebView;
    private View mAdContainer;
    /**
     * get status bar height.
     *
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        java.lang.reflect.Field field = null;
        int x = 0;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
            return statusBarHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }
    /**
     * "Zooms" in a thumbnail view by assigning the high resolution image to a hidden "zoomed-in"
     * image view and animating its bounds to fit the entire activity content area. More
     * specifically:
     *
     * <ol>
     *   <li>Assign the high-res image to the hidden "zoomed-in" (expanded) image view.</li>
     *   <li>Calculate the starting and ending bounds for the expanded view.</li>
     *   <li>Animate each of four positioning/sizing properties (X, Y, SCALE_X, SCALE_Y)
     *       simultaneously, from the starting bounds to the ending bounds.</li>
     *   <li>Zoom back out by running the reverse animation on click.</li>
     * </ol>
     *
     * @param thumbView  The thumbnail view to zoom in.
     * @param drawable The high-resolution version of the image represented by the thumbnail.
     */
    private void zoomImageFromThumb(final View thumbView, Drawable drawable) {
        // If there's an animation in progress, cancel it immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }


        // Load the high-resolution "zoomed-in" image.
        mAdWebView.getSettings().setJavaScriptEnabled(true);
        mAdWebView.loadUrl("file:///android_asset/Umax.html");
        mAdWebView.setVisibility(View.VISIBLE);
        isScrollEnabled = false;

        ImageView adImage = (ImageView) mAdContainer.findViewById(R.id.ad_image);
        adImage.setImageDrawable(drawable);

        // Calculate the starting and ending bounds for the zoomed-in image. This step
        // involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point startOffset = new Point();
        final Point finalOffset = new Point();
        final int statusBarHeight = getStatusBarHeight(getContext());

        // The start bounds are the global visible rectangle of the thumbnail, and the
        // final bounds are the global visible rectangle of the container view. Also
        // set the container view's offset as the origin for the bounds, since that's
        // the origin for the positioning animation properties (X, Y).
        thumbView.findViewById(R.id.ad_thumb).getGlobalVisibleRect(startBounds, startOffset);
        view.getRootView().getGlobalVisibleRect(finalBounds, finalOffset);
        startBounds.offset(-finalOffset.x, -finalOffset.y);
        finalBounds.offset(-finalOffset.x, -finalOffset.y);
        startOffset.y -= statusBarHeight;

        // Adjust the start bounds to be the same aspect ratio as the final bounds using the
        // "center crop" technique. This prevents undesirable stretching during the animation.
        // Also calculate the start scaling factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation begins,
        // it will position the zoomed-in view in the place of the thumbnail.
        thumbView.setAlpha(0f);
        mAdContainer.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the top-left corner of
        // the zoomed-in view (the default is the center of the view).
        mAdContainer.setPivotX(0f);
        mAdContainer.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and scale properties
        // (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mAdContainer, View.X, startOffset.x,
                        finalOffset.x))
                .with(ObjectAnimator.ofFloat(mAdContainer, View.Y, startOffset.y,
                        finalOffset.y))
                .with(ObjectAnimator.ofFloat(mAdContainer, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(mAdContainer, View.SCALE_Y, startScale, 1f))
                .with(ObjectAnimator.ofFloat(mAdWebView, View.ALPHA, 0, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down to the original bounds
        // and show the thumbnail instead of the expanded image.
        final float startScaleFinal = startScale;
        adImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel, back to their
                // original values.
                AnimatorSet set = new AnimatorSet();
                set
                        .play(ObjectAnimator.ofFloat(mAdContainer, View.X, startOffset.x))
                        .with(ObjectAnimator.ofFloat(mAdContainer, View.Y, startOffset.y))
                        .with(ObjectAnimator
                                .ofFloat(mAdContainer, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(mAdContainer, View.SCALE_Y, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(mAdWebView, View.ALPHA, 1f, 0));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        mAdContainer.setVisibility(View.GONE);
                        mAdWebView.setVisibility(View.INVISIBLE);
                        mAdWebView.loadUrl("about:blank");
                        isScrollEnabled = true;
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        mAdContainer.setVisibility(View.GONE);
                        mAdWebView.setVisibility(View.INVISIBLE);
                        mAdWebView.loadUrl("about:blank");
                        isScrollEnabled = true;
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
