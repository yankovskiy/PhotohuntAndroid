package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.GoodsAdapter;
import ru.neverdark.photohunt.adapters.MyItemsAdapter;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

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

    @Override
    public void onResume() {
        super.onResume();

        if (!mIsDataLoaded) {
            String user = Settings.getUserId(mContext);
            String password = Settings.getPassword(mContext);
            try {
                if (user.length() == 0 || password.length() == 0) {
                    throw new ToastException(R.string.error_not_authorized);
                }

                RestService service = new RestService(user, password);
                service.getShopApi().getShop(new GetShopHandler(mView));
            } catch (ToastException e) {
                e.show(mContext);
            }
        }
    }

    private class GetShopHandler extends CallbackHandler<RestService.ShopData> {
        public GetShopHandler(View view) {
            super(view, R.id.shop_hide_when_loading, R.id.shop_loading_progress);
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            Response response = error.getResponse();
            try {
                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 401) {
                    throw new ToastException(R.string.error_wrong_password);
                }

                if (response.getStatus() == 403) {
                    RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
                    throw new ToastException(err.error);
                }

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }
        }

        @Override
        public void success(RestService.ShopData data, Response response) {
            if (data != null) {
                if (data.shop_items != null) {
                    GoodsAdapter goodsAdapter = new GoodsAdapter(mContext, R.layout.shop_goods_list_item, data.shop_items);
                    mShopList.setAdapter(goodsAdapter);
                }

                if (data.my_items != null) {
                    MyItemsAdapter myItemsAdapter = new MyItemsAdapter(mContext, R.layout.shop_myitems_list_item, data.my_items);
                    mMyItemsList.setAdapter(myItemsAdapter);
                }

                mIsDataLoaded = true;
            }

            super.success(data, response);
        }
    }
}
