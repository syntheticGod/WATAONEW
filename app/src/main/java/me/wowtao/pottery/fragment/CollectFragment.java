package me.wowtao.pottery.fragment;
import me.wowtao.pottery.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.activity.PotteryFinishedActivity;
import me.wowtao.pottery.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ac on 9/6/15.
 *
 */
public class CollectFragment extends Fragment{
    private View root;
    private List<FileUtils.PotterySaved> data;
    private ArrayList<ImageView> dataImageView = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_collect, container, false);
        initData();
        initUI();
        return root;
    }

    private void initData() {
        data = FileUtils.getSavedPottery(getActivity());
    }

    private void initUI() {
        Wowtao.getGlManager().pushPottery();
        GridView displayLayout = (GridView) root.findViewById(R.id.collects);
        if (data.size() == 0) {
            root.findViewById(R.id.no_collect).setVisibility(View.VISIBLE);
            return;
        }
        for (FileUtils.PotterySaved ps : data) {
            if (ps.image != null) {
                ImageView iv = new ImageView(getActivity());
                iv.setImageBitmap(ps.image);
                iv.setTag(ps.fileName);
                iv.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), PotteryFinishedActivity.class);
                        intent.putExtra("fileName", (String) v.getTag());
                        intent.putExtra("fromcolloct", "yes");
                        getActivity().startActivity(intent);
                    }
                });
                dataImageView.add(iv);
            }
        }
        displayLayout.setAdapter(new BaseAdapter() {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return dataImageView.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public Object getItem(int position) {
                dataImageView.get(position);
                return null;
            }

            @Override
            public int getCount() {
                return dataImageView.size();
            }
        });
    }
}
