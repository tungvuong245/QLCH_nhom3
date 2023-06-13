package com.example.qlch.table;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.example.qlch.PushNotification.FMC;
import com.example.qlch.PushNotification.MyNotificator;
import com.example.qlch.R;
import com.example.qlch.base.BaseFragment;
import com.example.qlch.databinding.FragmentAddOderBinding;
import com.example.qlch.home.HomeFragment;
import com.example.qlch.maket.MarketFragment;
import com.example.qlch.model.Product;
import com.example.qlch.model.Receipt;
import com.example.qlch.model.Table;
import com.example.qlch.model.Token;
import com.example.qlch.table.adapter.OderAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailTableFragment extends BaseFragment {
    private FragmentAddOderBinding binding = null;
    private Table table;
    private ArrayList<String> getIdProduct = null;
    private ArrayList<Integer> getCountProduct = null;
    private String idTable = null;
    private Receipt receiptModel;
    private TableViewModel model = null;
    private String statusTable = "false";
    private Double totalMoney = 0.0;
    private ArrayList<String> listIdProduct;
    private ArrayList<Integer>listCountProduct = null;
    private MyNotificator myNotificator;


    List<String> listTk = new ArrayList<>();
    public DetailTableFragment(Table table) {
        this.table = table;
    }

    public DetailTableFragment() {
    }

    public static DetailTableFragment newInstance(Table table) {
        DetailTableFragment fragment = new DetailTableFragment(table);
        Bundle args = new Bundle();
        args.putSerializable("table", (Serializable) table);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddOderBinding.inflate(inflater, container, false);
        table = new Table();
        listIdProduct = new ArrayList<>();
        getCountProduct = new ArrayList<>();
        getIdProduct = new ArrayList<>();
        listCountProduct = new ArrayList<>();
        myNotificator = new MyNotificator();
        model = new ViewModelProvider(this).get(TableViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listening();
        initObSever();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void loadData() {
        model.liveDataGetAllToken();
        if (getArguments() != null) {
            if (getArguments().getSerializable("table") != null) {
                table = (Table) getArguments().getSerializable("table");
                idTable = String.valueOf(table.getId_table());
                if (getArguments().getStringArrayList("list_product_select") != null) {
                    getIdProduct = getArguments().getStringArrayList("list_product_select");
                    getCountProduct = getArguments().getIntegerArrayList("list_count_product");
                    listIdProduct.addAll(getIdProduct);
                    listCountProduct.addAll(getCountProduct);

                }
                statusTable = table.getStatus();
                binding.tvNameBill.setText(table.getName_table());
                if (table.getName_table() == null) {
                    binding.tvNameBill.setText("Bán mang về");
                    binding.btnPayOder.setVisibility(View.VISIBLE);
                    binding.btnSaveOder.setVisibility(View.GONE);
                }
            } else {
                binding.tvNameBill.setText("Bán mang về");
                binding.btnPayOder.setVisibility(View.VISIBLE);
                binding.btnSaveOder.setVisibility(View.VISIBLE);
            }
            model.listLiveData(listIdProduct);
        }

        binding.btnSaveOder.setOnClickListener(v -> {
            if (binding.listProductOder.getVisibility() == View.GONE) {
                notificationErrInput(getContext(), "Hãy chọn món !");
            } else {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("OderSave");
                String key = reference.push().getKey();
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String strDate = dateFormat.format(date);

                receiptModel = new Receipt(key, idTable, strDate, totalMoney, listIdProduct,listCountProduct, binding.edNoteOder.getText().toString(), true);
                reference.child(key).setValue(receiptModel);

                pushMessage("Poly Thông Báo", table.getName_table()+" đang có khách ngồi");
                model.setStatusTable(idTable, "true");
                replaceFragment(HomeFragment.newInstance());
            }
        });
        if (statusTable.equals("true")) {
            model.liveDataGetReceipt(idTable);
            model.liveDataGetCancelReceipt(idTable);
        }
        if (table.getName_table() != null) {
            model.liveDataGetReceipt.observe(getViewLifecycleOwner(), new Observer<Receipt>() {
                @Override
                public void onChanged(Receipt receipt) {
                    binding.btnSaveOder.setVisibility(View.GONE);
                    binding.btnPayOder.setVisibility(View.VISIBLE);
                    binding.cavCancelOder.setVisibility(View.VISIBLE);
                    receiptModel = receipt;
                    model.listLiveData(receipt.getListIdProduct());


                    model.listProductOder.observe(getViewLifecycleOwner(), new Observer<List<Product>>() {
                    @Override
                    public void onChanged(List<Product> products) {
                        if(products.size() != 0){
                            for(int i = 0; i < receiptModel.getListCountProduct().size(); i++){
                                for(int k = 0 ; k < products.size(); k++) {
                                    products.get(k).setIsClick(receiptModel.getListCountProduct().get(k));
                                }
                            }

                        }

                        OderAdapter adapter = new OderAdapter(products,0,getActivity());
                        int totalProduct = 0;
                        for(int i = 0; i < receipt.getListCountProduct().size(); i++){
                            totalProduct += receipt.getListCountProduct().get(i);
                        }
                        binding.tvAmountProduct.setText(String.valueOf(totalProduct));

                        Locale locale = new Locale("en", "EN");
                        NumberFormat numberFormat = NumberFormat.getInstance(locale);
                        String strMoney = numberFormat.format(receiptModel.getMoney());

                        binding.tvTotalOder.setText(strMoney);
                        binding.tvTotalAmount.setText(strMoney);
                        binding.listProductOder.setAdapter(adapter);

                    }
                });

                    binding.layoutAddProduct.setVisibility(View.GONE);
                    binding.edNoteOder.setEnabled(false);
                    binding.edNoteOder.setText(receipt.getNoteOder() + "");
                }
            });

            binding.btnCancelOder.setOnClickListener(btn ->{
                if (receiptModel != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Xác nhận hủy đơn hàng");
                    builder.setIcon(getContext().getDrawable(R.drawable.ic_cancelled));
                    builder.setMessage(getString( R.string.notification_cancel_oder));
                    builder.setCancelable(false);
                    builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            receiptModel.setStatusOder(false);
                            model.setStatusTable(idTable, "false");
                            model.liveDataCancelReceipt(receiptModel);
                            backStack();
                        }
                    });
                    builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getContext(),"Đã hủy !",Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }
                    });

                    AlertDialog sh = builder.create();
                    sh.show();

                }
            });
        }


        binding.btnPayOder.setOnClickListener(btn -> {
            if (binding.listProductOder.getVisibility() == View.GONE) {
                notificationErrInput(getContext(), "Hãy chọn món !");
            } else if (receiptModel != null) {
                model.setStatusTable(idTable, "false");
                pushMessage("Thông Báo Bàn Trống",table.getName_table()+" đang trống");
                model.liveDataPayReceipt(receiptModel);
                backStack();
            } else {
                DatabaseReference reference;
                reference = FirebaseDatabase.getInstance().getReference("PayReceipt");
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String strDate = dateFormat.format(date);
                String key = reference.push().getKey();
                Receipt receipt = new Receipt(key, "", strDate, totalMoney, listIdProduct,listCountProduct, binding.edNoteOder.getText().toString(), true);
                reference.child(key).setValue(receipt);
                notificationSuccessInput(getContext(), "Thanh toán thành công!");
                replaceFragment(MarketFragment.newInstance());
            }
        });




        model.liveDataGetReceipt.observe(getViewLifecycleOwner(), new Observer<Receipt>() {
            @Override
            public void onChanged(Receipt receipt) {
                binding.btnSaveOder.setVisibility(View.GONE);
                receiptModel = receipt;
                model.listLiveData(receipt.getListIdProduct());
                binding.layoutAddProduct.setVisibility(View.GONE);
            }
        });

        model.liveDataPayReceipt.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals("success")) {
                    notificationSuccessInput(getContext(), "Thanh toán thành công!");
                } else {
                    notificationErrInput(getContext(), "Thanh toán thất bại");
                }
            }
        });

        model.liveDataCancelReceipt.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals("cancel")) {
                    notificationSuccessInput(getContext(), "Đã hủy đơn!");
                } else {
                    notificationSuccessInput(getContext(), "Hủy đơn thất bại!");
                }
            }
        });





    }

    @Override
    public void listening() {
        binding.icBack.setOnClickListener(v -> backStack());
        binding.layoutAddProduct.setOnClickListener(v -> {
            FragmentListAllProductToOder productToOder = new FragmentListAllProductToOder();
            Bundle bundle = new Bundle();
            bundle.putSerializable("table", table);
            productToOder.setArguments(bundle);
            replaceFragment(productToOder);
        });
        binding.btnPayOder.setOnClickListener(v -> {
            model.liveDataPayReceipt(receiptModel);
            model.setStatusTable(idTable, "false");
        });

    }

    @Override
    public void initObSever() {
        model.listProductOder.observe(getViewLifecycleOwner(), new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                binding.layoutAddProduct.setVisibility(View.GONE);
                binding.listProductOder.setVisibility(View.VISIBLE);
                if(products.size() != 0){
                    for(int i = 0 ; i <listCountProduct.size(); i ++){
                        for(int k = 0; k < products.size() ; k++){
                            products.get(k).setIsClick(listCountProduct.get(k));
                        }
                    }
                }
                OderAdapter adapter = new OderAdapter(products,0,getActivity());
                int totalProduct = 0;
                for(int i = 0; i < listCountProduct.size(); i++){
                    totalProduct += listCountProduct.get(i);
                }
                binding.tvAmountProduct.setText(String.valueOf(totalProduct));

                for (Product product : products) {
                    totalMoney += (product.getPrice() * product.getIsClick());
                }

                Locale locale = new Locale("en", "EN");
                NumberFormat numberFormat = NumberFormat.getInstance(locale);
                String strMoney = numberFormat.format(totalMoney);

                binding.tvTotalOder.setText(strMoney);
                binding.tvTotalAmount.setText(strMoney);
                binding.listProductOder.setAdapter(adapter);

            }
        });


        model.oderTableStatus.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String aBoolean) {
                if (aBoolean == "true") {
                    notificationSuccessInput(getContext(), "Đặt bàn thành công!");

                }
            }
        });
    }

    @Override
    public void initView() {

    }
    public void pushMessage(String title, String message){
        model.liveDataListToken.observe(getViewLifecycleOwner(), new Observer<List<Token>>() {
            @Override
            public void onChanged(List<Token> tokens) {
                for (Token token: tokens
                     ) {
                    FMC.pushNotification(requireContext(),token.getToken(),title, message);
                }
            }
        });
    }
}