package com.example.uhf.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.example.uhf.adapter.ViewPagerAdapter;
import com.example.uhf.fragment.KeyDwonFragment;
import com.example.uhf.tools.UIHelper;
import com.example.uhf.widget.NoScrollViewPager;
import com.rfid.trans.StringUtility;
import com.yuanbao.moduleuhf_i6310.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015-03-10.
 */
public class BaseTabFragmentActivity extends FragmentActivity {

	private final int offscreenPage = 2; //����ViewPager���ڵļ���ҳ��

	protected ActionBar mActionBar;


	protected NoScrollViewPager mViewPager;
	protected ViewPagerAdapter mViewPagerAdapter;


	protected List<KeyDwonFragment> lstFrg = new ArrayList<KeyDwonFragment>();
	protected List<String> lstTitles = new ArrayList<String>();

	private int index = 0;

	private ActionBar.Tab tab_kill, tab_lock, tab_set,tab_test ;
	private DisplayMetrics metrics;
	private AlertDialog dialog;
	private long[] timeArr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void ConnectUHF() {
		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		String project=getValueFromProp();
		int result =-1;
		if(project.equals("SQ52TG")||project.equals("SQ52TGW")||project.equals("SQ52T")||project.equals("SQ52")||project.equals("SQ52C") ||project.equals("SQ52W")){
			android.util.Log.i("wujinquan","SQ52T project:"+project);
			result = Reader.rrlib.Connect("/dev/ttyUSB0", 57600);
		}else if(project.equals("SQ53C")) {
			result = Reader.rrlib.Connect("/dev/ttyHSL0", 57600);
		}else if(project.equals("SQ53")){
			result = Reader.rrlib.Connect("/dev/ttyMSM1", 57600);
		}else if(project.equals("SQ31T")){
			result = Reader.rrlib.Connect("/dev/ttyMT0", 57600);
			if(result==0)
				setBrightness("/sys/class/ugp_ctrl/uhf_enable/enable", "1".getBytes());
		}
		//SystemClock.sleep(1500);
		if(result ==0)
		{
			Toast.makeText(BaseTabFragmentActivity.this,R.string.Connectsuccess,
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(BaseTabFragmentActivity.this, R.string.Connectfailed,
					Toast.LENGTH_SHORT).show();
		}
	}
    public void setGPIOEnabled(boolean enable){
		String project=getValueFromProp();
		android.util.Log.i("wujinquan","project:"+project);
		if(project.equals("SQ52TG")||project.equals("SQ52TGW")||project.equals("SQ52T")||project.equals("SQ52")||project.equals("SQ52C")||project.equals("SQ52W")){
			set52OTGEnabled(enable);
		}else if(project.equals("SQ53C")) {
			set53CGPIOEnabled(enable);
		}else if(project.equals("SQ53")){
			setBrightness("/sys/devices/soc/c170000.serial/pogo_uart",enable?"1".getBytes():"0".getBytes());//20190919
			set53Enabled(enable);
		}else if(project.equals("SQ31T")){
			setBrightness("/sys/class/ugp_ctrl/uhf/pwrctl",enable?"1".getBytes():"0".getBytes());
			setBrightness("/sys/class/ugp_ctrl/uhf/enctl",enable?"1".getBytes():"0".getBytes());
		}
    }

	private String getValueFromProp() {
		String value = "";
		Class<?> classType = null;
		try {
			classType = Class.forName("android.os.SystemProperties");
			//拿到get方法，此方法传入一个String类型参数。
			Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
			// 键值persist.sys.sb.hide，其他的也可以。
			value = (String) getMethod.invoke(classType, new Object[]{"pwv.project"});
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return value;
	}

	private void set52OTGEnabled(boolean enable){
		FileOutputStream f = null;
		try{
			f = new FileOutputStream("/sys/devices/soc/78db000.usb/dpdm_pulldown_enable");
			f.write(enable?"otgenable".getBytes():"otgdisable".getBytes());
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(f != null){
				try {
					f.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}



	private void set53Enabled(boolean enable){
		FileOutputStream f = null;
		try{
			f = new FileOutputStream("sys/devices/virtual/Usb_switch/usbswitch/function_otg_en");
			f.write(enable?"2".getBytes():"0".getBytes());

		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(f != null){
				try {
					f.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

    public void set53CGPIOEnabled(boolean enable){
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
    }

	private synchronized void setBrightness(String ledDev, byte[] lightOn) {
		try {
			FileOutputStream fLed = new FileOutputStream(ledDev);
			fLed.write(lightOn);
			fLed.close();
		} catch (Exception e) {
			System.out.print(e);
		}
	}

	protected void initViewPageData() {

	}

	/**
	 * ����ActionBar
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
//		return super.onCreateOptionsMenu(menu);
	}

	protected void initViewPager() {

		mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), lstFrg, lstTitles);

		mViewPager = (NoScrollViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mViewPagerAdapter);
		mViewPager.setOffscreenPageLimit(offscreenPage);
	}

	protected void initTabs() {
		for (int i = 0; i < mViewPagerAdapter.getCount() - 3; ++i) {
			mActionBar.addTab(mActionBar.newTab()
					.setText(mViewPagerAdapter.getPageTitle(i)).setTabListener(mTabListener));
		}
		tab_kill = mActionBar.newTab().setText(mViewPagerAdapter.getPageTitle(3)).setTabListener(mTabListener);
		tab_lock = mActionBar.newTab().setText(mViewPagerAdapter.getPageTitle(4)).setTabListener(mTabListener);
		tab_set = mActionBar.newTab().setText(mViewPagerAdapter.getPageTitle(5)).setTabListener(mTabListener);

		//��Ӳ˵�
//        mActionBar.addTab(mActionBar.newTab().setText(getString(R.string.myMenu)).setTabListener(mTabListener));
	}


	protected ActionBar.TabListener mTabListener = new ActionBar.TabListener() {

		@Override
		public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
			if (mActionBar.getTabCount() > 3 && tab.getPosition() != 3) {
				mActionBar.removeTabAt(3);
			}
			if (tab.getPosition() == 3) {
				mViewPager.setCurrentItem(index, false);
			} else {
				mViewPager.setCurrentItem(tab.getPosition());
			}
		}

		@Override
		public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

		}

		@Override
		public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

		}
	};

	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		//
		if (mActionBar.getSelectedTab().getText().equals(item.getTitle())) {
			return true;
		}
		if (mActionBar.getTabCount() > 3
				&& item.getItemId() != android.R.id.home && item.getItemId() != R.id.UHF_ver) {
			mActionBar.removeTabAt(3);
		}
//		if (item.getItemId() == android.R.id.home) {
//			finish();
//		} else if (item.getItemId() == R.id.action_kill) {
//			index = 3;
//			mActionBar.addTab(tab_kill, true);
//		} else if (item.getItemId() == R.id.action_lock) {
//			index = 4;
//			mActionBar.addTab(tab_lock, true);
//		} else if (item.getItemId() == R.id.action_set) {
//			index = 5;
//			mActionBar.addTab(tab_set, true);
//		} else if (item.getItemId() == R.id.UHF_ver) {
//			getUHFVersion();
//		}

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == 139 ||keyCode == 280 || keyCode ==520 ||keyCode ==521 || keyCode ==522 || keyCode ==523) {

			if (event.getRepeatCount() == 0) {

				if (mViewPager != null) {

					KeyDwonFragment sf = (KeyDwonFragment) mViewPagerAdapter.getItem(mViewPager.getCurrentItem());
					sf.myOnKeyDwon();

				}
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void gotoActivity(Intent it) {
		startActivity(it);
	}

	public void gotoActivity(Class<? extends BaseTabFragmentActivity> clz) {
		Intent it = new Intent(this, clz);
		gotoActivity(it);
	}

	public void toastMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public void toastMessage(int resId) {
		Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		Reader.rrlib.DisConnect();
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

		if (str.length() % 2 == 0) {
			return  StringUtility.isHexNumberRex(str);
		}

		return false;
	}

	public void getUHFVersion() {

		byte[] Version=new byte[2];
		byte[] Power=new byte[1];
		byte[] band = new byte[1];
		byte[] MaxFre= new byte[1];
		byte[] MinFre = new byte[1];
		byte[] BeepEn = new byte[1];
		byte[] Ant =new byte[1];
		int result =Reader.rrlib.GetUHFInformation(Version,Power,band,MaxFre,MinFre,BeepEn,Ant);
		if(result ==0)
		{
			String temp = String.valueOf(Version[1]&255);
			if(temp.length()==1)
				temp="0"+temp;
			String rfidVer =String.valueOf(Version[0]&255)+"."+temp;
			UIHelper.alert(this, R.string.action_uhf_ver,
					rfidVer, R.drawable.webtext);
		}
	}

}
