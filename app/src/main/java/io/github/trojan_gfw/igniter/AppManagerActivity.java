package io.github.trojan_gfw.igniter;

import android.animation.Animator;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AppManagerActivity extends AppCompatActivity {

    private View loadingView;
    private RecyclerView appListView;
    private FastScroller fastScroller;
    private AppManagerAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        loadingView = findViewById(R.id.loading);
        appListView = (RecyclerView)findViewById(R.id.list);
        appListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        appListView.setItemAnimator(new DefaultItemAnimator());
        fastScroller = (FastScroller)findViewById(R.id.fastscroller);

        Observable.create(o -> {
            adapter = new AppManagerAdapter();
            o.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        n -> {},                             //onNext
                        e -> {LogHelper.e("shit happen",e.getMessage());},                //onError
                        ()-> switchToAppItemsView());      //onComplete
    }

    private void switchToAppItemsView(){
        appListView.setAdapter(adapter);
        fastScroller.setRecyclerView(appListView);
        long shortAnimTime = 1;
        appListView.setAlpha(0);
        appListView.setVisibility(View.VISIBLE);
        appListView.animate().alpha(1).setDuration(shortAnimTime);
        loadingView.animate().alpha(0).setDuration(shortAnimTime).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {

                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
    }
}

class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ImageView icon = (ImageView)itemView.findViewById(R.id.itemicon);
    private Switch check = (Switch)itemView.findViewById(R.id.itemcheck);
    private AppInfo item;
    private Boolean proxied = false;

    AppViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
    }

    void bind(AppInfo app) {
        this.item = app;
        proxied = AppProxyManager.Instance.isAppProxy(app.getPkgName());
        icon.setImageDrawable(app.getAppIcon());
        check.setText(app.getAppLabel());
        check.setChecked(proxied);
    }

    @Override
    public void onClick(View view) {
        if (proxied) {
            AppProxyManager.Instance.removeProxyApp(item.getPkgName());
            check.setChecked(false);
        } else {
            AppProxyManager.Instance.addProxyApp(item.getPkgName());
            check.setChecked(true);
        }
        proxied = !proxied;
    }
}

class AppManagerAdapter extends RecyclerView.Adapter<AppViewHolder> implements SectionTitleProvider {


    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_apps_item, parent, false));
    }

    @Override
    public void onBindViewHolder(AppViewHolder holder, int position) {
        AppInfo appInfo = AppProxyManager.Instance.mlistAppInfo.get(position);
        holder.bind(appInfo);
    }

    @Override
    public int getItemCount() {
        return AppProxyManager.Instance.mlistAppInfo.size();
    }

    @Override
    public String getSectionTitle(int position) {
        AppInfo appInfo = AppProxyManager.Instance.mlistAppInfo.get(position);
        return appInfo.getAppLabel();
    }
}
