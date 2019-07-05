package me.wowtao.pottery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import me.wowtao.pottery.R;

public class GeneralGridViewAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ImageView chosenView;

    public void choose(View view) {
        if (chosenView != null) {
            chosenView.setImageDrawable(null);
        }
        chosenView = ((ViewHold) view.getTag()).imageView;
    }

    private final class ViewHold {
        ImageView imageView;
    }

    public GeneralGridViewAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 9;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static final int[] classicId = {R.drawable.xmj09, R.drawable.xmj08, R.drawable.xmj05, R.drawable.xmj04, R.drawable.xmj03, R.drawable.xmj02, R.drawable.xmj01, R.drawable.xmj07, R.drawable.xmj06};

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHold viewHold;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_sample, parent, false);
            viewHold = new ViewHold();
            viewHold.imageView = (ImageView) convertView.findViewById(R.id.sample_img);
            convertView.setTag(viewHold);
        } else {
            viewHold = (ViewHold) convertView.getTag();
        }

        viewHold.imageView.setBackgroundResource(classicId[position]);

        viewHold.imageView.setImageDrawable(null);
        return convertView;
    }

}
