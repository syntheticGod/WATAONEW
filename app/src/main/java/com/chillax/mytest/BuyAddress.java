package com.chillax.mytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.*;
import com.chillax.bean.AddressInfo;
import com.chillax.db.AddressDB;
import me.wowtao.pottery.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class BuyAddress extends Activity {

    private EditText jiequText;
    private EditText nameText;
    private EditText phoneText;

    private String provinces;
    private AddressInfo myAddress;

    private LinearLayout shengLinear;
    private LinearLayout jiequLinear;
    private LinearLayout nameLinear;
    private LinearLayout phoneLinear;

    private TextView shengText;
    private TextView jiequTextView;
    private TextView nameTextView;
    private TextView phoneTextView;

    private Button postBtn;
    private AddressInfo addressinfo;
    private CheckBox checkBox;

    private LinearLayout onBackLinear;
    private TextView topTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.my_set_buyaddress);

        myAddress = new AddressInfo();

        Intent i = getIntent();
        provinces = i.getStringExtra("address");
        Bundle b = i.getBundleExtra("address_id");

        topTxt = (TextView) this.findViewById(R.id.txt_linear);
        onBackLinear = (LinearLayout) this.findViewById(R.id.download_layout1);
        topTxt.setText("添加收货地址");

        shengText = (TextView) this.findViewById(R.id.my_set_buyaddress_sheng);
        jiequText = (EditText) this.findViewById(R.id.my_set_buyaddress_jiequ);
        nameText = (EditText) this.findViewById(R.id.my_set_buyaddress_name);
        phoneText = (EditText) this.findViewById(R.id.my_set_buyaddress_phone);

        shengLinear = (LinearLayout) this
                .findViewById(R.id.my_set_buyaddress_sheng_linear);
        jiequLinear = (LinearLayout) this
                .findViewById(R.id.my_set_buyaddress_jiequ_linear);
        nameLinear = (LinearLayout) this
                .findViewById(R.id.my_set_buyaddress_name_linear);
        phoneLinear = (LinearLayout) this
                .findViewById(R.id.my_set_buyaddress_phone_linear);

        jiequTextView = (TextView) this
                .findViewById(R.id.my_set_buyaddress_jiequ_text);
        nameTextView = (TextView) this
                .findViewById(R.id.my_set_buyaddress_name_text);
        phoneTextView = (TextView) this
                .findViewById(R.id.my_set_buyaddress_phone_text);
        checkBox = (CheckBox) this.findViewById(R.id.my_set_address_checkbox);

        postBtn = (Button) this
                .findViewById(R.id.my_set_buyaddress_address_btn);

        if (provinces == null) {
        } else {
            myAddress.setProvinces(provinces);
            shengText.setText(provinces);
        }

        shengLinear.setOnClickListener(click);
        jiequLinear.setOnClickListener(click);
        nameLinear.setOnClickListener(click);
        phoneLinear.setOnClickListener(click);
        postBtn.setOnClickListener(click);
        onBackLinear.setOnClickListener(click);

        jiequText.setOnFocusChangeListener(focusChanger);
        nameText.setOnFocusChangeListener(focusChanger);
        phoneText.setOnFocusChangeListener(focusChanger);

        if (b != null) {
            addressinfo = (AddressInfo) b.get("address");

            phoneText.setVisibility(View.GONE);
            phoneText.setText(addressinfo.getPhone());
            phoneLinear.setVisibility(View.VISIBLE);
            phoneTextView.setText(addressinfo.getPhone());

            shengText.setText(addressinfo.getProvinces());
            jiequText.setText(addressinfo.getStreet());
            nameText.setText(addressinfo.getName());
            phoneText.setText(addressinfo.getPhone());

            jiequText.setVisibility(View.GONE);
            jiequLinear.setVisibility(View.VISIBLE);
            jiequTextView.setText(addressinfo.getStreet());

            nameText.setVisibility(View.GONE);
            nameLinear.setVisibility(View.VISIBLE);
            nameTextView.setText(addressinfo.getName());

            if (addressinfo.isStatus()) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }

            myAddress.setId(addressinfo.getId());
            myAddress.setProvinces(addressinfo.getProvinces());
            myAddress.setStreet(addressinfo.getStreet());
            myAddress.setName(addressinfo.getName());
            myAddress.setPhone(addressinfo.getPhone());
            myAddress.setStatus(addressinfo.isStatus());
        }

    }

    OnFocusChangeListener focusChanger = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // TODO Auto-generated method stub
            myAddress.setStreet(jiequText.getText().toString());
            myAddress.setName(nameText.getText().toString());
            myAddress.setPhone(phoneText.getText().toString());

            switch (v.getId()) {
                case R.id.my_set_buyaddress_jiequ:
                    if (!hasFocus && myAddress.getStreet().length() > 0) {
                        jiequText.setVisibility(View.GONE);
                        jiequLinear.setVisibility(View.VISIBLE);

                        jiequTextView.setText(myAddress.getStreet());
                    }

                    if (hasFocus) {
                        jiequText.setSelectAllOnFocus(true);
                    }
                    break;
                case R.id.my_set_buyaddress_name:
                    if (!hasFocus && myAddress.getName().length() > 0) {
                        nameText.setVisibility(View.GONE);
                        nameLinear.setVisibility(View.VISIBLE);

                        nameTextView.setText(myAddress.getName());
                    }

                    if (hasFocus) {
                        nameText.setSelectAllOnFocus(true);
                    }
                    break;
                case R.id.my_set_buyaddress_phone:
                    if (!hasFocus && myAddress.getPhone().length() > 0) {
                        phoneText.setVisibility(View.GONE);
                        phoneLinear.setVisibility(View.VISIBLE);

                        phoneTextView.setText(myAddress.getPhone());
                    }
                    if (hasFocus) {
                        phoneText.setSelectAllOnFocus(true);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    OnClickListener click = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.my_set_buyaddress_sheng_linear:
                    Intent i = new Intent(BuyAddress.this, AddressChoose.class);
                    i.putExtra("Boolean", "aaa");
                    startActivity(i);
                    break;
                case R.id.my_set_buyaddress_jiequ_linear:
                    jiequText.setVisibility(View.VISIBLE);
                    jiequLinear.setVisibility(View.GONE);

                    jiequText.setFocusable(true);
                    jiequText.setFocusableInTouchMode(true);

                    jiequText.requestFocus();
                    break;
                case R.id.my_set_buyaddress_name_linear:
                    nameText.setVisibility(View.VISIBLE);
                    nameLinear.setVisibility(View.GONE);

                    nameText.setFocusable(true);
                    nameText.setFocusableInTouchMode(true);

                    nameText.requestFocus();
                    break;
                case R.id.my_set_buyaddress_phone_linear:
                    phoneText.setVisibility(View.VISIBLE);
                    phoneLinear.setVisibility(View.GONE);

                    phoneText.setFocusable(true);
                    phoneText.setFocusableInTouchMode(true);

                    phoneText.requestFocus();
                    break;
                case R.id.my_set_buyaddress_address_btn:
                    myAddress.setStreet(jiequText.getText().toString());
                    myAddress.setName(nameText.getText().toString());
                    myAddress.setPhone(phoneText.getText().toString());

                    if (myAddress.getPhone().length() > 0) {
                        phoneText.setVisibility(View.GONE);
                        phoneLinear.setVisibility(View.VISIBLE);
                        phoneTextView.setText(myAddress.getPhone());
                    }
                    postBtn.requestFocus();

                    postBtn.setFocusable(true);
                    postBtn.setFocusableInTouchMode(true);

                    if (myAddress.getProvinces().length() < 1 || myAddress.getStreet().length() < 1
                            || myAddress.getName().length() < 1 || myAddress.getPhone().length() < 1) {
                        Toast.makeText(getBaseContext(), "请完整填写收货人资料", 0).show();
                        return;
                    }

                    myAddress.setStatus(checkBox.isChecked());
                    AddressDB addressDB = AddressDB.getInstance(getBaseContext());

                    if (checkBox.isChecked()) {
                        List<AddressInfo> list = addressDB.queryAddress();
                        if (list != null) {
                            Iterator<AddressInfo> iterator = list.iterator();
                            while (iterator.hasNext()) {
                                AddressInfo a = iterator.next();
                                a.setStatus(false);
                                addressDB.updeteAddress(a);
                            }
                        }

                    }


                    if (addressinfo != null) {
                        if (addressDB.updeteAddress(myAddress)) {
                            Toast.makeText(getBaseContext(), "修改收货地址成功", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getBaseContext(), "修改收货地址失败", Toast.LENGTH_LONG).show();
                        }
                    } else {

                        SimpleDateFormat format = new SimpleDateFormat(
                                "yyyyMMddHHmmss");
                        Date date = new Date();
                        String id = format.format(date);
                        myAddress.setId(id);

                        if (addressDB.insertAddress(myAddress)) {
                            Toast.makeText(getBaseContext(), "添加收货地址成功", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getBaseContext(), "添加收货地址失败", Toast.LENGTH_LONG).show();
                        }
                    }

                    Intent intent = new Intent(BuyAddress.this, PersonAddress.class);
                    startActivity(intent);
                    finish();


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
