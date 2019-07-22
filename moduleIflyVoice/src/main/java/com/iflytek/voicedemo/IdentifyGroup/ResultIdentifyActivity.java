package com.iflytek.voicedemo.IdentifyGroup;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.epichust.voice.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * 1:N检测结果解析界面
 * @author hjyu
 * @date 2017/9/28.
 * @see <a href="http://www.xfyun.cn">讯飞开放平台</a>
 */
public class ResultIdentifyActivity extends Activity implements OnClickListener {

	private String result;
	JSONArray candidates;
	MyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_result_identify);
		
		initUI();
	}

	private void initUI() {
		TextView title = (TextView) findViewById(R.id.txt_idf_result_title);

		result = getIntent().getStringExtra("result");
		// 结果形式
		// result = "
		/*{
			"ret":0,
			"group_id":"xxxxxx",
			"group_name":"xxxxxx",
			"ifv_result":{
				"candidates":[{
					"model_id":"xxxxxxxx",
					"decision":"accepted",
					"score":88.888888,
					"user":"user_name"}]
				},
			"sst":"identify",
			"ssub":"ivp",
			"topc":1
		}*/
		try {
			JSONObject obj = new JSONObject(result);
			// 组名称
			((TextView)findViewById(R.id.txt_group_name)).setText(obj.getString("group_name"));
			
			JSONObject ifv_result = obj.getJSONObject("ifv_result");
			candidates = ifv_result.getJSONArray("candidates");

			// 鉴别结果
			// 绑定XML中的ListView，作为Item的容器
			ListView list = (ListView) findViewById(R.id.lv_identify_result);
			// 去除行与行之间的黑线：
			list.setDivider(null);
			// 添加并且显示
			adapter = new MyAdapter(ResultIdentifyActivity.this, candidates);
			list.setAdapter(adapter);
			adapter.notifyDataSetChanged();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 相似度排行
	private class MyAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		public JSONArray arr;

		public MyAdapter(Context context, JSONArray array) {
			super();
			inflater = LayoutInflater.from(context);
			arr = array;
		}

		@Override
		public int getCount() {
			if (arr != null)
				return arr.length();
			else
				return 0;
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) {
				view = inflater.inflate(R.layout.item_identify_result, null);
			}
			final TextView tx_number = (TextView) view.findViewById(R.id.identify_item_number);
			final TextView tx_user = (TextView) view.findViewById(R.id.identify_item_user);
			final TextView tx_score = (TextView) view.findViewById(R.id.identify_item_score);
			JSONObject obj;
			try {
				obj = arr.getJSONObject(position);
				tx_number.setText("第" + getChineseNumber(position + 1) + ":");
				tx_user.setText(obj.optString("user"));
				DecimalFormat df = new DecimalFormat("0.00");
				
				Double score = obj.optDouble("score");
				String scoreStr = df.format(score) + "%";
				tx_score.setText(scoreStr);
			} catch (JSONException e) {
				e.printStackTrace();
			}


			return view;
		}
	}

	private String getChineseNumber(int number) {
		String[] str = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
		String ss[] = new String[] { "", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿" };
		String s = String.valueOf(number);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			String index = String.valueOf(s.charAt(i));
			sb = sb.append(str[Integer.parseInt(index)]);
		}
		String sss = String.valueOf(sb);
		int i = 0;
		for (int j = sss.length(); j > 0; j--) {
			sb = sb.insert(j, ss[i++]);
		}
		return sb.toString();
	}

	@Override
	public void onClick(View v) {
	}
}
