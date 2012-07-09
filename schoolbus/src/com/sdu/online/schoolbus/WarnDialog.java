package com.sdu.online.schoolbus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WarnDialog extends Activity {
	Button cancleButton,downloadButton;
	TextView tv;
	String url;
	//1为db,2为apk
	int type;
	private static final String TAG = WarnDialog.class.getSimpleName();
	private static final String STORAGE_PATH=Environment.getDownloadCacheDirectory().toString();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.warn);
		
		cancleButton = (Button) findViewById(R.id.warn_button_cancle);
		downloadButton = (Button)findViewById(R.id.warn_button_download);
		tv = (TextView)findViewById(R.id.warn_textView);
		Listener l = new Listener();
		cancleButton.setOnClickListener(l);
		downloadButton.setOnClickListener(l);
		init();
	}
	
	private void init(){
		Intent intent = getIntent();
		String text = intent.getStringExtra("text");
		url = intent.getStringExtra("url");
		type = intent.getIntExtra("type", 0);
		text = text.replaceAll("<br/>", "\n");
		tv.setText(text);
		Log.v(TAG, text);
	}
    public  File getFileFromServer(String path,ProgressDialog pd)throws Exception{
    	if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
    		URL url=new URL(path);
    		String fileName=path.substring(path.lastIndexOf("/")+1);
    		HttpURLConnection conn=(HttpURLConnection)url.openConnection();
    		conn.setConnectTimeout(5000);
    		int filesize=conn.getContentLength();
    		Log.v(TAG,filesize+"");
    		pd.setMax(filesize);
    		InputStream is=conn.getInputStream();
    		File file=new File(STORAGE_PATH,fileName);
    		FileOutputStream fos=new FileOutputStream(file);
    		BufferedInputStream bis=new BufferedInputStream(is);
    		int length;
    		int total=0;
    		byte[] buffer=new byte[1024];
    		while((length=bis.read(buffer))!=-1){
    			fos.write(buffer, 0, length);
    			fos.flush();
    			total+=length;
    			pd.setProgress(total);
    			
    		}
    		fos.close();
    		bis.close();
    		is.close();
    		return file;
    	}
    	
    	
		return null;
    	
    }
    protected void downloadApk(String urlpath) {
    	final String path=urlpath;
		final ProgressDialog pd;
		pd=new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载更新。。。");
		pd.show();
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
					File file=getFileFromServer(path, pd);
					sleep(3000);
					installApk(file);
					pd.dismiss();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}.start();
	}
    protected void installApk(File file){
    	Intent intent=new Intent();
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
    	startActivity(intent);
    }
	class Listener implements OnClickListener{
		public void onClick(View v) {
			if(v == cancleButton){
				WarnDialog.this.finish();
			}else if(v == downloadButton){			
				Intent intent=new Intent();
				intent.setClass(WarnDialog.this, UpdateActivity.class);
				intent.putExtra("url", "http://192.168.1.108:8080/mp3/schoolbus.apk");
				intent.putExtra("type", type);
				startActivity(intent);
				WarnDialog.this.finish();
				
			}
		}
	}
}
