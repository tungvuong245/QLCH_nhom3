package com.example.qlch.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.qlch.R;
import com.example.qlch.base.BaseFragment;
import com.example.qlch.databinding.FragmentProductBinding;
import com.example.qlch.model.Product;
import com.example.qlch.model.TypeProduct;
import com.example.qlch.product.Adapter.ProductAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductFragment extends BaseFragment implements  ProductAdapter.OnClickItemListener{
    public static final String TAG = ProductFragment.class.getName();
    private FragmentProductBinding bindProduct = null;
    private ArrayList<Product> listProduct;
    public ProductAdapter productAdapter = null;
    private TypeProduct typeProduct;


    public ProductFragment() {
    }

    public ProductFragment(TypeProduct typeProduct) {
        this.typeProduct = typeProduct;
    }

    public ProductFragment newInstance2() {
        return new ProductFragment(typeProduct);
    }

    public static ProductFragment newInstance() {
        ProductFragment fragment = new ProductFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bindProduct = FragmentProductBinding.inflate(inflater, container, false);
        return bindProduct.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (typeProduct == null) {
            bindProduct.tvNameTypeProduct.setText(R.string.text_type_product_1);
            listProduct = new ArrayList<>();
            getProduct();
        } else {
            bindProduct.tvNameTypeProduct.setText(typeProduct.getNameType());
            listProduct = new ArrayList<>();
            getFilterProduct();
        }
        productAdapter = new ProductAdapter(listProduct, ProductFragment.this,getActivity());
        LinearLayoutManager layoutManager  = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        bindProduct.listProduct.setLayoutManager(layoutManager);
        bindProduct.listProduct.setAdapter(productAdapter);
        listening();
        initObSever();

    }

    @Override
    public void loadData() {

    }

    @Override
    public void listening() {
        bindProduct.fabAddProduct.setOnClickListener(v -> {
            replaceFragment(new AddProductFragment().newInstance());
        });
        bindProduct.layoutType.setOnClickListener(layout -> {
            replaceFragment(new TypeProductFragment().newInstance());
        });

        bindProduct.searchViewProduct.clearFocus();
        bindProduct.searchViewProduct.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText, listProduct);
                return true;
            }
        });
        bindProduct.swiperRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (typeProduct == null) {
                    getProduct();
                } else {
                    getFilterProduct();
                }
                bindProduct.listProduct.setAdapter(productAdapter);
                bindProduct.swiperRefreshLayout.setRefreshing(false);
            }
        });

    }

    @Override
    public void initObSever() {

    }

    @Override
    public void initView() {


    }

    private void getProduct() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("list_product");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listProduct.clear();
                for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                    Product product = datasnapshot.getValue(Product.class);
                    if(product.isHidden()){
                        listProduct.add(product);
                    }
                }
                bindProduct.tvCountProduct.setText("Có " + listProduct.size() + " sản phẩm");
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product == null || listProduct == null || listProduct.isEmpty()) {
                    return;
                }
                for (int i = 0; i < listProduct.size(); i++) {
                    if (product.getId() == listProduct.get(i).getId()) {
                        listProduct.remove(listProduct.get(i));
                        break;
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void getFilterProduct() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("list_product");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listProduct.clear();
                for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                    Product product = datasnapshot.getValue(Product.class);
                    listProduct.add(product);
                }
                ArrayList<Product> listFilter = new ArrayList<>();
                for (Product product : listProduct) {
                    if (product.getTypeProduct().getId().equalsIgnoreCase(typeProduct.getId())) {
                        listFilter.add(product);
                    }
                }
                listProduct.retainAll(listFilter);
                bindProduct.tvCountProduct.setText("Có " + listProduct.size() + " sản phẩm ");
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product == null || listProduct == null || listProduct.isEmpty()) {
                    return;
                }
                for (int i = 0; i < listProduct.size(); i++) {
                    if (product.getId() == listProduct.get(i).getId()) {
                        listProduct.remove(listProduct.get(i));
                        break;
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void filterList(String text, ArrayList<Product> listProduct) {
        ArrayList<Product> filterLists = new ArrayList<>();
        for (Product product : listProduct) {
            if (product.getNameProduct().toLowerCase().contains(text.toLowerCase())) {
                filterLists.add(product);
            }
        }
        if (!filterLists.isEmpty()) {
            productAdapter.setFilterList(filterLists);
            bindProduct.tvCountProduct.setText("Có " + filterLists.size() + " sản phẩm.");
        }
    }


    @Override
    public void onClickItemProduct(Product product) {
        replaceFragment(new DetailProductFragment(product));
    }
}