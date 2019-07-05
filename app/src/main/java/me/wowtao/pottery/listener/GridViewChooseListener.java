package me.wowtao.pottery.listener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import me.wowtao.pottery.R;
import me.wowtao.pottery.activity.DecorateActivity;
import me.wowtao.pottery.activity.ShapeActivity;
import me.wowtao.pottery.adapter.GeneralGridViewAdapter;
import me.wowtao.pottery.fragment.ShapeFragment;
import me.wowtao.pottery.utils.GLManager;

public class GridViewChooseListener implements OnItemClickListener {

    private final Context context;
    private ShapeFragment fragment;

    public GridViewChooseListener(ShapeFragment fragment, Context context) {
        this.fragment = fragment;
        this.context = context;
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int position,
                            long id) {
        ((GeneralGridViewAdapter) adapterView.getAdapter()).choose(view);
        doClick(position);
    }

    private void doClick(final int position) {
        GLManager.setIsFix(true);
        fragment.loadMiji(position);
        new AlertDialog.Builder(context).setMessage("即将进入烧制!").setPositiveButton("烧制成瓷", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(fragment.getActivity(), DecorateActivity.class);
                ((ShapeActivity) fragment.getActivity()).getShapeFragment().needSave = false;
                fragment.getActivity().overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
                fragment.startActivity(intent);
            }
        }).setNegativeButton("继续拉坯", null).setCancelable(false).show();
    }
}

