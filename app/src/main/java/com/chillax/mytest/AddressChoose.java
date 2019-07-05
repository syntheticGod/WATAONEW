package com.chillax.mytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.chillax.bean.LandDivide;
import com.chillax.db.LandDivideDB;
import me.wowtao.pottery.R;
import me.wowtao.pottery.activity.BuyActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddressChoose extends Activity {
	
	private ListView mListView1;
	private ListView mListView2;
	private ListView mListView3;
	
	private LinearLayout mLinearLayout1;
	private LinearLayout mLinearLayout2;
	private LinearLayout mLinearLayout3;
	
	private LinearLayout onBackLinear;
	private TextView topTxt;
	
	private List<String> sheng = new ArrayList<String>();
	private List<String> shi = new ArrayList<String>();
	private List<String> qu = new ArrayList<String>();
	
	private ArrayAdapter<String> shengAdapter;
	private ArrayAdapter<String> shiAdapter;
	private ArrayAdapter<String> quAdapter;
	
	private TextView shengTxt2;
	private TextView shengTxt3,shiTxt3,topView3;
	
	private static String shengStr,shiStr,quStr;
	private LandDivideDB landDivideDB;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.my_set_addresschoose);
		
		mLinearLayout1 = (LinearLayout)this.findViewById(R.id.my_set_adresschoose_1);
		mLinearLayout2 = (LinearLayout)this.findViewById(R.id.my_set_adresschoose_2);
		mLinearLayout3 = (LinearLayout)this.findViewById(R.id.my_set_adresschoose_3);
		
		mLinearLayout1.setVisibility(View.VISIBLE);
		mLinearLayout2.setVisibility(View.GONE);
		mLinearLayout3.setVisibility(View.GONE);
		
		topTxt = (TextView) this.findViewById(R.id.txt_linear);
		onBackLinear = (LinearLayout) this.findViewById(R.id.download_layout1);
		topTxt.setText("添加收货地址");
		
		shengTxt2 = (TextView)this.findViewById(R.id.my_set_adresschoose_sheng_2);
		shengTxt3 = (TextView)this.findViewById(R.id.my_set_adresschoose_sheng_3);
		shiTxt3 = (TextView)this.findViewById(R.id.my_set_adresschoose_shi_3);
		topView3 = (TextView)this.findViewById(R.id.my_set_adresschoose_textview_3);

		landDivideDB = LandDivideDB.getInstance(getBaseContext());
		List<LandDivide> landDivide = landDivideDB.queryAddress("superior=?", new String[]{"1"});
		Iterator<LandDivide> iterator = null;
		if(landDivide!=null){
			iterator = landDivide.iterator();

			while(iterator.hasNext()){
				LandDivide l = iterator.next();
				sheng.add(l.getName());
			}
		}else{
			return;
		}
		
		shengAdapter = new ArrayAdapter(this,R.layout.my_set_addresschoose_listview_item,R.id.my_set_adresschoose_textview,sheng);
		shiAdapter = new ArrayAdapter(this,R.layout.my_set_addresschoose_listview_item,R.id.my_set_adresschoose_textview,shi);
		quAdapter = new ArrayAdapter(this,R.layout.my_set_addresschoose_listview_item,R.id.my_set_adresschoose_textview,qu);
		
		mListView1 = (ListView)this.findViewById(R.id.my_set_adresschoose_listview_1);
		mListView1.setAdapter(shengAdapter);
		mListView2 = (ListView)this.findViewById(R.id.my_set_adresschoose_listview_2);
		mListView2.setAdapter(shiAdapter);
		mListView3 = (ListView)this.findViewById(R.id.my_set_adresschoose_listview_3);
		mListView3.setAdapter(quAdapter);
		
		shengTxt2.setOnClickListener(click);
		shengTxt3.setOnClickListener(click);
		shiTxt3.setOnClickListener(click);
		
		mListView1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				mLinearLayout1.setVisibility(View.GONE);
				mLinearLayout2.setVisibility(View.VISIBLE);
				mLinearLayout3.setVisibility(View.GONE);
				
				shi.clear();
				String name = sheng.get(position);
				String code = null;
				shengStr = name;
				shengTxt2.setText(name);
				
				List<LandDivide> landDivide = landDivideDB.queryAddress("name=?", new String[]{name});
				Iterator<LandDivide> iterator= landDivide.iterator();
				while(iterator.hasNext()){
					LandDivide l = iterator.next();
					code = l.getCode();
					break;
				}
				
				List<LandDivide> landDivide_2 = landDivideDB.queryAddress("superior=?", new String[]{code});
				Iterator<LandDivide> iterator_2= landDivide_2.iterator();
				while(iterator_2.hasNext()){
					LandDivide l = iterator_2.next();
					shi.add(l.getName());
				}
				
                shiAdapter.notifyDataSetChanged();
                quAdapter.clear();
                quAdapter.notifyDataSetChanged();
			}
		});
		
		mListView2.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				
				qu.clear();
               
				String name = shi.get(position);
				String code = null;
				
				shiStr = name;
				shengTxt3.setText(shengStr);
				shiTxt3.setText(name);
				
				List<LandDivide> landDivide = landDivideDB.queryAddress("name=?", new String[]{name});
				
					Iterator<LandDivide> iterator= landDivide.iterator();
					while(iterator.hasNext()){
						LandDivide l = iterator.next();
						code = l.getCode();
						break;
					}
					
					
				List<LandDivide> landDivide_2 = landDivideDB.queryAddress("superior=?", new String[]{code});
				if(landDivide_2!=null){
					Iterator<LandDivide> iterator_2= landDivide_2.iterator();
					while(iterator_2.hasNext()){
						LandDivide l = iterator_2.next();
						qu.add(l.getName());
					}
				}
				
				
				
				 quAdapter.notifyDataSetChanged();
				 
				 if(qu.size()<1){
						mLinearLayout1.setVisibility(View.GONE);
						mLinearLayout2.setVisibility(View.VISIBLE);
						mLinearLayout3.setVisibility(View.GONE);
					 
					 Intent i = new Intent(AddressChoose.this, BuyActivity.class);
					 i.putExtra("address", shengStr+" "+shiStr);
//					 startActivity(i);
					 setResult(Activity.RESULT_OK, i);
					 finish();
					 
				 }else{
					 
						mLinearLayout1.setVisibility(View.GONE);
						mLinearLayout2.setVisibility(View.GONE);
						mLinearLayout3.setVisibility(View.VISIBLE);
					 
					 mListView3.setVisibility(View.VISIBLE);
					 topView3.setText("请选择  县区/其他...");
				 }
			}
		});
		
		mListView3.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				
				String name = qu.get(position);
				
				quStr = name;
				
				Intent i2 = new Intent(AddressChoose.this,BuyActivity.class);
				i2.putExtra("address", shengStr+" "+shiStr+" "+quStr);
				setResult(Activity.RESULT_OK, i2);
//				startActivity(i2);
				finish();
			}
		});
		
		onBackLinear.setOnClickListener(click);
	}
	
	OnClickListener click = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.my_set_adresschoose_sheng_2:
				mLinearLayout1.setVisibility(View.VISIBLE);
				mLinearLayout2.setVisibility(View.GONE);
				mLinearLayout3.setVisibility(View.GONE);
				break;
			case R.id.my_set_adresschoose_sheng_3:
				mLinearLayout1.setVisibility(View.VISIBLE);
				mLinearLayout2.setVisibility(View.GONE);
				mLinearLayout3.setVisibility(View.GONE);
				break;
			case R.id.my_set_adresschoose_shi_3:
				mLinearLayout1.setVisibility(View.GONE);
				mLinearLayout2.setVisibility(View.VISIBLE);
				mLinearLayout3.setVisibility(View.GONE);
				break;
				
			case R.id.download_layout1:
				finish();
				break;
			default:
				break;
			}
		}
	};

}
