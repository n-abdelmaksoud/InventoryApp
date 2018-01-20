
package com.example.littleprincess.inventory;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.littleprincess.inventory.data_entry.DataContract.ProductsEntry;
import com.squareup.picasso.Picasso;

/**
 * Created by Little Princess on 12/4/2017.
 */

public class RecyclerViewCustomAdapter extends RecyclerView.Adapter<RecyclerViewCustomAdapter.CustomViewHolder> {
    private static final String TAG = RecyclerViewCustomAdapter.class.getSimpleName();
    private Activity activity;
    private int mCurrentActivityState;
    private CustomCursorAdapter cursorAdapter;
    private Cursor cursor;
    private RecyclerViewItemListener recyclerViewItemListener;

    RecyclerViewCustomAdapter(final Activity c, int type) {
        activity=c;
        mCurrentActivityState=type;
        cursorAdapter = new CustomCursorAdapter();
        if (activity instanceof RecyclerViewItemListener) {
            recyclerViewItemListener = (RecyclerViewItemListener) activity;
        }
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = cursorAdapter.newView(activity, cursor, parent);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int itemWidth = displaymetrics.widthPixels / 2;
        holder.itemView.getLayoutParams().width = itemWidth;


        if (cursorAdapter.getCursor().moveToPosition(position)) {
            cursorAdapter.bindView(holder.itemView, activity, cursorAdapter.getCursor());
            String[] itemTag = new String[]{cursorAdapter.getItemName(), String.valueOf(cursorAdapter.getItemID())};
            holder.itemView.setTag(itemTag);
        }

    }

    @Override
    public int getItemCount() {
        return cursorAdapter.getCount();
    }

    void activityCursorLoaderFinished(Cursor data, int state) {
        mCurrentActivityState=state;
        cursorAdapter.swapCursor(data);
        notifyDataSetChanged();
    }

    void activityCursorLoaderReset(int state) {
        mCurrentActivityState=state;
        cursorAdapter.swapCursor(null);
        notifyDataSetChanged();
    }

    interface RecyclerViewItemListener {
        void onItemClick(String[] tag);

        void onItemLongClick(String[] tag);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder{
        CustomViewHolder(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerViewItemListener.onItemClick((String[]) itemView.getTag());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    recyclerViewItemListener.onItemLongClick((String[]) itemView.getTag());
                    return true;
                }
            });
        }
    }

    private class CustomCursorAdapter extends CursorAdapter {

        long id;
        String name;

        CustomCursorAdapter() {
            super(activity, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view;
            switch (mCurrentActivityState) {
                case ProductsEntry.STATE_CATEGORY:
                    view = LayoutInflater.from(activity).inflate(R.layout.rc_category_layout, parent, false);
                    return view;
                case ProductsEntry.STATE_ITEM:
                    view = LayoutInflater.from(activity).inflate(R.layout.rc_item_layout, parent, false);
                    return view;
                default:
                    return null;
            }
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            switch (mCurrentActivityState) {
                case ProductsEntry.STATE_CATEGORY:
                    bindCategoryView(view, context, cursor);
                    break;
                case ProductsEntry.STATE_ITEM:
                    bindItemsView(view, context, cursor);
                    break;
            }
        }

        private void bindCategoryView(View view, Context context, Cursor cursor) {
            TextView textView = view.findViewById(R.id.category_text_view);
            ImageView imageView = view.findViewById(R.id.category_image_view);

            int columnImageIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_IMAGE);
            int columnNameIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_NAME);
            int columnIDIndex = cursor.getColumnIndex(ProductsEntry._ID);
            name = cursor.getString(columnNameIndex);
            id = cursor.getLong(columnIDIndex);
            textView.setText(name);

            Uri imageUri = Uri.parse(cursor.getString(columnImageIndex));
            if (imageUri != null) {
                Log.i(TAG, "image uri:" + imageUri.toString());
                Picasso.with(activity).load(imageUri).placeholder(R.drawable.loading_placeholder).error(R.drawable.unavailable).resizeDimen(R.dimen.category_image_size_width, R.dimen.category_image_size_height).onlyScaleDown().centerCrop().into(imageView);
            }
        }

        private void bindItemsView(View view, Context context, Cursor cursor) {
            double sellPrice;
            int stock;
            int sale;
            double newSellPrice;

            TextView nameTextView = view.findViewById(R.id.item_name_text_view);
            ImageView imageView = view.findViewById(R.id.item_image_view);
            TextView stockTextView = view.findViewById(R.id.item_stock_text_view);
            TextView sellPriceTextView = view.findViewById(R.id.item_sell_price_text_view);
            TextView saleLabelTextView = view.findViewById(R.id.item_sale_label_text_view);
            TextView newSellPriceTextView = view.findViewById(R.id.item_sell_price_after_sale_text_view);

            int columnImageIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_IMAGE);
            int columnNameIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_NAME);
            int columnIDIndex = cursor.getColumnIndex(ProductsEntry._ID);
            int columnSaleIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_SALE_PERCENTAGE);
            int columnStockIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_IN_STOCK);
            int columnSellPriceIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_SELL_PRICE);
            name = cursor.getString(columnNameIndex);
            id = cursor.getLong(columnIDIndex);
            sellPrice = cursor.getDouble(columnSellPriceIndex);
            stock = cursor.getInt(columnStockIndex);
            sale = cursor.getInt(columnSaleIndex);

            if (sale == 0) {
                saleLabelTextView.setVisibility(View.INVISIBLE);
                sellPriceTextView.setPaintFlags(sellPriceTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                sellPriceTextView.setTextColor(ContextCompat.getColor(context, R.color.itemDetailsColor));
                newSellPriceTextView.setVisibility(View.GONE);
            } else {
                saleLabelTextView.setVisibility(View.VISIBLE);
                String saleString = String.format(context.getString(R.string.item_sale_text_view), sale);
                saleLabelTextView.setText(saleString);

                sellPriceTextView.setPaintFlags(sellPriceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                sellPriceTextView.setTextColor(ContextCompat.getColor(context, R.color.item_old_price));
                newSellPrice = sellPrice - (sellPrice * sale) / 100;
                String newSellString = String.format(context.getString(R.string.item_sell_price_text_view), newSellPrice);
                newSellPriceTextView.setVisibility(View.VISIBLE);
                newSellPriceTextView.setText(newSellString);

            }
            String sellString = String.format(context.getString(R.string.item_sell_price_text_view), sellPrice);
            sellPriceTextView.setText(sellString);


            String stockString = String.format(context.getString(R.string.item_stock_text_view), stock);
            stockTextView.setText(stockString);

            Uri imageUri = Uri.parse(cursor.getString(columnImageIndex));
            if (imageUri != null) {
                Log.i(TAG, "image uri:" + imageUri.toString());
                Picasso.with(activity).load(imageUri).placeholder(R.drawable.loading_placeholder).error(R.drawable.unavailable).resizeDimen(R.dimen.category_image_size_width, R.dimen.category_image_size_height).onlyScaleDown().centerCrop().into(imageView);
            }

            nameTextView.setText(name);


        }

        private long getItemID() {
            return id;
        }

        private String getItemName() {
            return name;
        }
    }


}
