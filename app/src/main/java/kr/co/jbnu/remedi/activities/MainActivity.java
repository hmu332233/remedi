package kr.co.jbnu.remedi.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import kr.co.jbnu.remedi.R;
import kr.co.jbnu.remedi.Utils.GlobalValue;
import kr.co.jbnu.remedi.Utils.ProgressBarDialog;
import kr.co.jbnu.remedi.adapters.BoardAdapter;
import kr.co.jbnu.remedi.models.Answer;
import kr.co.jbnu.remedi.models.Board;
import kr.co.jbnu.remedi.models.Medicine;
import kr.co.jbnu.remedi.models.Reply;
import kr.co.jbnu.remedi.models.User;
import kr.co.jbnu.remedi.serverIDO.ServerConnectionManager;
import kr.co.jbnu.remedi.serverIDO.ServerConnectionService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    ListView boardListView;
    BoardAdapter boardAdapter;
    ArrayList<Board> boards;

    private ProgressBarDialog progressBarDialog;
    private Boolean isConnectionOk = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //asdfasd
        //asdfasdf
        boards = User.getInstance().getUserBoardList();
        if(boards == null)
        {
            User.getInstance().setUserBoardList(new ArrayList<Board>());
            boards = User.getInstance().getUserBoardList();
        }




        boardAdapter = new BoardAdapter(this,User.getInstance(),boards);
        boardListView = (ListView) findViewById(R.id.lv_board);
        boardListView.setAdapter(boardAdapter);


        if(User.getInstance().getUser_type().equals("normal")){
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
        }else{
            com.github.clans.fab.FloatingActionButton btn_pick_gallery = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.btn_pick_gallery);
            btn_pick_gallery.setImageResource(R.drawable.checked1);
            btn_pick_gallery.setLabelText("답글 리스트 확인");



            com.github.clans.fab.FloatingActionButton btn_take_picture = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.btn_take_picture);
            btn_take_picture.setVisibility(View.GONE);
            //ViewGroup layout = (ViewGroup) btn_take_picture.getParent();

            //if(layout!=null) layout.removeView(btn_take_picture);

        }




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
                    System.out.println("사진찍기 데이터 : "+data.getExtras().get("data").toString());

                    Intent intent = new Intent(this,WriteBoardActivity.class);
                    intent.setData(imageURI);
                    intent.putExtra("data",(Bitmap)data.getExtras().get("data"));
                    intent.putExtra("type","camera");
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
                //System.out.println("갤러리 데이터 : "+data.getExtras().get("data").toString());
                Intent intent = new Intent(this,WriteBoardActivity.class);
                System.out.println("이미지 유알아이 세팅");
                intent.setData(imageURI);
                System.out.println("비트맵세팅");
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageURI));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bitmap = scaleDownBitmap(bitmap,100,getApplicationContext());

                intent.putExtra("data",bitmap);
                intent.putExtra("type","gallery");

                System.out.println("데이터는 모든 처리가 되었습니다");
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
        Log.d("보드인덱스",index+"");

        View view = getViewByPosition(index,boardListView);
        final Board board = (Board)boardListView.getAdapter().getItem(index);

        TextView tv_medicine_name = (TextView) view.findViewById(R.id.tv_medicine_name);
        Log.d("보드인덱스",medicine.getName());
        tv_medicine_name.setText(medicine.getName());

        final EditText et_answer = (EditText)view.findViewById(R.id.et_answer_content);

        Button btn_add_answer = (Button) view.findViewById(R.id.btn_add_answer);

        btn_add_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBarDialog = new ProgressBarDialog(MainActivity.this);
                progressBarDialog.show();



                Answer answer = new Answer(medicine.getName(),et_answer.getText().toString(),medicine.getShape(),
                        medicine.getEnterprise(),medicine.getStandardCode(),medicine.getCategory(),medicine.getEffect(),"","");
                System.out.println("medi category 확인 : "+answer.getMedi_category());

                board.setAnswer(answer);

                boards.set(index,board);

                register_answer(board.getId(),answer);

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

    private void register_answer(int board_id,Answer answer){
        System.out.println("답변 등록 요청");

        ServerConnectionManager serverConnectionManager = ServerConnectionManager.getInstance();
        ServerConnectionService serverConnectionService = serverConnectionManager.getServerConnection();


        Call<Boolean> registeranswer = serverConnectionService.register_answer(board_id,answer.getMedi_name(),answer.getAnswer_content(),answer.getMedi_element()
        ,answer.getMedi_company(),answer.getMedi_serialnumber(), answer.getMedi_category(),answer.getMedi_effect(),User.getInstance().getEmail());

        registeranswer.enqueue(new Callback<Boolean>() {

            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                isConnectionOk = response.body();
                System.out.println("존재 하는 값 확인"+isConnectionOk.toString());
                if(isConnectionOk==true){
                    progressBarDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "답변 등록 완료", Toast.LENGTH_SHORT).show();
                }else{
                    progressBarDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "답변 등록 오류", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                //final TextView textView = (TextView) findViewById(R.id.textView);
                //textView.setText("Something went wrong: " + t.getMessage());
                progressBarDialog.dismiss();
                Log.w("서버 통신 실패",t.getMessage());
            }
        });
    }

    private void get_normaluser_boardlist(){
        System.out.println("일반유저 게시물 데이터 요청");

        ServerConnectionManager serverConnectionManager = ServerConnectionManager.getInstance();
        ServerConnectionService serverConnectionService = serverConnectionManager.getServerConnection();

        //user 임시 세팅
        User user = new User("rupitere@naver.com","고석현","normal");
        User.setUser(user);

        final Call<ArrayList<Board>> board = serverConnectionService.get_normaluser_board(User.getInstance().getEmail());
        board.enqueue(new Callback<ArrayList<Board>>() {

            @Override
            public void onResponse(Call<ArrayList<Board>> call, Response<ArrayList<Board>> response) {

                ArrayList<Board> boardlist = response.body();
                for(int i=0;i<boardlist.size();i++){
                    System.out.println("board 아이디 : "+boardlist.get(i).getId());
                    if(boardlist.get(i).getAnswer()!=null){
                        Answer answer = boardlist.get(i).getAnswer();
                        System.out.println("답글 내용"+answer.getMedi_name()+answer.getMedi_company()+answer.getMedi_effect());
                        ArrayList<Reply> replies = answer.getRepliesList();
                        if(replies!=null){
                            for(int j=0;j<replies.size();j++)
                                System.out.println("댓글 내용 : "+replies.get(j).getContent());
                        }
                    }
                }

            }
            @Override
            public void onFailure(Call<ArrayList<Board>> call, Throwable t) {
                //final TextView textView = (TextView) findViewById(R.id.textView);
                //textView.setText("Something went wrong: " + t.getMessage());
                //progressBarDialog.dismiss();
                Log.w("서버 통신 실패",t.getMessage());
            }
        });
    }

    public Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h= (int) (newHeight*densityMultiplier);
        int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));

        photo=Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

}
