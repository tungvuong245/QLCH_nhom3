package com.example.qlch.setting;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.qlch.R;
import com.example.qlch.base.BaseFragment;
import com.example.qlch.databinding.LayoutChangeProfileBinding;
import com.example.qlch.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class UpdateUserFragment extends BaseFragment {
    private LayoutChangeProfileBinding binding = null;
    private User userData = null;

    private FirebaseAuth firebaseAuth;
    private Uri avatar;
    private static final int PICL_IMAGES_CODE = 1000;
    FirebaseUser firebaseUser;
    Calendar calendar;


    public UpdateUserFragment newInstance(User user) {
        this.userData = user;
        return new UpdateUserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LayoutChangeProfileBinding.inflate(inflater,container,false);
//        Log.d("TAG", "onCreateView: "+ userData.getId());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window window = getActivity().getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(getActivity().getColor(R.color.white));

        firebaseAuth = FirebaseAuth.getInstance();
        StorageReference reference = FirebaseStorage.getInstance().getReference().child("avatars");
        reference.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference files: listResult.getItems()
            ) {
                if (files.getName().equals(userData.getId())){
                    files.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d("TAG", "initView: "+uri);
                        if(getActivity() != null){
                            Glide.with(getActivity()).load(uri).into(binding.imgAvatar);
                        }

                    });
                }

            }
        }).addOnFailureListener(e -> {

        });

        listening();
        initObSever();


    }

    @Override
    public void loadData() {
        showUserProfile();
    }

    @Override
    public void listening() {
        binding.icBack.setOnClickListener(v -> {
            backStack();
        });

        binding.icChooserBirth.setOnClickListener(v -> {
            dialogChooseBirth();
        });

        binding.icAddImg.setOnClickListener(v -> {
            requestPermission();
        });

        binding.icSave.setOnClickListener(v -> {
            dialogConfirmUpdate(getContext());
        });
    }


    @Override
    public void initObSever() {

    }

    @Override
    public void initView() {

    }

    // oke
    private void showUserProfile(){
        firebaseUser = firebaseAuth.getCurrentUser();
        String userID = firebaseUser.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userData = snapshot.getValue(User.class);
//                Log.d("TAG", "onDataChange1: "+userData.getId());
                if(firebaseUser != null) {
//                    Log.d("TAG", "showUserProfile: " + firebaseUser);
                    binding.edName.setText(userData.getName_user());
                    binding.edPhone.setText(userData.getPhone_number());
                    binding.edEmail.setText(firebaseUser.getEmail());
                    binding.edAddress.setText(userData.getAddress());
                    binding.edBirth.setText(userData.getBirthday());
                    if(userData.getSex()){
                        binding.rdoMale.setSelected(true);
                    }else {
                        binding.rdoFemale.setSelected(true);
                    }

                }else {
                    Toast.makeText(getContext(), "Lỗi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Something Wrong!!!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void dialogChooseBirth(){
        calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String strDay = "";
                        String strMonths = "";
                        if(dayOfMonth < 10){
                            strDay = "0" + dayOfMonth;
                        }else{
                            strDay = String.valueOf(dayOfMonth);
                        }
                        if((month + 1) < 10){
                            strMonths = "0" + (month + 1);
                        }else{
                            strMonths = String.valueOf(month + 1);
                        }
                        binding.edBirth.setText(strDay + "/" + strMonths + "/" + year);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateUserProfile(){
        firebaseUser = firebaseAuth.getCurrentUser();
        String userID = firebaseUser.getUid();
        if (avatar != null) {
            StorageReference reference = FirebaseStorage.getInstance().getReference("avatars/"+userID);
            reference.putFile(avatar).addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(command -> {
                Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thất bại", Toast.LENGTH_SHORT).show();
            });
        }
        boolean gender;
            if(binding.rdoMale.isSelected()){
                gender = true;
            } else {
                gender = false;
            }

        userData = new User(binding.edName.getText().toString().trim(), gender, binding.edPhone.getText().toString().trim(), binding.edBirth.getText().toString().trim(),
                binding.edAddress.getText().toString().trim(), binding.edEmail.getText().toString().trim());
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child("users").child(userID).updateChildren(userData.toMap(),
                    new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if (firebaseUser != null) {
                        Toast.makeText(getContext(), "Sửa thông tin thành công", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }


    private void dialogConfirmUpdate(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Cập nhật thông tin cá nhân");
        builder.setIcon(context.getDrawable(R.drawable.ic_save));
        builder.setMessage("Bạn chắc chắn muốn thay đổi thông tin cá nhân");
        builder.setCancelable(false);
        builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(firebaseUser.getEmail() != binding.edEmail.getText().toString()){
                    updateUserProfile();
                }else {
                    updateUserProfile();
                }

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

    private void requestPermission(){
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }

        } else {
            addImage();
        }
    }

    private void addImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICL_IMAGES_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICL_IMAGES_CODE) {
                avatar = data.getData();
                binding.imgAvatar.setImageURI(avatar);
                Log.d("TAG", "onActivityResult: "+data.getData());

            }
        }
    }


}