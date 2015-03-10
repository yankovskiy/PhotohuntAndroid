package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.GoodsAdapter;
import ru.neverdark.photohunt.adapters.MyItemsAdapter;
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class ShopFragment extends UfoFragment {
    private View mView;
    private Context mContext;
    private boolean mIsDataLoaded;
    private ListView mShopList;
    private ListView mMyItemsList;
    private int mUserMoney;
    private int mUserDc;

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

    private void updateBalance(int money, int dc) {
        mUserMoney = money;
        mUserDc = dc;
        String balance = String.format(Locale.US, "%s: %d MON / %d DC", getString(R.string.balance), money, dc);
        ((UfoFragmentActivity)getActivity()).getSupportActionBar().setSubtitle(balance);
    }

    @Override
    public void onDetach() {
        ((UfoFragmentActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        super.onDetach();
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

    public static ShopFragment getInstance() {
        ShopFragment fragment = new ShopFragment();
        return fragment;
    }

    private class ListItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.enter();
            Log.variable("id", String.valueOf(view.getId()));
            switch (parent.getId()) {
                case R.id.shop_shop_tab:
                    // TODO сделать проверку на баланс, отображать диалог подтверждения только если денег достаточно
                    RestService.Goods goods = (RestService.Goods) mShopList.getAdapter().getItem(position);
                    ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
                    dialog.setTitle(R.string.confirm_buy_title);
                    dialog.setMessage(goods.name);
                    dialog.setCallback(new ConfirmPurchaseListener(goods));
                    dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
                    break;
                case R.id.shop_myitems_tab:
                    RestService.Item item = (RestService.Item) mMyItemsList.getAdapter().getItem(position);
                    // TODO что делать с выбранными предметами в своих покупках
                    break;
            }
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
                service.getShopApi().getShop(new GetShopListener(mView));
            } catch (ToastException e) {
                e.show(mContext);
            }
        }
    }

    private class GetShopListener extends CallbackHandler<RestService.ShopData> {
        public GetShopListener(View view) {
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

                updateBalance(data.money, data.dc);
                mIsDataLoaded = true;
            }

            super.success(data, response);
        }
    }

    private class ConfirmPurchaseListener implements OnCallback, ConfirmDialog.OnPositiveClickListener {

        private final RestService.Goods mGoods;

        public ConfirmPurchaseListener(RestService.Goods goods) {
            this.mGoods = goods;
        }
        @Override
        public void onPositiveClickHandler() {
            String user = Settings.getUserId(mContext);
            String password = Settings.getPassword(mContext);
            try {
                if (user.length() == 0 || password.length() == 0) {
                    throw new ToastException(R.string.error_not_authorized);
                }

                RestService service = new RestService(user, password);
                service.getShopApi().buyItem(mGoods.id, new BuyItemListener(mGoods));
            } catch (ToastException e) {
                e.show(mContext);
            }
        }
    }

    private class BuyItemListener implements Callback<Void> {
        private final RestService.Goods mGoods;

        public BuyItemListener(RestService.Goods goods) {
            this.mGoods = goods;
        }

        @Override
        public void success(Void data, Response response) {
            RestService.Item item = new RestService.Item(this.mGoods);
            MyItemsAdapter adapter = (MyItemsAdapter) mMyItemsList.getAdapter();
            if (adapter == null) {
                List<RestService.Item> items = new ArrayList<>();
                items.add(item);
                adapter = new MyItemsAdapter(mContext, R.layout.shop_myitems_list_item, items);
                mMyItemsList.setAdapter(adapter);
            } else {
                // Если уже есть купленный предмет, увеличиваем количество
                RestService.Item item1 = adapter.getItemByServiceName(this.mGoods.service_name);
                if (item1 != null) {
                    item1.count++;
                } else {
                    adapter.add(item);
                }

                adapter.notifyDataSetChanged();
            }

            // если купили аватар
            // аватар вечный, вторая покупка не требуется, убираем из списка магазина
            if (this.mGoods.service_name.equals(RestService.Item.AVATAR)) {
                GoodsAdapter goodsAdapter = (GoodsAdapter) mShopList.getAdapter();
                goodsAdapter.remove(this.mGoods);
                goodsAdapter.notifyDataSetChanged();
            }

            updateBalance(mUserMoney - this.mGoods.price_money, mUserDc - this.mGoods.price_dc);
            Common.showMessage(mContext, R.string.purchase_success);
        }

        @Override
        public void failure(RetrofitError error) {
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
    }
}
