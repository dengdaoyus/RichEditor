package cn.ddy.richeditor.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import cn.ddy.richeditormode.R;


/**
 * 发布文章图片删除dialog
 * Created by Administrator on 2017/10/31 0031.
 */

public class DialogIssueArticleDeleteUtils {


    TextView tv_delete;

    private View view;
    private Dialog dialog;

    private View.OnClickListener onClickListener;


    public void initView(Context context, View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        view = LayoutInflater.from(context).inflate(R.layout.dialog_issue_article_delete, null);
        tv_delete = view.findViewById(R.id.tv_delete);
        setContentView(context);
        setOnClickListener();
    }

    private void setContentView(Context context) {
        dialog = new Dialog(context, R.style.MyNewAlertDialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view);
        initWindow();
    }

    private void initWindow() {
        Window window = dialog.getWindow();
        assert window != null;
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        dialog.show();
    }

    private void setOnClickListener() {
        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) onClickListener.onClick(view);
                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                view = null;
            }
        });
    }

}
