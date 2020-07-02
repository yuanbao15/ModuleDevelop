package com.example.uhf.activity;


import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.uhf.fragment.UHFKillFragment;
import com.example.uhf.fragment.UHFLockFragment;
import com.example.uhf.fragment.UHFReadFragment;
import com.example.uhf.fragment.UHFReadTagFragment;
import com.example.uhf.fragment.UHFSetFragment;
import com.example.uhf.fragment.UHFWriteFragment;
import com.rfid.trans.StringUtility;
import com.yuanbao.moduleuhf_i6310.R;

import java.util.HashMap;

/**
 * UHF使用demo
 * 
 * 1、使用前请确认您的机器已安装此模块。 
 * 2、要正常使用模块需要在\libs\armeabi\目录放置libDeviceAPI.so文件，同时在\libs\目录下放置DeviceAPIver20160728.jar文件。 
 * 3、在操作设备前需要调用 init()打开设备，使用完后调用 free() 关闭设备
 * 
 * 
 * 更多函数的使用方法请查看API说明文档
 * 
 * @author
 * 更新于 2016年8月9日
 */
public class UHFMainActivity extends BaseTabFragmentActivity {

	private final static String TAG = "MainActivity";
//	public AppContext appContext;// ȫ��Context
//
	// public Reader mReader;
//	public RFIDWithUHF mReader;

//	public void playSound(int id) {
//		appContext.playSound(id);
//	}
	/*public void set53CGPIOEnabled(boolean enable){
		FileOutputStream f = null;
		FileOutputStream f1 = null;
		try{
			f = new FileOutputStream("/sys/devices/soc/soc:sectrl/ugp_ctrl/gp_pogo_5v_ctrl/enable");
			f.write(enable?"1".getBytes():"0".getBytes());
			f1 = new FileOutputStream("/sys/devices/soc/soc:sectrl/ugp_ctrl/gp_otg_en_ctrl/enable");
			f1.write(enable?"1".getBytes():"0".getBytes());

		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(f != null){
				try {
					f.close();
					f1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	    	initSound();
            setGPIOEnabled(true);
			//set53CGPIOEnabled(true);
			SystemClock.sleep(200);
	        ConnectUHF();
	        initViewPageData();
	        initViewPager();
	        initTabs();
	}
	
	 @Override
	    protected void initViewPageData() {
	        lstFrg.add(new UHFReadTagFragment());
	        lstFrg.add(new UHFReadFragment());
	        lstFrg.add(new UHFWriteFragment());
	        lstFrg.add(new UHFKillFragment());
	        lstFrg.add(new UHFLockFragment());
	        lstFrg.add(new UHFSetFragment());


	        lstTitles.add(getString(R.string.uhf_msg_tab_scan));
	        lstTitles.add(getString(R.string.uhf_msg_tab_read));
	        lstTitles.add(getString(R.string.uhf_msg_tab_write));
	        lstTitles.add(getString(R.string.uhf_msg_tab_kill));
	        lstTitles.add(getString(R.string.uhf_msg_tab_lock));
	        lstTitles.add(getString(R.string.uhf_msg_tab_set));
	    }

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Reader.rrlib.DisConnect();

			setGPIOEnabled(false);
            if (soundPool != null) {
                soundPool.release();
            }
			finish();

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
        try{
            Reader.rrlib.DisConnect();
            setGPIOEnabled(false);
			//set53CGPIOEnabled(false);
        }catch(Exception ex)
        {}
	}

	/**
	 * �豸�ϵ��첽��
	 *
	 * @author liuruifeng
	 */
	public class InitTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog mypDialog;

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			String sversion  =Reader.rrlib.GetVersion();
			if(sversion!=null)
				return true;
			else
				return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mypDialog.cancel();
			if (!result) {
				Toast.makeText(UHFMainActivity.this, "init fail",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			mypDialog = new ProgressDialog(UHFMainActivity.this);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setMessage("init...");
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();
		}

	}



	/**
	 * ��֤ʮ����������Ƿ���ȷ
	 *
	 * @param str
	 * @return
	 */
	public boolean vailHexInput(String str) {

		if (str == null || str.length() == 0) {
			return false;
		}

		// ���ȱ�����ż��
		if (str.length() % 2 == 0) {
			return StringUtility.isHexNumberRex(str);
		}

		return false;
	}
	HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
	private SoundPool soundPool;
	private float volumnRatio;
	private AudioManager am;
	private void initSound(){
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
		soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
		soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
		am = (AudioManager) this.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象
	}

	/**
	 * 播放提示音
	 *
	 * @param id 成功1，失败2
	 */
	public void playSound(int id) {

		float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
		float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
		volumnRatio = audioCurrentVolumn / audioMaxVolumn;
		try {
			soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
					volumnRatio, // 右声道音量
					1, // 优先级，0为最低
					0, // 循环次数，0无不循环，-1无永远循环
					1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
			);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}
