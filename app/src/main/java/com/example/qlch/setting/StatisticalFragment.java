package com.example.qlch.setting;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.example.qlch.Oder.Adapter.ListOderAdapter;
import com.example.qlch.Oder.DetailReceiptFragment;
import com.example.qlch.R;
import com.example.qlch.base.BaseFragment;
import com.example.qlch.databinding.DialogChooseFunctionFilterStatisticBinding;
import com.example.qlch.databinding.DialogChooseTimeStatisticBinding;
import com.example.qlch.databinding.FragmentOderStatisticsBinding;
import com.example.qlch.model.Receipt;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticalFragment extends BaseFragment implements ListOderAdapter.OnClickListener {
    private FragmentOderStatisticsBinding binding = null;
    private SettingViewModel viewModel;
    private ListOderAdapter adapter;
    private int lastSelectedYear;
    private int lastSelectedMonth;
    private int lastSelectedDayOfMonth;

    public StatisticalFragment() {
        // Required empty public constructor
    }

    public static StatisticalFragment newInstance() {
        StatisticalFragment fragment = new StatisticalFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Calendar c = Calendar.getInstance();
        this.lastSelectedYear = c.get(Calendar.YEAR);
        this.lastSelectedMonth = c.get(Calendar.MONTH);
        this.lastSelectedDayOfMonth = c.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//         Inflate the layout for this fragment
        binding = FragmentOderStatisticsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(SettingViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window window = getActivity().getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(getActivity().getColor(R.color.white));
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
        getPaidOder();

    }

    @Override
    public void listening() {
        binding.icBack.setOnClickListener(v -> backStack());
        binding.tvFilterTime.setOnClickListener(v -> dialogFunctionPickDate(requireContext()));


        binding.tvFilterFunction.setOnClickListener(v ->{
            dialogFunctionFilterOder(getContext());
        });

    }


    @Override
    public void initObSever() {


    }

    @Override
    public void initView() {
    }



    private void dialogFunctionFilterOder(Context context) {
        final Dialog dialog = new Dialog(context);
        DialogChooseFunctionFilterStatisticBinding bindingDialog = DialogChooseFunctionFilterStatisticBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(bindingDialog.getRoot());
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        window.setAttributes(layoutParams);
        bindingDialog.dialogChooserFunction.setTranslationY(150);
        bindingDialog.dialogChooserFunction.animate().translationYBy(-150).setDuration(400);
        if( binding.tvFilterFunction.getText().toString().equals("Theo ngày hoàn thành")){
            bindingDialog.icFilter1.setVisibility(View.VISIBLE);
            bindingDialog.icFilter2.setVisibility(View.GONE);
        }else {
            bindingDialog.icFilter1.setVisibility(View.GONE);
            bindingDialog.icFilter2.setVisibility(View.VISIBLE);
        }

        bindingDialog.layoutFilterCompleteOder.setOnClickListener(ic ->{
            binding.tvFilterFunction.setText("Theo ngày hoàn thành");
            binding.tvFilterTime.setText("Tất cả");
            getPaidOder();

            dialog.cancel();
        });
        bindingDialog.layoutFilterCancelOder.setOnClickListener(ic ->{
            binding.tvFilterFunction.setText("Theo ngày hủy");
            binding.tvFilterTime.setText("Tất cả");
            getCancelOder();
            dialog.cancel();
        });



        dialog.show();

    }


    private void dialogFunctionPickDate(Context context) {
        final Dialog dialog = new Dialog(context);
        DialogChooseTimeStatisticBinding bindingDialog = DialogChooseTimeStatisticBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(bindingDialog.getRoot());
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        window.setAttributes(layoutParams);
        bindingDialog.dialogChooserFunction.setTranslationY(150);
        bindingDialog.dialogChooserFunction.animate().translationYBy(-150).setDuration(400);

        bindingDialog.layoutChooserTimeStart.setOnClickListener(v -> {
            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                    bindingDialog.tvTimeStart.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    bindingDialog.tvTimeStart.setVisibility(View.VISIBLE);
                    lastSelectedYear = year;
                    lastSelectedMonth = monthOfYear;
                    lastSelectedDayOfMonth = dayOfMonth;
                }
            };
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), dateSetListener, lastSelectedYear, lastSelectedMonth,
                    lastSelectedDayOfMonth);
            datePickerDialog.show();
        });
        bindingDialog.layoutChooserTimeEnd.setOnClickListener(tv -> {
            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    bindingDialog.tvTimeEnd.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    bindingDialog.tvTimeEnd.setVisibility(View.VISIBLE);
                    lastSelectedYear = year;
                    lastSelectedMonth = monthOfYear;
                    lastSelectedDayOfMonth = dayOfMonth;
                }
            };
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), dateSetListener, lastSelectedYear, lastSelectedMonth,
                    lastSelectedDayOfMonth);
            datePickerDialog.show();
        });

        bindingDialog.btnFilter.setOnClickListener(v -> {
            if (bindingDialog.tvTimeStart.getVisibility() == View.GONE){
                Toast.makeText(context, "Hẫy chọn ngày bắt đầu", Toast.LENGTH_SHORT).show();
            }else if(bindingDialog.tvTimeEnd.getVisibility() == View.GONE){
                Toast.makeText(context, "Hẫy chọn ngày kết thúc", Toast.LENGTH_SHORT).show();
            }else {
                if( binding.tvFilterFunction.getText().toString().equals("Theo ngày hoàn thành")){
                    viewModel.getReceiptByDate(bindingDialog.tvTimeStart.getText().toString(), bindingDialog.tvTimeEnd.getText().toString());
                    adapter.notifyDataSetChanged();
                }else {
                    viewModel.getReceiptCancelByDate(bindingDialog.tvTimeStart.getText().toString(), bindingDialog.tvTimeEnd.getText().toString());
                    adapter.notifyDataSetChanged();
                }
                binding.tvFilterTime.setText(bindingDialog.tvTimeStart.getText().toString()+ " đến " +bindingDialog.tvTimeEnd.getText().toString());
                dialog.cancel();
            }

        });

        dialog.show();
    }

    public void getPaidOder(){
        viewModel.getAllReceipt();
        viewModel.liveDateGetAllReceipt.observe(getViewLifecycleOwner(), new Observer<List<Receipt>>() {
            @Override
            public void onChanged(List<Receipt> receipts) {
                binding.tvOrderNumber.setText(receipts.size() + "");
                Double money = 0.0;
                for (Receipt receipt : receipts) {
                    money += receipt.getMoney();
                }
                Locale locale = new Locale("en", "EN");
                NumberFormat numberFormat = NumberFormat.getInstance(locale);
                String strMoney = numberFormat.format(money);
                binding.tvTotalOderValue.setText(strMoney);
                adapter = new ListOderAdapter((ArrayList<Receipt>) receipts,StatisticalFragment.this,0);
                LinearLayoutManager layoutManager  = new LinearLayoutManager(getContext());
                layoutManager.setStackFromEnd(true);
                layoutManager.setReverseLayout(true);
                binding.recVListOder.setLayoutManager(layoutManager);
                binding.recVListOder.setAdapter(adapter);
            }
        });

        viewModel.liveDateGetReceipt.observe(getViewLifecycleOwner(), new Observer<List<Receipt>>() {
            @Override
            public void onChanged(List<Receipt> receipts) {
                if(receipts.size() == 0){
                    binding.layoutNotificationNullData.setVisibility(View.VISIBLE);
                    binding.tvOrderNumber.setText("0");
                    binding.tvTotalOderValue.setText("0");
                    binding.viewHeader.setVisibility(View.GONE);
                    binding.recVListOder.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }else {
                    binding.layoutNotificationNullData.setVisibility(View.GONE);
                    binding.viewHeader.setVisibility(View.VISIBLE);
                    binding.recVListOder.setVisibility(View.VISIBLE);
                    binding.tvOrderNumber.setText(receipts.size() + "");
                    Double money = 0.0;
                    for (Receipt receipt : receipts) {
                        money += receipt.getMoney();
                    }
                    Locale locale = new Locale("en", "EN");
                    NumberFormat numberFormat = NumberFormat.getInstance(locale);
                    String strMoney = numberFormat.format(money);

                    binding.tvTotalOderValue.setText(strMoney);
                    adapter = new ListOderAdapter((ArrayList<Receipt>) receipts,StatisticalFragment.this,0);
                    LinearLayoutManager layoutManager  = new LinearLayoutManager(getContext());
                    layoutManager.setStackFromEnd(true);
                    layoutManager.setReverseLayout(true);
                    binding.recVListOder.setLayoutManager(layoutManager);
                    binding.recVListOder.setAdapter(adapter);
                }
            }
        });


    }
    public void getCancelOder(){
        adapter.notifyDataSetChanged();
        viewModel.getAllCancelReceipt();
        viewModel.liveDateGetAllCancelReceipt.observe(getViewLifecycleOwner(), new Observer<List<Receipt>>() {
            @Override
            public void onChanged(List<Receipt> receipts) {
                binding.tvOrderNumber.setText(receipts.size() + "");
                Double money = 0.0;
                for (Receipt receipt : receipts) {
                    money += receipt.getMoney();
                }
                Locale locale = new Locale("en", "EN");
                NumberFormat numberFormat = NumberFormat.getInstance(locale);
                String strMoney = numberFormat.format(money);
                binding.tvTotalOderValue.setText(strMoney);
                adapter = new ListOderAdapter((ArrayList<Receipt>) receipts,StatisticalFragment.this,0);
                LinearLayoutManager layoutManager  = new LinearLayoutManager(getContext());
                layoutManager.setStackFromEnd(true);
                layoutManager.setReverseLayout(true);
                binding.recVListOder.setLayoutManager(layoutManager);
                binding.recVListOder.setAdapter(adapter);
            }
        });

        viewModel.liveDateGetCancelReceipt.observe(getViewLifecycleOwner(), new Observer<List<Receipt>>() {
            @Override
            public void onChanged(List<Receipt> receipts) {
                if(receipts.size() == 0){
                    binding.layoutNotificationNullData.setVisibility(View.VISIBLE);
                    binding.tvOrderNumber.setText("0");
                    binding.tvTotalOderValue.setText("0");
                    binding.viewHeader.setVisibility(View.GONE);
                    binding.recVListOder.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }else {
                    binding.layoutNotificationNullData.setVisibility(View.GONE);
                    binding.viewHeader.setVisibility(View.VISIBLE);
                    binding.recVListOder.setVisibility(View.VISIBLE);
                    binding.tvOrderNumber.setText(receipts.size() + "");
                    Double money = 0.0;
                    for (Receipt receipt : receipts) {
                        money += receipt.getMoney();
                    }
                    Locale locale = new Locale("en", "EN");
                    NumberFormat numberFormat = NumberFormat.getInstance(locale);
                    String strMoney = numberFormat.format(money);

                    binding.tvTotalOderValue.setText(strMoney);
                    adapter = new ListOderAdapter((ArrayList<Receipt>) receipts,StatisticalFragment.this,0);
                    LinearLayoutManager layoutManager  = new LinearLayoutManager(getContext());
                    layoutManager.setStackFromEnd(true);
                    layoutManager.setReverseLayout(true);
                    binding.recVListOder.setLayoutManager(layoutManager);
                    binding.recVListOder.setAdapter(adapter);
                }
            }
        });
    }



    @Override
    public void onClickListener(Receipt receipt) {
        replaceFragment(new DetailReceiptFragment(receipt));
    }
}