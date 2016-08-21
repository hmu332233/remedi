package kr.co.jbnu.remedi.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kr.co.jbnu.remedi.GlobalValue;
import kr.co.jbnu.remedi.R;
import kr.co.jbnu.remedi.adapters.BoardAdapter;
import kr.co.jbnu.remedi.models.Answer;
import kr.co.jbnu.remedi.models.Board;
import kr.co.jbnu.remedi.models.Medicine;
import kr.co.jbnu.remedi.models.User;

public class MainActivity extends AppCompatActivity {


    ListView boardListView;
    BoardAdapter boardAdapter;
    ArrayList<Board> boards;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //asdfasd
        //asdfasdf
        boards = new ArrayList<>();
        Board b = new Board("","답변없는질문",null);
        Board c = new Board("","없없",null);
        Board d = new Board("","질문?",null);

        boards.add(b);
        boards.add(c);
        boards.add(d);

        Board b2 = new Board("","답변이 달려있음",new Answer("","이건답변입니다","","","","","","",""));
        boards.add(b2);

        User user = new User(1,"email","name", User.PHARM,"a",boards);
        boardAdapter = new BoardAdapter(this,user.getUser_type(),user.getUserBoardList());


        boardListView = (ListView) findViewById(R.id.lv_board);
        boardListView.setAdapter(boardAdapter);

        com.github.clans.fab.FloatingActionButton btn_pick_gallery = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.btn_pick_gallery);
        btn_pick_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallery();
            }
        });

        com.github.clans.fab.FloatingActionButton btn_take_picture = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.btn_take_picture);
        btn_take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTakeCamera();
            }
        });



/*
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Medicine_Parser medicine_Parser = new Medicine_Parser();

                ArrayList<Medicine> medicineList = medicine_Parser.requestMedicines("타이레놀");

                Medicine medicine = medicineList.get(0);

                medicine = medicine_Parser.requestMedicine_detail(medicine,medicine.getCodeNumber());

                medicine.print();
            }
        });*/
       // t.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == RESULT_OK) {
            if (requestCode == GlobalValue.TAKE_CAMERA) {
                if (data != null) {
                    Log.e("Test", "result = " + data);

                    Uri imageURI = data.getData();

                    /*
                    Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                    if (thumbnail != null) {
                        Log.d("알림", "사진찍기 완료 완료");
                    }*/

                    Intent intent = new Intent(this,WriteBoardActivity.class);
                    intent.setData(imageURI);
                    startActivityForResult(intent,321);
                }
            }


            if(requestCode == GlobalValue.GET_MEDICINE)
            {

                Log.d("알림","그거테스트입니다");
                setAnswer(data.getIntExtra("index",0),(Medicine)data.getSerializableExtra("medicine"));
            }


            if( requestCode == GlobalValue.PICK_FROM_GALLERY)
            {
                Uri imageURI = data.getData() ;
                Log.d("알림", "이미지 가져오기 완료");

                Intent intent = new Intent(this,WriteBoardActivity.class);
                intent.setData(imageURI);
                startActivityForResult(intent,321);
            }

            if( requestCode == 321 )
            {

                boards.add((Board)data.getSerializableExtra("board"));
                boardAdapter.notifyDataSetChanged();
            }
        }




    }


    private void setAnswer(final int index, final Medicine medicine)
    {
        View view = boardListView.getChildAt(index);
        final Board board = (Board)boardListView.getAdapter().getItem(index);

        TextView tv_medicine_name = (TextView) view.findViewById(R.id.tv_medicine_name);
        tv_medicine_name.setText(medicine.getName());

        final EditText et_answer = (EditText)view.findViewById(R.id.et_answer_content);

        Button btn_add_answer = (Button) view.findViewById(R.id.btn_add_answer);

        btn_add_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Answer answer = new Answer(medicine.getName(),et_answer.getText().toString(),medicine.getShape(),
                        medicine.getEnterprise(),medicine.getStandardCode(),medicine.getCategory(),medicine.getEffect(),"","");
                board.setAnswer(answer);

                boards.set(index,board);
                boardAdapter.notifyDataSetChanged();
            }
        });


    }

    public void getImageFromGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GlobalValue.PICK_FROM_GALLERY);
    }

    private void getTakeCamera()
    {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, GlobalValue.TAKE_CAMERA);
    }
}