package com.p2po2p.tcpdump;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/*
 * 
 *分享utils
 *
 */
public class ShareUtil {
	private static final boolean debug = true;
	private static byte[] mByte = new byte[0];

	private static ShareUtil mSareUtil;
	static Context mContext;

	private ShareUtil(Context context) {
		super();
		mContext = context;
	}

	public static ShareUtil getInstance(Context context) {
		synchronized (mByte) {
			if (mSareUtil == null) {
				mSareUtil = new ShareUtil(context);
			}
			return mSareUtil;
		}
	}

	/**
	 * 调用已安装app 分享一个附件
	 *
	 * @param text
	 *            内容
	 * @param resourceAddr
	 *            附件url
	 */

	public static void shareSingle(Context context, String text, String resourceAddr) {
		if (null == resourceAddr) {
			return;
		}
		Log.i("h02659",resourceAddr);
		Intent tent = new Intent(Intent.ACTION_SEND);
		File file = new File(resourceAddr);
		tent.setType(getMimeType(file));//设置发送类型
		tent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		tent.putExtra(Intent.EXTRA_TEXT, text); // 内容
		context.startActivity(Intent.createChooser(tent, context.getString(R.string.sharing_title)));
	}

	/**
	 * 调用已安装app，分享多个附件
	 *
	 * @param text
	 *            内容
	 * @param resourcesAddr
	 *            附件url
	 */

	public static void shareMutiple(Context context, String text, String[] resourcesAddr) {
		if (null == resourcesAddr) {
			return;
		}
		Intent tent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		ArrayList<Uri> uris = new ArrayList<Uri>();
		for (int i = 0; i < resourcesAddr.length; i++) {
			File file = new File(resourcesAddr[i]);
			uris.add(Uri.fromFile(file));
			tent.setType(getMimeType(file));//设置发送类型
		}
		tent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		tent.putExtra(Intent.EXTRA_TEXT, text); // 内容
		context.startActivity(Intent.createChooser(tent, context.getString(R.string.sharing_title)));
	}

	public static void shareTxt(Context context, String title, String text) {
		Intent tent=new Intent(Intent.ACTION_SEND_MULTIPLE);
		tent.putExtra(Intent.EXTRA_SUBJECT, title); // 主题
		tent.putExtra(Intent.EXTRA_TEXT, text); // 内容
		tent.setType("text/plain");
		context.startActivity(Intent.createChooser(tent, context.getString(R.string.sharing_title)));
	}


	/**
	 * 转发邮箱,带一个附件
	 *
	 * @param title
	 *            标题
	 * @param text
	 *            内容
	 * @param address
	 *            收件人地址
	 * @param url 附件地址
	 */
	public void shareByEmailOne(String title, String text, String[] address,
                                String url) {
		Intent tent = new Intent(Intent.ACTION_SEND); // 调用系统邮箱,只能添加一个附件
		if (url != null) {
			File file = new File(url);
			Uri uri = Uri.fromFile(file);
			tent.putExtra(Intent.EXTRA_STREAM, uri);
		}
		tent.putExtra(Intent.EXTRA_SUBJECT, title); // 主题
		tent.putExtra(Intent.EXTRA_TEXT, text); // 内容
		tent.putExtra(Intent.EXTRA_EMAIL, address); // 收件人地址
		tent.setType("message/rfc882"); // 发送类型
		mContext.startActivity(Intent.createChooser(tent, mContext.getString(R.string.sharing_title)));
	}

