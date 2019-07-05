package me.wowtao.pottery.fragment;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import me.wowtao.pottery.R;
import me.wowtao.pottery.network.DataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ac on 9/6/15.
 */
public class OrderFragment extends Fragment {
    private List<OrderAdapter.Order> orders = new ArrayList<>();
    private OrderAdapter adapter = new OrderAdapter(orders);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_order, container, false);
        ListView orderListView = (ListView) root.findViewById(R.id.orders);
        orderListView.setAdapter(adapter);
        orderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            }
        });
        getOrderRemote();

        return root;
    }

    private void getOrderRemote() {
        Map<String, String> params = new HashMap<>();

        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String simSerialNumber = telephonyManager.getSimSerialNumber();
        if (simSerialNumber == null) {
            simSerialNumber = telephonyManager.getDeviceId();
        }

        if (simSerialNumber == null) {
            simSerialNumber = Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        params.put(DataProvider.KEY_USER_NAME, simSerialNumber);
        DataProvider
                .getInstance(getActivity())
                .getRequestQueue()
                .add(new DataProvider.CustomRequest.CustomGetRequest(
                        DataProvider.buildGetURL(DataProvider.GET_ORDER_LIST, params), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            response = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
                            JsonElement je = new JsonParser().parse(response);
                            JsonElement dataJE = je.getAsJsonObject().get("data");
                            List<OrderAdapter.Order> orders = new Gson().fromJson(dataJE, new TypeToken<List<OrderAdapter.Order>>() {
                            }.getType());
                            OrderFragment.this.orders.clear();
                            for (OrderAdapter.Order o : orders) {
                                OrderFragment.this.orders.add(o);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "网络错误，请检查联网设置", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }

                    }
                }, new DataProvider.BaseErrorListener()));

    }

    private class OrderAdapter extends BaseAdapter {

        private final List<Order> orders;

        class Order {
            String id;
            String order_number;
            String pottery_name;
            String price;
            String image;
            String name;
            String order_flag;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            String getOrder_number() {
                return order_number;
            }

            String getPottery_name() {
                return pottery_name;
            }

            String getPrice() {
                return price;
            }

            public String getImage() {
                return image;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            String getOrder_flag() {
                return order_flag;
            }

        }

        OrderAdapter(List<Order> orders) {
            if (orders == null) {
                this.orders = new ArrayList<>();
            } else {
                this.orders = orders;
            }
        }

        @Override
        public int getCount() {
            return orders.size();
        }

        @Override
        public Object getItem(int i) {
            return orders.get(i);
        }

        @Override
        public long getItemId(int i) {
            return Long.parseLong(orders.get(i).getId());
        }

        class ViewHolder {
            TextView orderNumber;
            TextView name;
            TextView price;
            TextView status;
            NetworkImageView picture;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater li = LayoutInflater.from(getActivity());
                view = li.inflate(R.layout.item_order, viewGroup, false);
                ViewHolder vh = new ViewHolder();
                vh.orderNumber = (TextView) view.findViewById(R.id.order_number);
                vh.name = (TextView) view.findViewById(R.id.pottery_name);
                vh.price = (TextView) view.findViewById(R.id.pottery_price);
                vh.status = (TextView) view.findViewById(R.id.order_status);
                vh.picture = (NetworkImageView) view.findViewById(R.id.order_image);
                view.setTag(vh);
            }

            ViewHolder vh = (ViewHolder) view.getTag();
            Order order = (Order) getItem(i);
            vh.orderNumber.setText(order.getOrder_number());
            vh.name.setText(order.getPottery_name());
            final String text = order.getPrice() + "元";
            vh.price.setText(text);
            vh.status.setText(order.getOrder_flag());
            vh.picture.setImageUrl(order.getImage(), DataProvider.getInstance(getActivity()).getImageLoader());

            return view;
        }
    }
}
