package cn.ddy.richeditor;

import android.animation.LayoutTransition;
import android.animation.LayoutTransition.TransitionListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import java.util.ArrayList;
import java.util.List;

import cn.ddy.richeditor.utils.DialogIssueArticleDeleteUtils;
import cn.ddy.richeditormode.R;

/**
 * 这是一个富文本编辑器，给外部提供insertImage接口，添加的图片跟当前光标所在位置有关
 *
 * @author xmuSistone
 */
@SuppressLint({"NewApi", "InflateParams"})
public class RichTextEditor extends ObservableScrollView {
    private final String TAG = "RichTextEditor";
    private static final int EDIT_PADDING = 10; // EditText常规padding是10dp
    private int viewTagIndex = 1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
    private LinearLayout allLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
    private LayoutInflater inflater;
    private View.OnClickListener imgListener; // 图片监听
    private View.OnKeyListener keyListener; // 所有EditText的软键盘监听器
    private View.OnFocusChangeListener focusListener; // 所有EditText的焦点监听listener
    private EditText lastFocusEdit; // 最近被聚焦的EditText
    private RichTextEditorOnFocusChangeListener richTextEditorOnFocusChangeListener;//EditText获取焦点监听
    private RichTextEditorOnScrollChanged richTextEditorOnScrollChanged;//滑动监听

    private LayoutTransition mTransition; // 只在图片View添加或remove时，触发transition动画
    private int disappearingImageIndex = 0;
    private int lastEditIndex = 0;//EditText

    public RichTextEditor(Context context) {
        this(context, null);
    }

    public RichTextEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichTextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflater = LayoutInflater.from(context);

        // 1. 初始化allLayout
        allLayout = new LinearLayout(context);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        allLayout.setBackgroundColor(Color.WHITE);
        setupLayoutTransitions();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        addView(allLayout, layoutParams);
        //滑动监听收回键盘在滑动在最底部的时候会产生冲突， 这里我直接监听的是图片的onTouch事件 发现比较好用
//        setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
//            @Override
//            public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
//                if (richTextEditorOnScrollChanged != null)
//                    richTextEditorOnScrollChanged.OnScrollChanged();
//            }
//
//            @Override
//            public void onDownMotionEvent() {
//
//            }
//
//            @Override
//            public void onUpOrCancelMotionEvent(ScrollState scrollState) {
//
//            }
//        });
        // 2. 初始化键盘退格监听
        // 主要用来处理点击回删按钮时，view的一些列合并操作
        keyListener = new OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    EditText edit = (EditText) view;
                    onBackspacePress(edit);
                }
                return false;
            }
        };

        // 3. 图片监听
        imgListener = new OnClickListener() {
            @Override
            public void onClick(final View view) {
                DialogIssueArticleDeleteUtils dialogIssueArticleDeleteUtils = new DialogIssueArticleDeleteUtils();
                dialogIssueArticleDeleteUtils.initView(view.getContext(), new OnClickListener() {
                    @Override
                    public void onClick(View views) {
                        RelativeLayout parentView = (RelativeLayout) view.getParent();
                        onImageCloseClick(parentView);
                    }
                });
            }
        };

        //EditText 焦点监听
        focusListener = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    if (richTextEditorOnFocusChangeListener != null)
                        richTextEditorOnFocusChangeListener.onFocusChange(view, true);
                    lastFocusEdit = (EditText) view;
                }
            }
        };
