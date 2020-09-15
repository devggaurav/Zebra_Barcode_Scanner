package com.gc.myscanner

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.ArrayList

class ProductScanningAdapter(ctx: Context?, list: ArrayList<ScanningProductsModel>?) :
    BaseAdapter<ScanningProductsModel, ProductScanningAdapter.ScanningViewHolder>(ctx, list) {


    var productList: ArrayList<ScanningProductsModel>? = list
    var mContext: Context? = ctx

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductScanningAdapter.ScanningViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ScanningViewHolder(inflater.inflate(R.layout.item_scanned_barcode, parent, false))
    }

    override fun getItemCount(): Int {
        return productList!!.size
    }

    override fun onBindViewHolder(
        holder: ProductScanningAdapter.ScanningViewHolder,
        position: Int
    ) {

        val product = productList!![position]

        holder.productName.setText(product.name)







    }

    class ScanningViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val productName: TextView = itemView.findViewById(R.id.txt_product)
        val mainCard: MaterialCardView = itemView.findViewById(R.id.cv_container)


    }
}