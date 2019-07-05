package com.chillax.mytest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.chillax.bean.AddressInfo;
import com.chillax.db.AddressDB;
import me.wowtao.pottery.R;

import java.util.ArrayList;
import java.util.List;

public class PersonAddress extends Activity {
	
	private ListView listView;
	private LinearLayout add_button;
	private List<AddressInfo> address = new ArrayList<AddressInfo>();
	private AddressDB addressDB;
	private MyAdapter adapter;
	private RelativeLayout dizhi;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myset_address);
		
		listView=(ListView) this.findViewById(R.id.myset_address_listView1);
		dizhi=(RelativeLayout) this.findViewById(R.id.dizhi);
		
		addressDB = AddressDB.getInstance(getBaseContext());
		address = addressDB.queryAddress();
		
		
		if(address == null){
			dizhi.setVisibility(View.VISIBLE);
		}
		
		adapter = new MyAdapter(getBaseContext());
		listView.setAdapter(adapter);
		add_button=(LinearLayout) this.findViewById(R.id.myset_address_linear);
		add_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent intent=new Intent(PersonAddress.this, AddressChoose.class);
				startActivity(intent);
				finish();
			}
		});
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3) {
				// TODO Auto-generated method stub
				final AddressInfo a = address.get(position);
				AlertDialog dialog = new AlertDialog.Builder(PersonAddress.this)
				.setTitle("删除收货地址")
				.setMessage("确定删除这个收货地址吗?")
				.setPositiveButton("确定", new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
								// TODO Auto-generated method stub
								if(addressDB.deleteAddress(a)){
									Toast.makeText(getBaseContext(), "删除成功", Toast.LENGTH_LONG).show();
								}else{
									Toast.makeText(getBaseContext(), "删除失败", Toast.LENGTH_LONG).show();
								}

								address.remove(position);
								adapter.notifyDataSetChanged();
								
								if(address.size() == 0){
									dizhi.setVisibility(View.VISIBLE);
								}
					}
				})
				.setNegativeButton("取消", new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.create();// 创建
				// 显示对话框
				dialog.show();
				
				
				return true;
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Intent i = new Intent(PersonAddress.this,BuyAddress.class);
				Bundle b = new Bundle();
				b.putSerializable("address", address.get(position));
				i.putExtra("address_id", b);
				startActivity(i);
			}
		});
	}
	
	class MyAdapter extends BaseAdapter{
		private Context context;
		private LayoutInflater inflater;
		
		public MyAdapter(Context context){
			this.context=context;
			this.inflater=LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return address!=null?address.size():0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder viewHolder=null;
			if(convertView==null){
				convertView=inflater.inflate(R.layout.myset_adress_list_item, null);
				viewHolder=new ViewHolder();
				viewHolder.name=(TextView)convertView.findViewById(R.id.myset_address_item_name);
				viewHolder.listViewItem=(LinearLayout)convertView.findViewById(R.id.listview_item);
				viewHolder.provinces=(TextView)convertView.findViewById(R.id.myset_address_item_provinces);
				viewHolder.street=(TextView)convertView.findViewById(R.id.myset_address_item_street);
				viewHolder.phone=(TextView)convertView.findViewById(R.id.myset_address_phone);
				viewHolder.moren=(CheckBox)convertView.findViewById(R.id.loading_checkbox);
				convertView.setTag(viewHolder);
			}else{
				viewHolder=(ViewHolder)convertView.getTag();
			}
			
			viewHolder.moren.setClickable(false);
			viewHolder.name.setText(address.get(position).getName());
			viewHolder.provinces.setText(address.get(position).getProvinces());
			viewHolder.street.setText(address.get(position).getStreet());
			viewHolder.phone.setText(address.get(position).getPhone());
			viewHolder.listViewItem.setTag(address.get(position).getId());
			
			if(address.get(position).isStatus()){
				viewHolder.moren.setChecked(true);
			}else{
				viewHolder.moren.setChecked(false);
			}
			
			
			return convertView;
		}
		
		class ViewHolder{
			LinearLayout listViewItem;
			TextView name,provinces,street,phone;
			CheckBox moren;
		}
	}

}
