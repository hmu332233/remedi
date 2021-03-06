package kr.co.jbnu.remedi.Utils;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import kr.co.jbnu.remedi.R;

/**
 * Created by seokhyeon on 2016-08-25.
 */
public class ImageDialog extends Dialog{

    Context context;
    String imgurl;


    public ImageDialog(Context context,String imgurl){
        super(context);

        this.context = context;
        this.imgurl = imgurl;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image_dialog);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((WindowManager.LayoutParams) params);


        ImageView bigimg = (ImageView)findViewById(R.id.big_image);
        Picasso.with(getContext()).load(Uri.parse(imgurl)).error(R.drawable.ic_nocover).into(bigimg);
        bigimg.setScaleType(ImageView.ScaleType.FIT_XY);
        bigimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });



    }

}