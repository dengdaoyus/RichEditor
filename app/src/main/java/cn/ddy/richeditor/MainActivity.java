package cn.ddy.richeditor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.muzhi.camerasdk.model.CameraSdkParameterInfo;

public class MainActivity extends AppCompatActivity {
    private ImageView ivAdd;
    private RichTextEditor richTextEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setOnClick();
    }

    private void initView() {
        ivAdd = (ImageView) findViewById(R.id.iv_pic);
        richTextEditor = (RichTextEditor) findViewById(R.id.richTextEditor);
        richTextEditor.setRichTextEditorOnTouch(new RichTextEditor.RichTextEditorOnTouch() {
            @Override
            public boolean OnTouchListener() {
                return false;
            }
        });
    }

    private void setOnClick() {
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraSdkParameterInfo mCameraSdkParameterInfo=new CameraSdkParameterInfo();
                mCameraSdkParameterInfo.setMax_image(9);
                Intent intent = new Intent();
                intent.setClassName(getApplication(), "com.muzhi.camerasdk.PhotoPickActivity");
                Bundle b=new Bundle();
                b.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, mCameraSdkParameterInfo);
                intent.putExtras(b);
                startActivityForResult(intent, CameraSdkParameterInfo.TAKE_PICTURE_FROM_GALLERY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode != -1) {
            if (data != null) {
                //将返回的数据直接插入就可以了
                //当然在插入的时候 可以先将图片压缩一下，这里我推荐使用luban
                CameraSdkParameterInfo mCameraSdkParameterInfo = (CameraSdkParameterInfo) data.getExtras().getSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER);
                for (int i = 0; i <mCameraSdkParameterInfo.getImage_list().size() ; i++) {
                    richTextEditor.insertImage(mCameraSdkParameterInfo.getImage_list().get(i));
                }
            }
        }
    }
}
