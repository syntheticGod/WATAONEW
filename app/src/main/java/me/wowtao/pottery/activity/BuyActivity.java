package me.wowtao.pottery.activity;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.pay.demo.Result;
import com.alipay.sdk.pay.demo.SignUtils;
import com.chillax.mytest.AddressChoose;
import com.chillax.service.landDivideServeice;
import com.dfire.retail.app.manage.network.AsyncHttpPostForUploadOrder;
import com.dfire.retail.app.manage.network.RequestParameter;
import com.dfire.retail.app.manage.network.RequestResultCallback;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.wowtao.pottery.Constants;
import me.wowtao.pottery.R;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.gl.Pottery;
import me.wowtao.pottery.utils.CommonUtil;
import me.wowtao.pottery.utils.PotteryTextureManager;
import me.wowtao.pottery.utils.PotteryTextureManager.Pattern;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class BuyActivity extends BaseActivity {

    private EditText numberEditText;
    private int number_t;

    private static class MyHandler extends Handler {
        WeakReference<BuyActivity> mActivity;

        MyHandler(BuyActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        public void handleMessage(Message msg) {
            final BuyActivity context = mActivity.get();
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    Result resultObj = new Result((String) msg.obj);
                    String resultStatus = resultObj.resultStatus;

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        new AlertDialog.Builder(context)
                                .setMessage(
                                        "感谢使用哇陶！\n" +
                                                " 订单号： " + context.orderNumber + "，\n" +
                                                " 客服电话：18079819897，\n" +
                                                " 公众号：哇陶科技")
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        context.finish();
                                    }
                                }).show();
                        context.changeOrderStatus("已支付");
                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000” 代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(context, "支付结果确认中",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "支付失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }
                case SDK_CHECK_FLAG: {
                    Toast.makeText(context, "检查结果为：" + msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    }


    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        return closeApp(keyCode, event);
    }


    private EditText nameEditText;
    private EditText phoneEditText;
    private TextView addressEditText;
    private EditText addressDetailEditText;
    private EditText workNameEditText;
    private EditText remarkEditText;
    private View icon;
    private EditText emailEditText;
    private ProgressDialog dialog;
    private TextView price;
    private String orderNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new MyHandler(this);
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.activity_buy);
        findViewById(R.id.back).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                BuyActivity.this.finish();
            }
        });

        ImageView image = (ImageView) findViewById(R.id.buy_image);
        image.setImageBitmap(Wowtao.getGlManager().getTempImage());


        icon = findViewById(R.id.buy_icon);
        icon.setVisibility(View.GONE);

        nameEditText = (EditText) findViewById(R.id.buy_name);
        phoneEditText = (EditText) findViewById(R.id.buy_phone);
        addressEditText = (TextView) findViewById(R.id.buy_address);
        addressEditText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!landDivideServeice.isFinish) {
                    Toast.makeText(BuyActivity.this, "正在加载地址，请稍后！", Toast.LENGTH_SHORT).show();
                    while (!landDivideServeice.isFinish) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Intent intent = new Intent(BuyActivity.this, AddressChoose.class);
                startActivityForResult(intent, 10086);
            }
        });

        addressDetailEditText = (EditText) findViewById(R.id.buy_detail_address);
        workNameEditText = (EditText) findViewById(R.id.buy_work);
        remarkEditText = (EditText) findViewById(R.id.buy_remark);
        emailEditText = (EditText) findViewById(R.id.email_address);
        numberEditText = (EditText) findViewById(R.id.number);

        Button confirm = (Button) findViewById(R.id.buy_submit);
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                upLoadOrder();
            }
        });

        price = (TextView) findViewById(R.id.buy_price);

        if (getIntent().getStringExtra("from") != null && getIntent().getStringExtra("from").equals("collect")) {
            final String text = Wowtao.getGlManager().price + "元";
            price.setText(text);
            price.setTag(String.valueOf(Wowtao.getGlManager().price));
        } else {
            final String text = String.valueOf(Wowtao.getGlManager().getPrice()) + "元";
            price.setText(text);
            price.setTag(String.valueOf(Wowtao.getGlManager().getPrice()));
        }
    }

    private void upLoadOrder() {
        if (!checkInternet()) {
            Toast.makeText(this, "当前网络末连接，请联网后重提交", Toast.LENGTH_SHORT).show();
            return;
        }

        final String name = getString(nameEditText);
        final String phone = getString(phoneEditText);
        final String address = getString(addressEditText);
        final String addressDetail = getString(addressDetailEditText);
        final String workName = getString(workNameEditText);
        final String remark = getString(remarkEditText);
        final String email = getString(emailEditText);
        final String number_str = getString(numberEditText);
        number_t = 1;
        if (number_str != null) {
            try {
                number_t = Integer.valueOf(number_str);
                if (number_t <= 0) {
                    Toast.makeText(BuyActivity.this, "请输入正确的数量", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(BuyActivity.this, "请输入正确的数量", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        final int number = number_t;
        final boolean checked = false;
        final float price = Wowtao.getGlManager().getPrice();

        if (email == null
                || phone == null
                || address == null
                || addressDetail == null
                || workName == null) {
            Toast.makeText(BuyActivity.this, "请完善购买信息", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmail(email)) {
            Toast.makeText(this, "请输入正确的email地址", Toast.LENGTH_SHORT).show();
            return;
        }

        this.dialog = ProgressDialog.show(BuyActivity.this, "请稍候", "正在上传订单", true);
        RequestParameter rp = new RequestParameter(false);
        rp.setUrl(Constants.LOGIN_URL);
        TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String simSerialNumber = telemamanger.getSimSerialNumber();
        if (simSerialNumber == null) {
            simSerialNumber = telemamanger.getDeviceId();
        }
        rp.setParam("username", simSerialNumber);
        rp.setParam("phone_number", phone);
        rp.setParam("password", phone + "123456");
        rp.setParam("gender", "2");
        rp.setParam("email", email);
        rp.setParam("rev_address", address);
        rp.setParam("rev_zipcode", "123456");
        rp.setParam("rev_phone", phone);
        rp.setParam("user_point", "0");
        rp.setParam("user_header", "meiyou");
        rp.setParam("rank", "1");
        rp.setParam("address", address + addressDetail);

        new AsyncHttpPostForUploadOrder(rp, new RequestResultCallback() {
            @Override
            public void onSuccess(String str) {
                try {
                    System.out.println(str);
                    str = str.trim();
                    str = str.substring(str.indexOf("{"), str.lastIndexOf("}") + 1);
                    JsonElement je = new JsonParser().parse(str);
                    int result = je.getAsJsonObject().get("result").getAsInt();

                    if (result == 0 || result == 1) {
                        upload(name == null ? "未填写erw3" : name, phone, email, address, addressDetail, workName, remark, checked, price, number);
                    } else {
                        CommonUtil.makeToast(BuyActivity.this, "网络出错，请稍候再试。");
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    CommonUtil.makeToast(BuyActivity.this, "网络出错，请稍候再试。");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(Exception e) {
                CommonUtil.makeToast(BuyActivity.this, "网络出错，请稍候再试。");
                e.printStackTrace();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }).execute();
    }

    private static boolean isEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void upload(String name, String phone, String email, String address, String addressDetail, String workName, String remark, boolean checked, float price, int number) {
        RequestParameter rp = new RequestParameter(false);
        rp.setUrl(Constants.UPLOAD_ORDER);

        TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String simSerialNumber = telemamanger.getSimSerialNumber();
        if (simSerialNumber == null) {
            simSerialNumber = telemamanger.getDeviceId();
        }
        rp.setParam("name", name);
        rp.setParam("phone", phone);
        rp.setParam("address", address + addressDetail);
        rp.setParam("pottery_name", workName);
        rp.setParam("number", String.valueOf(number));
        if (remark == null || remark.length() == 0) {
            remark = "无";
        }
        remark += "email:" + email;
        remark += "address:" + address + addressDetail;
        remark += "userName:" + name;
        remark += "phone:" + phone;
        rp.setParam("remark", remark);
        rp.setParam("price", String.valueOf(price));
        orderNumber = getTradeNo();
        rp.setParam("order_number", orderNumber);
        rp.setParam("user_name", simSerialNumber);
        rp.setParam("order_flag", "未支付");
        if (checked) {
            rp.setParam("agree_post_to_market", "1");
        } else {
            rp.setParam("agree_post_to_market", "0");
        }

        //获取cache目录
        File cacheDir = getExternalCacheDir();

        final File file = new File(cacheDir, "test.obj");
        Wowtao.getGlManager().getPottery().toOBJFile(file, true);

        //获取效果图
        File effectFile = new File(cacheDir, "xiaoguo.png");
        FileOutputStream effectStream = null;
        try {
            effectStream = new FileOutputStream(effectFile);
            Wowtao.getGlManager().getBigTempImage().compress(CompressFormat.PNG, 80, effectStream);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            if (dialog != null) {
                dialog.dismiss();
            }
        } finally {
            if (effectStream != null) {
                try {
                    effectStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        rp.setParam("image", effectFile);
        rp.setParam("obj", file);

        List<Long> decoratorIds = new ArrayList<>();
        List<Float> decoratorPositions = new ArrayList<>();
        List<File> customers = new ArrayList<>();
        int i = 0;
        for (Pattern p : PotteryTextureManager.getPatterns().values()) {
            if (p.needP) {
                File tempFile = new File(cacheDir, "customer" + ++i + ".png");
                FileOutputStream tempStream = null;
                try {
                    tempStream = new FileOutputStream(tempFile);
                    PotteryTextureManager.getPatternTexture(p.idResource, false).compress(CompressFormat.PNG, 80, tempStream);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                } finally {
                    if (tempStream != null) {
                        try {
                            tempStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                customers.add(tempFile);
            } else {
                decoratorIds.add(p.id);
                decoratorPositions.add((float) p.top / (float) Pottery.VERTICAL_PRECISION / 10.0f);
            }
        }

        //可能没有装饰，这时候不传
        if (decoratorIds.size() != 0) {
            rp.setParam("decorators", ListToString(decoratorIds));
        }

        //可能没有装饰，这时候不传
        if (decoratorPositions.size() != 0) {
            rp.setParam("decorators_position", ListToString(decoratorPositions));
        }

        //可能没有装饰，这时候不传
        if (customers.size() != 0) {
            rp.setParam("customers[]", customers);
        }

        float[] radius = Wowtao.getGlManager().getPottery().getRadii();
        List<Float> radiusList = new ArrayList<>(radius.length);
        radiusList.add(Wowtao.getGlManager().getPottery().getHeightReal());
        radiusList.add(Wowtao.getGlManager().getPottery().getMaxWidth());
        rp.setParam("pottery_shape", ListToString(radiusList));

        File textureFile = new File(cacheDir, "texture.png");
        FileOutputStream fis = null;
        try {
            fis = new FileOutputStream(textureFile);
            Matrix matrix = new Matrix();
            matrix.preScale(2, 1);
            Bitmap texture = PotteryTextureManager.getTexture();
            Bitmap.createBitmap(texture, 0, texture.getHeight() / 2 - 50, texture.getWidth(), texture.getHeight() / 2 + 50, matrix, true)
                    .compress(CompressFormat.PNG, 90, fis);
        } catch (Exception e) {
            e.printStackTrace();
            if (dialog != null) {
                dialog.dismiss();
            }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        rp.setParam("image_decorator", textureFile);
//        rp.setParam("image_decorator", file);

        new AsyncHttpPostForUploadOrder(rp, new RequestResultCallback() {

            @Override
            public void onSuccess(String str) {
                try {
                    str = str.trim();
                    JsonElement je = new JsonParser().parse(str.substring(1, str.length() - 1));
                    JsonObject data = je.getAsJsonObject().get("data").getAsJsonObject();
                    String result = data.get("result").getAsString();
                    if (result.equals("success")) {
                        orderNumber = data.get("order_number").getAsString();
                        pay();
                    } else {
                        CommonUtil.makeToast(BuyActivity.this, "服务器内部错误！");
                    }
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                } catch (Exception e) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    CommonUtil.makeToast(BuyActivity.this, "服务器内部错误！");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(Exception e) {
                e.printStackTrace();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }).execute();

        icon.setScaleX(2);
        icon.setScaleY(2);
        icon.setVisibility(View.VISIBLE);
        icon.animate().setDuration(700).scaleX(1).scaleY(1).alpha(1).setListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        }).start();
    }

    private String ListToString(List<?> decoratorIds) {
        String res = "";
        for (Object id : decoratorIds) {
            if (id instanceof Long) {
                Long idLong = (Long) id;
                res += String.valueOf(idLong) + ",";
            } else {
                Float iFloat = (Float) id;
                res += String.valueOf(iFloat) + ",";
            }
        }
        if (res.length() != 0) {
            res = res.substring(0, res.length() - 1);
        }
        return res;
    }

    private String getString(TextView editText) {
        String result = editText.getText().toString();
        if (result.length() == 0) {
            return null;
        } else {
            return result;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String address = data.getStringExtra("address");
            addressEditText.setText(address);
        }
    }

    //    public static final String PARTNER = "2088711556531385";
    private static final String PARTNER = "2088811654507285";
    //    public static final String SELLER = "2488067698@qq.com";
    private static final String SELLER = "3142837216@qq.com";
    //    public static final String RSA_PRIVATE = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMFrdgZMa3rdYOx2 PeeZIgy1RyoOf8UxyWaTYaEtCuxxyy3rwCur99zwRXudbVOrxih6odzZ6cwVm/NF I8G3+TwGKA+GMpImG6sg81KCeDjVbhAV09pAIHaq7eTz3moEOUBc4qXBRxHhubJT QT1dGIxLbP6Ir8nGAIiUyKC/E7TlAgMBAAECgYEAv1tXqyeT9jxrFcZtvGHuI/B8 9Yjes/NrtAX/wvvTINX8E/R3bT13vaggtgmsDemV5Jpe5SbafcSrQ2SV2aPv+M/y Fs+l04l7A5fG90eSGTTxCHqo0W/qqspInYfN96Cipow/HeYE65RpZJ4Cc2Bb0ogX IHtQd7iN9sUHWgPRF4ECQQDmknq9dRXVJKKKuQ300HhPcVxVbs0F2qb0jwHp7h+5 VpuC31HB+LcDukyx4j8uqyF29lOze3btUBjPygj8/wIRAkEA1sAX8JuMmg/JABBH Ac8S9bcYi4WX24VcRqzwCPQbRA/CrnLiA2KooK9AE5NPcS45pHAQQUwYoO66SSze 5N5xlQJBAKDAAPizd7w5JWVn7TYAXdCtLP2XGTN6pKmeRmxMiyuRGSyd+4crmpTr vurJ3NjxkIw64lIgwuJi1FmR9sBEHbECQAKK872dmeSZG0As8SpMUWUnbdr5Efs/ cQBFO/JfMZN0vFFketifam+8o32X2PD2IyiXSxn61K/TI9GJ/nmnSKECQGaiFmuy 64VMz4Cxuuq8gU3l0SNDLRXgGFvOIZFkf+wzQMNMKbM2jN8aQE1GbAGbY/gu74W7 RaVLrmkX4z7gEHE=";
    private static final String RSA_PRIVATE = "MIICWwIBAAKBgQCxHciEJgtkoAFtUAZphXj2SBECBCHSpkLtNYs7wVFZVIgcjclS moIm9pcC59N6R3YX9aroOwtiHWyqN/yesKUYpkurWjR/AbeKY1GfHGhiwrRnSxky MGlAwA4w8clOc8oK68variL37DY/M3j7OZqcEFR6RQ7APjKfG6erQcMjCwIDAQAB AoGALUzKzlHUtCXgd47yNOb4azU/dF6OMAxqLbV2KRBmuAxQy8zP4xq1kzWaphmZ EztXzwT8c202mh+gfWDYcdYIQ3rDpobkhpiIMCRky+zW7fxtQ/aFfFbfv9FCPZM/ Lpt7UmJFzfTk6Ad2cDBx8dkTk5QM7TfCgCPAyhgDsYKVg+ECQQDY4B8QEZBEuxEY A0Yi27fbrVx7aVxHzqWC+7Xv72Whaqx8GEbuJpKLW4UDHSVdcrmbdISlzQE23gPQ dgi3InoDAkEA0RF7FZUeX2n/tw4ylb63/FQ5ZWXPrAAwf3oL+6OSVoMzV9e9QBRj 97XdTW+QK6yIYltP/r7kSj2zMsqc6rzoWQJAJQbWptKo0+MwPu5IKiljEYFemb9a PvQ788nvvQAdVNq2ihVG/t/dAyfj5K00NOkiYTUadIg0nd53vj54rHOZawJAXctz 5vjhiXjqqluKQjgwHtpCbcVBaC8lkutUWO7HhlySOkSluQvs1YMX59e3XICpJ0dE GkvV66DGtnDD+WQK8QJAPhmEuJLMRWesk4wvmejzUSvd5TnVCHCnt3xScDr/yxWh 0qUQ1G8iXZLqS2+I4jZ/75js0vsDiNAaYYusNNiqEg==";
    //public static final String RSA_PUBLIC = "";

    private static final int SDK_PAY_FLAG = 1;

    private static final int SDK_CHECK_FLAG = 2;

    private Handler mHandler;

    private void changeOrderStatus(String status) {
        RequestParameter rp = new RequestParameter(false);
        String urlApi = null;
        try {
            urlApi = Constants.CHANGE_ORDER_STATUS + "?order_number=" + orderNumber + "&order_flag="
                    + URLEncoder.encode(status, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        rp.setUrl(urlApi);

        new AsyncHttpPostForUploadOrder(rp, new RequestResultCallback() {
            @Override
            public void onSuccess(String str) {
            }

            @Override
            public void onFail(Exception e) {
                e.printStackTrace();
            }
        }).execute();
    }


    /**
     * call alipay sdk pay. 调用SDK支付
     */
    private void pay() {
        String price = (String) this.price.getTag();
        float price_i = Float.valueOf(price);
        String orderInfo = getOrderInfo(getString(nameEditText), "哇陶DIY瓷器", String.valueOf(price_i * number_t));
//        String orderInfo = getOrderInfo(getString(nameEditText), "哇陶DIY瓷器", "0.01");
        String sign = sign(orderInfo);
        try {
            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + getSignType();

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(BuyActivity.this);
                // 调用支付接口
                String result = alipay.pay(payInfo);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * check whether the device has authentication alipay account.
     * 查询终端设备是否存在支付宝认证账户
     */
    public void check(View v) {
        Runnable checkRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask payTask = new PayTask(BuyActivity.this);
                boolean isExist = payTask.checkAccountIfExist();

                Message msg = new Message();
                msg.what = SDK_CHECK_FLAG;
                msg.obj = isExist;
                mHandler.sendMessage(msg);
            }
        };

        Thread checkThread = new Thread(checkRunnable);
        checkThread.start();

    }

    /**
     * create the order info. 创建订单信息
     */
    private String getOrderInfo(String subject, String body, String price) {
        // 合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        String notifyUrl = Constants.CHANGE_ORDER_STATUS + "?order_number=" + orderNumber + "&order_flag=已支付";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + notifyUrl
                + "\"";

        // 接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值
        // orderInfo += "&paymethod=\"expressGateway\"";
        return orderInfo;
    }

    private String getTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);

        return key;
    }


    /**
     * get the out_trade_no for an order. 获取外部订单号
     */
    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();

        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    private String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }

}