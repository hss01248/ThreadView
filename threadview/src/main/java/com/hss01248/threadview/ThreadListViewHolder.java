package com.hss01248.threadview;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

public class ThreadListViewHolder {

    public View root;
    ListView listView;
    BaseAdapter adapter;
    List<WeakReference<Thread>> list;
    TextView textView;


    public ThreadListViewHolder(Context context){
        root = View.inflate(context,R.layout.threadview_list,null);
        listView = root.findViewById(R.id.listview);
        initListView(listView);
        textView = root.findViewById(R.id.tv_refresh);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
                String text = "refresh("+adapter.getCount()+")->\nalive:"+ThreadHookUtil.alive+",dead:"+ThreadHookUtil.terminated;
                textView.setText("refresh"+text);
            }
        });
        String text = "refresh("+adapter.getCount()+")->\nalive:"+ThreadHookUtil.alive+",dead:"+ThreadHookUtil.terminated;
        textView.setText("refresh"+text);

    }

    private void initListView(ListView listView) {
        list = ThreadHookUtil.getAll();
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int i) {
                return list.get(i);
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(view == null){
                    view = View.inflate(viewGroup.getContext(),R.layout.thread_list_item,null);
                }
                TextView name = view.findViewById(R.id.tv_name);
                TextView state = view.findViewById(R.id.tv_state);
                if(list.get(i) == null || list.get(i).get() == null){
                    state.setText("dead");
                    name.setText("xx");
                    view.setOnClickListener(null);
                    return view;
                }
                state.setText(list.get(i).get().getState().name());
                name.setText(list.get(i).get().getName());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDetail(list.get(i).get(),viewGroup.getContext());
                    }
                });
                return view;
            }
        };
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void showDetail(Thread thread,Context context) {
        Dialog dialog = new Dialog(context);
        ScrollView scrollView = new ScrollView(context);

        TextView textView = new TextView(context);
        textView.setPadding(30,30,30,30);
        String str = formatThreadInfo(thread);
        textView.setTextSize(13);
        textView.setText(str);
        scrollView.addView(textView);

        dialog.setContentView(scrollView);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = context.getResources().getDisplayMetrics().widthPixels;
                dialog.getWindow().setAttributes(params);
            }
        });
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.show();
    }

    private String formatThreadInfo(Thread thread) {
        StringBuilder sb = new StringBuilder(thread.getName()).append(":\n\n");
        sb.append(thread.getState().name());
        sb.append("\n");
        sb.append("\n");
        sb.append("current stack:\n".toUpperCase());
        sb.append(formatStacks(thread.getStackTrace()));
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("start() stack:\n".toUpperCase());
        sb.append(formatStacks(ThreadHookUtil.map.get(thread)));
        sb.append("\n");
        sb.append("\n");
        sb.append(thread.getThreadGroup());
        sb.append("\n");
        sb.append("\n");

        return sb.toString();
    }

    private String formatStacks(StackTraceElement[] stackTrace) {
        String s = Arrays.toString(stackTrace);
        s = s.replace(",","\n");
        return s;
    }

    void refresh(){
        list.clear();
        list.addAll(ThreadHookUtil.getAll());
        adapter.notifyDataSetChanged();
    }
}
