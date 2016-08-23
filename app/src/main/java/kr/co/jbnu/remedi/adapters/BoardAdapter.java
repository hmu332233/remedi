package kr.co.jbnu.remedi.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kr.co.jbnu.remedi.R;
import kr.co.jbnu.remedi.Utils.GlobalValue;
import kr.co.jbnu.remedi.activities.MedicineSearchActivity;
import kr.co.jbnu.remedi.models.Answer;
import kr.co.jbnu.remedi.models.Board;
import kr.co.jbnu.remedi.models.Reply;
import kr.co.jbnu.remedi.models.User;

public class BoardAdapter extends ArrayAdapter<Board> {

    String user_type;
    Context context;
    User user;

    ArrayList<Reply> replies;


    public BoardAdapter(Context _context, User _user, ArrayList<Board> boards) {
        super(_context, 0, boards);
        user = _user;
        user_type = user.getUser_type();
        context = _context;
    }
    
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {

        final Board board = getItem(position);

        final LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item = null;

        if( user_type == User.NORMAL || board.getAnswer() != null )
        {
            item = inflater.inflate(R.layout.item_board,null);

            final LinearLayout layout_extra = (LinearLayout) item.findViewById(R.id.layout_extra);

            CardView extra_btn = (CardView) item.findViewById(R.id.cv_btn_extra);
            extra_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(layout_extra.getVisibility() == View.GONE )
                        layout_extra.setVisibility(View.VISIBLE);
                    else
                        layout_extra.setVisibility(View.GONE);
                }
            });

            TextView tv_question = (TextView)item.findViewById(R.id.tv_question);
            tv_question.setText(board.getContent());

            ImageView imageView = (ImageView)item.findViewById(R.id.iv_warning);
            imageView.setAlpha(50);


            if(board.getAnswer() != null)
            {
                extra_btn.setVisibility(View.VISIBLE);

                TextView tv_answer = (TextView) item.findViewById(R.id.tv_answer);
                tv_answer.setVisibility(View.VISIBLE);
                tv_answer.setText(board.getAnswer().getAnswer_content());

                LinearLayout layout_no_answer = (LinearLayout) item.findViewById(R.id.layout_no_answer);
                layout_no_answer.setVisibility(View.GONE);


                Answer answer = board.getAnswer();

                TextView tv_medicine_name;
                TextView tv_medicine_element;
                TextView tv_medicine_company ;
                TextView tv_medicine_serialnumber;
                TextView tv_medicine_category;
                TextView tv_medicine_effect;

                tv_medicine_name = (TextView) item.findViewById(R.id.medicine_name);
                tv_medicine_element = (TextView) item.findViewById(R.id.medicine_element);
                tv_medicine_company = (TextView) item.findViewById(R.id.medicine_enterprise);
                tv_medicine_serialnumber = (TextView) item.findViewById(R.id.medicine_standardCode);
                tv_medicine_category = (TextView) item.findViewById(R.id.medicine_category);
                tv_medicine_effect = (TextView) item.findViewById(R.id.medicine_effect);

                tv_medicine_name.setText(answer.getMedi_name());
                tv_medicine_element.setText(answer.getMedi_element());
                tv_medicine_company.setText(answer.getMedi_company());
                tv_medicine_serialnumber.setText(answer.getMedi_serialnumber());
                tv_medicine_category.setText(answer.getMedi_category());
                tv_medicine_effect.setText(answer.getMedi_effect());


                //------댓글
                replies = answer.getRepliesList();
                if(replies == null)
                {
                    replies = new ArrayList<Reply>();
                }



                ListView lv_reply = (ListView) item.findViewById(R.id.lv_reply);
                final ReplyAdapter replyAdapter = new ReplyAdapter(context,replies);
                lv_reply.setAdapter(replyAdapter);


                final EditText et_reply = (EditText) item.findViewById(R.id.et_reply);

                et_reply.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {

                        if(event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                        {
                            Log.d("알림","ㅁㄴㅇㄹ");

                            replies.add(0,new Reply(et_reply.getText().toString(),user.getName()));
                            et_reply.setText("");
                            replyAdapter.notifyDataSetChanged();
                            return true;
                        }

                        return false;
                    }
                });

                final LinearLayout layout_reply = (LinearLayout) item.findViewById(R.id.layout_reply);

                CardView cv_reply = (CardView) item.findViewById(R.id.cv_reply);
                cv_reply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(layout_reply.getVisibility() == View.GONE) {
                            layout_reply.setVisibility(View.VISIBLE);

                            /*
                            * 여기다가 클릭하면 로딩ㅇ할수있게
                            */

                            replyAdapter.notifyDataSetChanged();
                        }
                        else
                            layout_reply.setVisibility(View.GONE);
                    }
                });

                cv_reply.setVisibility(View.VISIBLE);

            }
        }
        else
        {
            item = inflater.inflate(R.layout.item_board_pharm,null);

            LinearLayout layout_btn_input = (LinearLayout) item.findViewById(R.id.layout_btn_input);
            final LinearLayout layout_btn = (LinearLayout) item.findViewById(R.id.layout_btn);
            final LinearLayout layout_input = (LinearLayout) item.findViewById(R.id.layout_input);

            layout_btn_input.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if( layout_btn.getVisibility() == View.VISIBLE)
                    {
                        layout_btn.setVisibility(View.GONE);
                        layout_input.setVisibility(View.VISIBLE);
                    }

/*
                    Intent intent = new Intent(context, AnswerActivity.class);

                    intent.putExtra("board", board);
                    intent.putExtra("index",position);

                    ((Activity) context).startActivityForResult(intent,123);
*/

                }
            });

            TextView tv_question = (TextView)item.findViewById(R.id.tv_question);
            tv_question.setText(board.getContent());

            TextView tv_medicine_name = (TextView) item.findViewById(R.id.tv_medicine_name);
            tv_medicine_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MedicineSearchActivity.class);

                    intent.putExtra("board", board);
                    intent.putExtra("index",position);

                    ((Activity) context).startActivityForResult(intent, GlobalValue.GET_MEDICINE);
                }
            });
/*
            Button btn_add_answer = (Button) item.findViewById(R.id.btn_add_answer);

            btn_add_answer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("알림","테스트입니다");
                }
            });*/


        }

        return item;
    }

    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("알림", "onActivityResult");
    }
}
