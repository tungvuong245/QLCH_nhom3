package com.example.qlch.product;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import com.example.qlch.R;
import com.example.qlch.base.BaseFragment;
import com.example.qlch.databinding.FragmentDetailsProductBinding;
import com.example.qlch.databinding.LayoutFullImageProductBinding;
import com.example.qlch.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailProductFragment extends BaseFragment {

    private FragmentDetailsProductBinding binding = null;
    private Product dataProduct = null;


    public DetailProductFragment(Product product) {
        this.dataProduct = product;

    }

    public DetailProductFragment() {
    }

    public DetailProductFragment newInstance() {
        return new DetailProductFragment(dataProduct);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailsProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Log.d("TAG", "newInstance222: "+dataProduct.getId());
        listening();
        initObSever();
    }

    @Override
    public void loadData() {

    }
    @Override
    public void listening() {
        binding.btnDeleteProduct.setOnClickListener(v -> {
            dialogConfirmDelete(getContext());
        });
        binding.imgProduct.setOnClickListener(ic ->{
            StorageReference reference = FirebaseStorage.getInstance().getReference().child("imgProducts");
            reference.listAll().addOnSuccessListener(listResult -> {
                for (StorageReference files: listResult.getItems()){
                    if(files.getName().equals(dataProduct.getId())){
                        dialogFullImg(getContext(),binding.bgDetailsProduct);
                    }
                }
            });

        });
        binding.icEditProduct.setOnClickListener(v->{
            replaceFragment(new UpdateProductFragment(dataProduct));
        });
        binding.icBack.setOnClickListener(v->{
            backStack();
        });
    }

    @Override
    public void initObSever() {
        showDetailProduct();
    }

    @Override
    public void initView() {

    }

    private void dialogConfirmDelete(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Xóa sản phẩm");
        builder.setIcon(context.getDrawable(R.drawable.ic_delete));
        builder.setMessage("Bạn chắc chắn muốn xóa "+dataProduct.getNameProduct());
        builder.setCancelable(false);
        builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ẩn sản phẩm chứ k xóa trên readtime
                dataProduct.setHidden(false);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("list_product");
                reference.child(dataProduct.getId()).setValue(dataProduct).addOnCompleteListener(task->{
                    if (task.isSuccessful()){
                        notificationSuccessInput(getContext(),"Đã xóa sản phẩm!");
                        backStack();
                    }else {
                        notificationSuccessInput(getContext(),"Xóa không thành công!");

                    }
                });
            }
        });


        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context,"Đã hủy !",Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });
        AlertDialog sh = builder.create();
        sh.show();
    }

    private void showDetailProduct(){
        Locale locale = new Locale("en","EN");
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        StorageReference reference = FirebaseStorage.getInstance().getReference().child("imgProducts");
        reference.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference files: listResult.getItems()){
                if(files.getName().equals(dataProduct.getId())){
                    files.getDownloadUrl().addOnSuccessListener(uri -> {
                        if(getActivity() != null){
                            Glide.with(getActivity()).load(uri).into(binding.imgProduct);
                        }

                    });
                }
            }
        });

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
        reference2.child("list_product").child(dataProduct.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataProduct = snapshot.getValue(Product.class);
                if (dataProduct == null) {
                    return;
                }
                Double price = dataProduct.getPrice();
                String strPrice = numberFormat.format(price);
                binding.tvNameProduct.setText(dataProduct.getNameProduct());
                binding.tvDescribe.setText(dataProduct.getDescribe());
                binding.tvTypeProduct.setText(dataProduct.getTypeProduct().getNameType());
                binding.tvPriceProduct.setText(strPrice+" đ");
                binding.tvNoteProduct.setText(dataProduct.getNote());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void dialogFullImg(Context context, LinearLayout layout){
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Material_Light_NoActionBar);
        LayoutFullImageProductBinding binding = LayoutFullImageProductBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false);
        context.setTheme(R.style.Theme_Full_Img);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        layout.setAlpha(0.1f);
        binding.icCloseDialog.setOnClickListener(ic ->{
            dialog.cancel();
            layout.setAlpha(1f);
        });
        StorageReference reference = FirebaseStorage.getInstance().getReference().child("imgProducts");
        reference.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference files: listResult.getItems()){
                if(files.getName().equals(dataProduct.getId())){
                    files.getDownloadUrl().addOnSuccessListener(uri -> {
                        if(getActivity() != null){
                            Glide.with(getActivity()).load(uri).into(binding.imgFullProduct);
                        }

                    });
                }
            }
        });

        dialog.show();
    }
}