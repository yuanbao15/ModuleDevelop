package com.iflytek.voicedemo.IdentifyGroup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.epichust.voice.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 声纹密码和人脸识别相关组操作
 * 支持组创建/添加成员/查询组成员/删除成员/删除组/
 * @see this#createGroup()
 * @see this#joinGroup(String)
 * @see //this#queryGroups()
 * @see this#deleteGroup(String, boolean) ,第二个参数用于标示删除组成员or组
 *
 * @author hjyu
 * @date 2017/9/28.
 * @see <a href="http://www.xfyun.cn">讯飞开放平台</a>
 */
public class GroupManagerActivity extends Activity implements OnClickListener ,AdapterView.OnItemClickListener {
	private final static String TAG = GroupManagerActivity.class.getSimpleName();
	// 身份验证对象
	private IdentityVerifier mIdVerifier;
	// 用户名信息
	private TextView mUserNameText;

	EditText etGroupId;
	EditText etGroupName;
	private Toast mToast;
	MyAdapter adapter;
	private ProgressDialog mProDialog;
	String authId;
	String mfv_type;
	ArrayList<Map<String,String>> mList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_group_manager);
		mIdVerifier = IdentityVerifier.createVerifier(this, null);

		authId = getIntent().getStringExtra("auth_id");
		mfv_type = getIntent().getStringExtra("mfv_scenes");
		mList = new ArrayList<Map<String, String>>();
		// 画面初期化
		initLayout();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * 画面初期化。
	 */
	@SuppressLint("ShowToast")
	private void initLayout() {

		mProDialog = new ProgressDialog(this);
		// 等待框设置为不可取消
		mProDialog.setCancelable(true);
		mProDialog.setCanceledOnTouchOutside(false);
		mProDialog.setTitle("请稍候");

		mProDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// cancel进度框时,取消正在进行的操作
				if (null != mIdVerifier) {
					mIdVerifier.cancel();
				}
			}
		});

		LayoutInflater inflater = LayoutInflater.from(this);
		RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.group_header_layout, null);

		mUserNameText = (TextView) view.findViewById(R.id.txt_username);
		mUserNameText.setText(authId);

		stopProgress();
		// 创建组
		((Button) view.findViewById(R.id.btn_group_create)).setOnClickListener(this);
		// 加入组
		((Button) view.findViewById(R.id.btn_group_join)).setOnClickListener(this);
		
		((Button) view.findViewById(R.id.btn_person_delete)).setOnClickListener(this);
		((Button) view.findViewById(R.id.btn_group_delete)).setOnClickListener(this);
		// 输入组id
		etGroupId = ((EditText) view.findViewById(R.id.edt_group_id));
		// 输入组名称
		etGroupName = ((EditText) view.findViewById(R.id.edt_group_name));
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

		// 绑定XML中的ListView，作为Item的容器
		ListView list = (ListView) findViewById(R.id.lv_my_group);
		list.setOnItemClickListener(this);
		list.addHeaderView(view);
		// 去除行与行之间的黑线：
		list.setDivider(null);
		
		// 添加并且显示
		adapter = new MyAdapter(this, mList );
		list.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		if( null == mIdVerifier ){
			// 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
			showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
			return;
		}
		
		if (v.getId() == R.id.btn_group_create ) {
			createGroup();
		}else if (v.getId() == R.id.btn_group_join ) {
			joinGroup(null);
		}else if (v.getId() == R.id.btn_person_delete ) {
			deleteGroup(null, false);
		}else if (v.getId() == R.id.btn_group_delete ) {
			deleteGroup(null, true);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent init = new Intent();
		Map<String,String> map = (Map<String,String>)parent.getAdapter().getItem(position);

		init.putExtra("group_id",map.get("group_id"));
		if (mfv_type.equals("ivp")) {
			init.setClass(GroupManagerActivity.this,VocalIdentifyActivity.class);
		}else {
			init.setClass(GroupManagerActivity.this, FaceIdentifyActivity.class);
		}
		startActivity(init);
	}

	/**
	 * 开启进度条
	 */
	private void startProgress(String msg) {
		mProDialog.setMessage(msg);
		mProDialog.show();
		((RelativeLayout) findViewById(R.id.group_manager_layout)).setEnabled(false);
	}

	/**
	 * 关闭进度条
	 */
	private void stopProgress() {
		if (null != mProDialog) {
			mProDialog.dismiss();
		}
		((RelativeLayout) findViewById(R.id.group_manager_layout)).setEnabled(true);
	}

	private void createGroup() {
		String groupName = etGroupName.getText().toString();
		if (TextUtils.isEmpty(groupName)) {
			showTip("请填写groupName");
			return;
		}
		startProgress("正在创建组...");
		// sst=add，scope=group，group_name=famil;
		// 设置人脸模型操作参数
		// 清空参数
		mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
		// 设置会话场景
		mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
		// 用户id
		mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authId);

		// 设置模型参数，若无可以传空字符传
		StringBuffer params = new StringBuffer();
		params.append("auth_id=" + authId);
		params.append(",scope=group");
		params.append(",group_name=" + groupName);
		// 执行模型操作
		mIdVerifier.execute("ipt", "add", params.toString(), mCreateListener);
	}

	private void joinGroup(String groupIdCreate) {
		String groupId;
		if (!TextUtils.isEmpty(groupIdCreate)) {
			groupId = groupIdCreate;
		} else {
			groupId = etGroupId.getText().toString();
		}
		if (TextUtils.isEmpty(groupId)) {
			showTip("请填写groupId");
			return;
		}
		startProgress("正在加入组...");

		// sst=add，auth_id=eqhe，group_id=123456，scope=person
		mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
		// 设置会话场景
		mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
		// 用户id
		mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authId);
		// 设置模型参数，若无可以传空字符传
		StringBuffer params2 = new StringBuffer();
		params2.append("auth_id=" + authId);
		params2.append(",scope=person");
		params2.append(",group_id=" + groupId);
		// 执行模型操作
		mIdVerifier.execute("ipt", "add", params2.toString(), mAddListener);
	}
	
