package com.muzhi.camerasdk.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * 参数
 */
public class CameraSdkParameterInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public static final String EXTRA_PARAMETER="extra_camerasdk_parameter";
	
	public static final int TAKE_PICTURE_FROM_CAMERA = 100;
	public static final int TAKE_PICTURE_FROM_GALLERY = 200;
	public static final int TAKE_PICTURE_PREVIEW=300;
	

	//设置参数
	public boolean is_net_path=false;		//是否来自网络默认为否
	private int ret_type=0;					//返回类型(0:返回生成的图片路径 1:返回生成的Bitmap)    默认为0
	private int max_image = 9;				//最大图片选择数，int类型，默认9
	private boolean single_mode = false;	//图片选择模式，默认多选
	private boolean show_camera = true;		//是否显示相机，默认显示
	private int position = 0;				//在预览时带的下标，也可作为标识使用
	private boolean croper_image=false;		//正方形的裁剪图片必须与单张相结合
	private boolean filter_image=false;		//使用滤镜功能
	private boolean show_dialog=true;		//使用滤镜功能
	private String className;
	private int type=-1;
	private boolean isGif  = false;

	public boolean isGif() {
		return isGif;
	}

	public void setGif(boolean gif) {
		isGif = gif;
	}

	//返回的参数
	public static ArrayList<Bitmap> bitmap_list=new ArrayList<Bitmap>();	//静态保存返回的bitmap类型
	private ArrayList<String> image_list;	//已经选择的图片集路径	

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getRet_type() {
		return ret_type;
	}
	public void setRet_type(int ret_type) {
		this.ret_type = ret_type;
	}
	public int getMax_image() {
		return max_image;
	}
	public void setMax_image(int max_image) {
		this.max_image = max_image;
	}
	public boolean isSingle_mode() {
		return single_mode;
	}
	public void setSingle_mode(boolean single_mode) {
		this.single_mode = single_mode;
	}
	public boolean isShow_camera() {
		return show_camera;
	}
	public void setShow_camera(boolean show_camera) {
		this.show_camera = show_camera;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public boolean isCroper_image() {
		return croper_image;
	}
	public void setCroper_image(boolean croper_image) {
		this.croper_image = croper_image;
	}
	public boolean isFilter_image() {
		return filter_image;
	}
	public void setFilter_image(boolean filter_image) {
		this.filter_image = filter_image;
	}
	public boolean isShow_dialog() {
		return show_dialog;
	}
	public void setShow_dialog(boolean show_dialog) {
		this.show_dialog = show_dialog;
	}
	public ArrayList<String> getImage_list() {
		if(image_list==null){
			image_list=new ArrayList<String>();
		}
		return image_list;
	}
	
	public void setImage_list(ArrayList<String> image_list) {
		this.image_list = image_list;
	}
	

    
	
}