	/**
	 * 转发邮箱，带多个附件
	 *
	 * @param title 标题
	 * @param text 内容
	 * @param address
	 *            收件人地址
	 * @param str
	 *            附件地址数组
	 */
	public static void shareByEmail(String title, String text,
                                    String[] address, String[] str) {
		Intent tent = new Intent(Intent.ACTION_SEND_MULTIPLE); // 调用系统邮箱,可添加附件
		ArrayList<Uri> url = new ArrayList<Uri>();
		if (str != null) {
			for (int i = 0; i < str.length; i++) {
				File file = new File(str[i]);
				url.add(Uri.fromFile(file));
			}
			tent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, url);
		}
		tent.putExtra(Intent.EXTRA_SUBJECT, title); // 主题
		tent.putExtra(Intent.EXTRA_TEXT, text); // 内容
		tent.putExtra(Intent.EXTRA_EMAIL, address); // 收件人地址
		tent.setType("message/rfc882"); // 发送类型
		// tent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(Intent.createChooser(tent, mContext.getString(R.string.sharing_title)));
	}

	public void sendLogByEmail(String title, String text,
                               String[] address, String[] str, String chooserTitle) {
		if (null == str) {
			return;
		}
		Intent tent = new Intent(Intent.ACTION_SEND_MULTIPLE); // 调用系统邮箱,可添加附件
		ArrayList<Uri> url = new ArrayList<Uri>();
		for (int i = 0; i < str.length; i++) {
			File file = new File(str[i]);
			url.add(Uri.fromFile(file));
			tent.setType(getMimeType(file));//设置发送类型

		}
		tent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, url);
		tent.putExtra(Intent.EXTRA_SUBJECT, title); // 主题
		tent.putExtra(Intent.EXTRA_TEXT, text); // 内容
		tent.putExtra(Intent.EXTRA_EMAIL, address); // 收件人地址
		mContext.startActivity(Intent.createChooser(tent, chooserTitle));
	}

	/**
	 * 短信分享本地图片
	 *
	 * @param number
	 *            电话号码
	 * @param text
	 *            短信内容
	 * @param uri
	 *            附件地址    短信只能添加一个附件
	 */
	public void shareBySMS(String number, String text, String uri) {
		Intent tent = new Intent(Intent.ACTION_SEND);

		File file = new File(uri); // 图片地址
		Uri url = Uri.fromFile(file);
		if (url != null) {
			tent.putExtra(Intent.EXTRA_STREAM, url); // 添加图片
		}

		tent.setType("image/png");
		tent.putExtra("address",number);
		tent.putExtra("sms_body", text);// 短信内容	
		mContext.startActivity(tent);
	}

	/**
	 * 短信分享本地图片,分享多个
	 *
	 * @param number
	 *            电话号码
	 * @param text
	 *            短信内容
	 * @param strurl
	 *            附件地址数组   
	 */
	public void shareBySMSMore(String number, String text, String[] strurl) {
//		Uri smsToUri = Uri.parse("smsto:123654");
		Intent tent = new Intent(Intent.ACTION_SEND_MULTIPLE, Uri.parse("mmsto"));

		ArrayList<Uri> url = new ArrayList<Uri>();
		if (strurl != null) {
			for (int i = 0; i < strurl.length; i++) {
				File file = new File(strurl[i]);
				url.add(Uri.fromFile(file));
			}
			tent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, url);
		}

		tent.setType("image/png");
		tent.putExtra("address",number);
		tent.putExtra("sms_body", text);// 短信内容	
//		mContext.startActivity(tent);
		mContext.startActivity(Intent.createChooser(tent, "MMS:"));
	}



	// 分享到主流社交工具
	public static void shareBySystem() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.sharing_title));
		intent.putExtra(Intent.EXTRA_TEXT, "终于可以了!!!");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	}


	/**
	 * 获得文件类型MimeType
	 * @param file
	 * @return
	 */
	public static String getMimeType(File file){
		String suffix = getSuffix(file);
		if (suffix == null) {
			return "file/*";
		}
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
		if (type != null && !type.isEmpty()) {
			return type;
		}
		return "file/*";
	}

	/**
	 * 获得文件后缀
	 * @param file
	 * @return
	 */
	private static String getSuffix(File file) {
		if (file == null || !file.exists() || file.isDirectory()) {
			return null;
		}
		String fileName = file.getName();
		if (fileName.equals("") || fileName.endsWith(".")) {
			return null;
		}
		int index = fileName.lastIndexOf(".");
		if (index != -1) {
			return fileName.substring(index + 1).toLowerCase(Locale.US);
		} else {
			return null;
		}
	}


}
