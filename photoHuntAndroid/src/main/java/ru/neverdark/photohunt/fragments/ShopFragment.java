package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;

public class ShopFragment extends UfoFragment {
    private View mView;
    private Context mContext;
    private boolean mIsDataLoaded;
    private ListView mShopList;
    private ListView mMyItemsList;

    @Override
    public void bindObjects() {
        mShopList = (ListView) mView.findViewById(R.id.shop_shop_tab);
        mMyItemsList = (ListView) mView.findViewById(R.id.shop_myitems_tab);
    }

    @Override
    public void setListeners() {
        mShopList.setOnItemClickListener(new ListItemClickListener());
        mMyItemsList.setOnItemClickListener(new ListItemClickListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.shop_fragment, container, false);
        mContext = mView.getContext();
        mIsDataLoaded = false;
        bindObjects();
        setListeners();
        getActivity().setTitle(R.string.shop);
        initTabs();
        return mView;
    }

    private void initTabs() {
        TabHost tabHost = (TabHost) mView.findViewById(R.id.shop_tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("shopTab");
        tabSpec.setIndicator(getString(R.string.shop));
        tabSpec.setContent(R.id.shop_shop_tab);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("myItemsTab");
        tabSpec.setIndicator(getString(R.string.my_items));
        tabSpec.setContent(R.id.shop_myitems_tab);
        tabHost.addTab(tabSpec);
    }

    private class ListItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    }
}