//        //默认添加一个输入框
        LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        EditText firstEdit = createEditText("请输入文章内容...", dip2px(EDIT_PADDING));
        allLayout.addView(firstEdit, firstEditParam);
        lastFocusEdit = firstEdit;
        lastFocusEdit.requestFocus();
    }

    /**
     * 处理软键盘backSpace回退事件
     *
     * @param editTxt 光标所在的文本输入框
     */
    private void onBackspacePress(EditText editTxt) {
        int startSelection = editTxt.getSelectionStart();
        // 只有在光标已经顶到文本输入框的最前方，在判定是否删除之前的图片，或两个View合并
        if (startSelection == 0) {
            int editIndex = allLayout.indexOfChild(editTxt);
            View preView = allLayout.getChildAt(editIndex - 1); // 如果editIndex-1<0,
            // 则返回的是null
            if (null != preView) {
                if (preView instanceof RelativeLayout) {
                    // 光标EditText的上一个view对应的是图片
                    onImageCloseClick(preView);
                } else if (preView instanceof EditText) {
                    // 光标EditText的上一个view对应的还是文本框EditText
                    String str1 = editTxt.getText().toString();
                    EditText preEdit = (EditText) preView;
                    String str2 = preEdit.getText().toString();

                    // 合并文本view时，不需要transition动画
                    allLayout.setLayoutTransition(null);
                    allLayout.removeView(editTxt);
                    allLayout.setLayoutTransition(mTransition); // 恢复transition动画

                    // 文本合并
                    preEdit.setText(String.format("%s%s", str2, str1));
                    preEdit.requestFocus();
                    preEdit.setSelection(str2.length(), str2.length());
                    lastFocusEdit = preEdit;
                    Log.e(TAG, "删除后lastFocusEdit :" + lastFocusEdit);
                }
            }
        }
    }

    //图片删除 整个image对应的relativeLayout view
    private void onImageCloseClick(View view) {
        if (!mTransition.isRunning()) {
            disappearingImageIndex = allLayout.indexOfChild(view);
            allLayout.removeView(view);
        }
    }

    //生成文本输入框
    private EditText createEditText(String hint, int paddingTop) {
        EditText editText = (EditText) inflater.inflate(R.layout.richeditor_edit,
                null);
        editText.setOnKeyListener(keyListener);
        editText.setTag(viewTagIndex++);
        editText.setPadding(paddingTop, paddingTop, paddingTop, paddingTop);
        editText.setHint(hint);
        editText.setOnFocusChangeListener(focusListener);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getWordTotal();
            }
        });
        return editText;
    }

    //生成图片View
    private RelativeLayout createImageLayout() {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.richeditor_img, null);
        layout.setTag(viewTagIndex++);
        View closeView = layout.findViewById(R.id.edit_imageView);
        closeView.setTag(layout.getTag());
        closeView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return richTextEditorOnTouch != null && richTextEditorOnTouch.OnTouchListener();
            }
        });
        closeView.setOnClickListener(imgListener);
        return layout;
    }

    //根据绝对路径添加view
    public void insertImage(String imagePath) {
        Bitmap bmp = getScaledBitmap(imagePath, getWidth());
        insertImage(bmp, imagePath);
    }

    //插入一张图片
    private void insertImage(Bitmap bitmap, String imagePath) {
        hideKeyBoard();
        String lastEditStr = lastFocusEdit.getText().toString();
        int cursorIndex = lastFocusEdit.getSelectionStart();
        String editStr1 = lastEditStr.substring(0, cursorIndex).trim();
        lastEditIndex = allLayout.indexOfChild(lastFocusEdit);
        Log.e(TAG, "lastEditIndex:" + lastEditIndex);
        if (lastEditStr.length() == 0 || editStr1.length() == 0) {
            Log.e(TAG, "顶在了editText的最前面 :");
            // 如果EditText为空，或者光标已经顶在了editText的最前面，则在Editor后直接插入图片，并且在图片后在次插入EditText，并且将lastEditIndex定位在最后插入的EditText
            addImageViewAtIndex(lastEditIndex + 1, bitmap, imagePath, true);
        } else {   // 如果EditText非空且光标不在最顶端，则需要添加新的imageView和EditText
            Log.e(TAG, "光标不在最顶端 :");
            //如果插入连续图片 ，图片之间不插入EditText ，只在最后边插入一个EditText
            lastFocusEdit.setText(editStr1);
//            //截取的字符串
//            String editStr2 = lastEditStr.substring(cursorIndex).trim();
//            if (allLayout.getChildCount() - 1 == lastEditIndex || editStr2.length() > 0) {
//                addEditTextAtIndex(lastEditIndex + 1, editStr2);
//                lastFocusEdit.requestFocus();
//            }
//            addImageViewAtIndex(lastEditIndex + 1, bitmap, imagePath, false);
//            lastFocusEdit.setSelection(editStr1.length(), editStr1.length());

            //每插入一个图片 都在后边插入一个EditText
            addImageViewAtIndex(lastEditIndex + 1, bitmap, imagePath, false);
            //截取的字符串
            String editStr2 = lastEditStr.substring(cursorIndex).trim();
            addEditTextAtIndex(lastEditIndex + 2, editStr2);
            lastFocusEdit.setSelection(editStr1.length(), editStr1.length());
        }
    }

    /**
     * 在特定位置插入EditText
     *
     * @param index   位置
     * @param editStr EditText显示的文字
     */
    private void addEditTextAtIndex(final int index, String editStr) {
        EditText editText2 = createEditText("", getResources()
                .getDimensionPixelSize(R.dimen.edit_padding_top));
        editText2.setText(editStr);
        // 请注意此处，EditText添加、或删除不触动Transition动画
        allLayout.setLayoutTransition(null);
        allLayout.addView(editText2, index);
        allLayout.setLayoutTransition(mTransition); // remove之后恢复transition动画
    }

    //在特定位置插入EditText
    private void addEditTextAtIndex(int index) {
        EditText editText2 = createEditText("", getResources()
                .getDimensionPixelSize(R.dimen.edit_padding_top));
        // 请注意此处，EditText添加、或删除不触动Transition动画
        allLayout.setLayoutTransition(null);
        allLayout.addView(editText2, index);
        allLayout.setLayoutTransition(mTransition); // remove之后恢复transition动画
        lastFocusEdit = editText2;
        lastEditIndex = allLayout.indexOfChild(lastFocusEdit);
    }

    /**
     * 在特定位置添加ImageView
     *
     * @param index     位置
     * @param bmp       bmp
     * @param imagePath 图片路径
     */
    private void addImageViewAtIndex(final int index, Bitmap bmp,
                                     String imagePath, boolean isFirst) {
        final RelativeLayout imageLayout = createImageLayout();
        DataImageView imageView = imageLayout
                .findViewById(R.id.edit_imageView);
        imageView.setImageBitmap(bmp);
        imageView.setAbsolutePath(imagePath);
        // 调整imageView的高度
        int imageHeight = getWidth() * bmp.getHeight() / bmp.getWidth();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, imageHeight);
        imageView.setLayoutParams(lp);
        // onActivityResult无法触发动画，此处post处理  注意 在添加多图的时候如果用post 会出现两张图片挨着 中间没有添加EditText，  原因是因为延迟
        // allLayout.postDelayed(() -> {
        allLayout.addView(imageLayout, index);
        if (isFirst)
            addEditTextAtIndex(lastEditIndex + 2);
        //  }, 200);
    }


    /**
     * 隐藏小键盘
     */
    public void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(lastFocusEdit.getWindowToken(), 0);
    }

    public EditText getLastFocusEdit() {
        return lastFocusEdit;
    }

    /**
     * 根据view的宽度，动态缩放bitmap尺寸
     *
     * @param width view的宽度
     */
    private Bitmap getScaledBitmap(String filePath, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int sampleSize = options.outWidth > width ? options.outWidth / width
                + 1 : 1;
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 初始化transition动画
     */
    private void setupLayoutTransitions() {
        mTransition = new LayoutTransition();
        allLayout.setLayoutTransition(mTransition);
        mTransition.addTransitionListener(new TransitionListener() {

            @Override
            public void startTransition(LayoutTransition transition,
                                        ViewGroup container, View view, int transitionType) {

            }

            @Override
            public void endTransition(LayoutTransition transition,
                                      ViewGroup container, View view, int transitionType) {
//                if (!transition.isRunning()
//                        && transitionType == LayoutTransition.CHANGE_DISAPPEARING) {
//                    // transition动画结束，合并EditText
////					 mergeEditText();
//                }
            }
        });
        mTransition.setDuration(300);
    }

    //图片删除的时候，如果上下方都是EditText，则合并处理
    private void mergeEditText() {
        View preView = allLayout.getChildAt(disappearingImageIndex - 1);
        View nextView = allLayout.getChildAt(disappearingImageIndex);
        if (preView != null && preView instanceof EditText && null != nextView
                && nextView instanceof EditText) {
            Log.d("LeiTest", "合并EditText");
            EditText preEdit = (EditText) preView;
            EditText nextEdit = (EditText) nextView;
            String str1 = preEdit.getText().toString();
            String str2 = nextEdit.getText().toString();
            String mergeText;
            if (str2.length() > 0) {
                mergeText = str1 + "\n" + str2;
            } else {
                mergeText = str1;
            }

            allLayout.setLayoutTransition(null);
            allLayout.removeView(nextEdit);
            preEdit.setText(mergeText);
            preEdit.requestFocus();
            preEdit.setSelection(str1.length(), str1.length());
            allLayout.setLayoutTransition(mTransition);
        }
    }

    /**
     * dp和pixel转换
     *
     * @param dipValue dp值
     * @return 像素值
     */
    public int dip2px(float dipValue) {
        float m = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    /**
     * 对外提供的接口, 生成编辑数据上传
     */
    public List<EditData> buildEditData() {
        List<EditData> dataList = new ArrayList<>();
        int num = allLayout.getChildCount();
        for (int index = 0; index < num; index++) {
            View itemView = allLayout.getChildAt(index);
            EditData itemData = new EditData();
            if (itemView instanceof EditText) {
                EditText item = (EditText) itemView;
                itemData.inputStr = item.getText().toString();
            } else if (itemView instanceof RelativeLayout) {
                DataImageView item = (DataImageView) itemView
                        .findViewById(R.id.edit_imageView);
                itemData.imagePath = item.getAbsolutePath();
            }
            dataList.add(itemData);
        }
        return dataList;
    }

    private int count = 0;

    //获取字数
    public int getWordTotal() {
        count = 0;
        int num = allLayout.getChildCount();
        for (int index = 0; index < num; index++) {
            View itemView = allLayout.getChildAt(index);
            EditData itemData = new EditData();
            if (itemView instanceof EditText) {
                EditText item = (EditText) itemView;
                itemData.inputStr = item.getText().toString();
                if (!TextUtils.isEmpty(itemData.inputStr)) {
                    count = count + itemData.inputStr.length();
                }
            }
        }
        return count;
    }

    public class EditData {
        public String inputStr;
        public String imagePath;
    }


    public void setRichTextEditorOnFocusChangeListener(RichTextEditorOnFocusChangeListener richTextEditorOnFocusChangeListener) {
        this.richTextEditorOnFocusChangeListener = richTextEditorOnFocusChangeListener;
    }

    public void setRichTextEditorOnScrollChanged(RichTextEditorOnScrollChanged richTextEditorOnScrollChanged) {
        this.richTextEditorOnScrollChanged = richTextEditorOnScrollChanged;
    }

    public interface RichTextEditorOnScrollChanged {
        void OnScrollChanged();
    }

    public interface RichTextEditorOnFocusChangeListener {
        void onFocusChange(View v, boolean hasFocus);
    }

    RichTextEditorOnTouch richTextEditorOnTouch;

    public void setRichTextEditorOnTouch(RichTextEditorOnTouch richTextEditorOnTouch) {
        this.richTextEditorOnTouch = richTextEditorOnTouch;
    }

    public interface RichTextEditorOnTouch {
        boolean OnTouchListener();
    }

    public int getCount() {
        return count;
    }
}
