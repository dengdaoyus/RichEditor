# RichEditor
富文本编辑器

![效果图](https://github.com/dengdaoyus/RichEditor/blob/master/app/img/img%20(1).png?raw=true)  
![效果图](https://github.com/dengdaoyus/RichEditor/blob/master/app/img/img%20(3).png?raw=true)
![效果图](https://github.com/dengdaoyus/RichEditor/blob/master/app/img/img%20(4).png?raw=true)

###使用方法：

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode != -1) {
            if (data != null) {
                CameraSdkParameterInfo mCameraSdkParameterInfo = (CameraSdkParameterInfo) data.getExtras().getSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER);
                for (int i = 0; i <mCameraSdkParameterInfo.getImage_list().size() ; i++) {
                    richTextEditor.insertImage(mCameraSdkParameterInfo.getImage_list().get(i));
                }
            }
        }
    }