//	/**
//	 * 查询指定组中成员
//	 * @param groupJoin
//	 */
//	private void queryGroup(String groupJoin) {
//		String groupId;
//		if (!TextUtils.isEmpty(groupJoin)) {
//			groupId = groupJoin;
//		} else {
//			groupId = etGroupId.getText().toString();
//		}
//		if (TextUtils.isEmpty(groupId)) {
//			showTip("请填写groupId");
//			return;
//		}
//		startProgress("正在查询组成员...");
//
//		// sst=add，auth_id=eqhe，group_id=123456，scope=person
//		mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
//		// 设置会话场景
//		mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
//		// 用户id
//		mIdVerifier.setParameter(SpeechConstant.AUTH_ID, DemoApp.getHostUser().getUsername());
//		// 设置模型参数，若无可以传空字符传
//		StringBuffer params2 = new StringBuffer();
//		params2.append("scope=group");
//		params2.append(",group_id=" + groupId);
//		// 执行模型操作
//		mIdVerifier.execute("ipt", "query", params2.toString(), mQueryListener);
//	}

//	/**
//	 * 查询当前appid 下所有组
//	 * 此处特别申明：***查询所有组信息能力仅提供用于开发者debug所用，如果用于其他
//	 * 场景或者处于其他目的，责任由程序开发者自付***
//	 */
	/*
	private void queryGroups() {
		startProgress("正在查询组数量...");
		mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
		mIdVerifier.setParameter(SpeechConstant.AUTH_ID, "wer");
		// 设置模型参数，若无可以传空字符传
		StringBuffer params2 = new StringBuffer();
		params2.append("scope=appid");
		Log.e(TAG,"queryGroups:"+params2.toString());
		// 执行模型操作
		mIdVerifier.execute("ipt", "query", params2.toString(), mQueryListener);
	}*/

	private void deleteGroup(String groupJoin, boolean deleteGroup) {
		String groupId;
		if (!TextUtils.isEmpty(groupJoin)) {
			groupId = groupJoin;
		} else {
			groupId = etGroupId.getText().toString();
		}
		if (TextUtils.isEmpty(groupId)) {
			showTip("请填写groupId");
			return;
		}
		
		
		startProgress("正在删除...");

		// sst=add，auth_id=eqhe，group_id=123456，scope=person
		mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
		// 设置会话场景
		mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
		// 用户id
		mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authId);

		// 设置模型参数，若无可以传空字符传
		StringBuffer params2 = new StringBuffer();
		if(deleteGroup) {
			params2.append("scope=group");
		} else {
			// 删除组中指定auth_id用户
			params2.append("scope=person");
			params2.append(",auth_id="+authId);
		}
		params2.append(",group_id=" + groupId);
		// 执行模型操作
		mIdVerifier.execute("ipt", "delete", params2.toString(), mDeleteListener);
	}
	

	/**
	 * 创建组监听器
	 */
	private IdentityListener mCreateListener = new IdentityListener() {

		@Override
		public void onResult(IdentityResult result, boolean islast) {
			stopProgress();
			Log.d(TAG, result.getResultString());
			try {
				JSONObject resObj = new JSONObject(result.getResultString());
				resObj.getString("group_id");
				// 创建成功后将自己加入到组里
				joinGroup(resObj.getString("group_id"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			showTip("组创建成功");
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}

		@Override
		public void onError(SpeechError error) {
			stopProgress();
			showTip(error.getPlainDescription(true));
		}
	};
	/**
	 * 加入组监听器
	 */
	private IdentityListener mAddListener = new IdentityListener() {

		@Override
		public void onResult(IdentityResult result, boolean islast) {
			Log.d(TAG, result.getResultString());
			try {
				JSONObject resObj = new JSONObject(result.getResultString());
				// 保存到用户信息中，用来显示用户加人的组
				Map<String,String> map = new HashMap<>();
				map.put("group_name",resObj.getString("group_name"));
				map.put("group_id",resObj.getString("group_id"));
				synchronized (mList) {
					if (mList != null && mList.size() >0) {
						for(int i = 0;i<mList.size();i++) {
							if(!mList.contains(map)) {
								mList.add(map);
							}
						}
					}else {
						mList.add(map);
					}
					mList.notify();
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
			showTip("加入组成功");
			//adapter.setArray(mList);
			adapter.notifyDataSetChanged();
			stopProgress();
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}

		@Override
		public void onError(SpeechError error) {
			stopProgress();
			showTip(error.getPlainDescription(true));
		}
	};
	
	private IdentityListener mQueryListener = new IdentityListener() {
		@Override
		public void onResult(IdentityResult result, boolean islast) {
			Log.d(TAG, result.getResultString());
			showTip("查询成功");
			stopProgress();
		}
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

		}
		@Override
		public void onError(SpeechError error) {
			Log.d(TAG, error.getPlainDescription(true));
			//showTip(ErrorDesc.getDesc(error) + ":" + error.getErrorCode());
			stopProgress();
		}
	};
	
	private IdentityListener mDeleteListener = new IdentityListener() {
		@Override
		public void onResult(IdentityResult result, boolean islast) {
			Log.d(TAG, result.getResultString());
			try {
				JSONObject resObj = new JSONObject(result.getResultString());
				Log.d(TAG,"resObj == "+resObj.toString());
				int ret = resObj.getInt("ret");
				if(0 != ret) {
					onError(new SpeechError(ret));
					return;
				} else {
					if(result.getResultString().contains("user")) {
						String user = resObj.getString("user");
						showTip("删除组成员"+user+"成功");
					} else {
						showTip("删除组成功");
						// 保存到用户信息中，用来显示用户加人的组
						for (Map<String,String> map1:mList){
							if (map1.get("group_id").equals(resObj.getString("group_id"))){
								mList.remove(map1);
							}
						}

					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			adapter.setArray(mList);
			adapter.notifyDataSetChanged();
			stopProgress();
		}
		
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			
		}
		
		@Override
		public void onError(SpeechError error) {
			stopProgress();
			showTip(error.getPlainDescription(true));
		}
	};

	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

	private class MyAdapter extends BaseAdapter {

		private Context context;
		private LayoutInflater inflater;
		public ArrayList<Map<String,String>> arr;

		public MyAdapter(Context context, ArrayList<Map<String,String>> array) {
			super();
			this.context = context;
			inflater = LayoutInflater.from(context);
			arr = array;
		}

		@Override
		public int getCount() {
			if (arr != null)
				return arr.size();
			else
				return 0;
		}

		@Override
		public Object getItem(int arg0) {
			if (arr.get(arg0) != null) {
				return arr.get(arg0);
			}
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
		
		public void setArray(ArrayList<Map<String,String>> list) {
			this.arr = list;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) {
				view = inflater.inflate(R.layout.item_group, null);
			}
			final TextView edit = (TextView) view.findViewById(R.id.group_item_content);
			Map<String,String> map2 = arr.get(arr.size() - position - 1);
			edit.setText(map2.get("group_id")+"("+map2.get("group_name")+")"); // 在重构adapter的时候不至于数据错乱
			return view;
		}
	}
}
