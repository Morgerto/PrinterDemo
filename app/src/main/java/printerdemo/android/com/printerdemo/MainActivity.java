package printerdemo.android.com.printerdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import printerdemo.android.com.printerdemo.Tools.MyPrintAdapter;

//Reminder before you start use this demo
/*
you should an service from app market to search printer in the same network, such as HP Print Service.
some android phones are not support this kind of service, these phones can not use this function.
*/
public class MainActivity extends AppCompatActivity {
    public void requestPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS},
                    3);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN},
                    5);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS},
                    4);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 6);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        findViewById(R.id.printPicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    doPhotoPrint();
                } catch (IOException e) {
                    Log.i("blb", "--------print picture error");
                }
            }
        });

        findViewById(R.id.printDocument).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDocPrint("blb" + System.currentTimeMillis());
            }
        });
    }

    private void doDocPrint(String jobName) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setColorMode(PrintAttributes.COLOR_MODE_COLOR);
//        builder.setDuplexMode(DUPLEX_MODE_SHORT_EDGE);
//        builder.setResolution();
        PrintAttributes.MediaSize temp = PrintAttributes.MediaSize.ISO_A4;
        temp.asLandscape();
        Log.i("blb", "--------is portrait:" + temp.isPortrait());
        builder.setMediaSize(temp);//invalid param
//        builder.setDuplexMode();
//        builder.setMediaSize(M);
//        PrintAttributes.Margins margins = new PrintAttributes.Margins(10, 10, 10, 10)//invalid param
        builder.setResolution(new PrintAttributes.Resolution("white", "whiteRadish", 300, 300));//invalid param
        MyPrintAdapter myPrintAdapter = new MyPrintAdapter(this);

        printManager.print(jobName, myPrintAdapter, builder.build());
    }

    //print photo from assert directory
    private void doPhotoPrint() throws IOException {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
//        photoPrinter.setColorMode(PrintHelper.COLOR_MODE_COLOR);
        photoPrinter.setColorMode(PrintHelper.COLOR_MODE_MONOCHROME);
        photoPrinter.setOrientation(PrintHelper.ORIENTATION_LANDSCAPE);//invalid param
        Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("testPicture.jpg"));
//                BitmapFactory.decodeResource(getResources(),
//                R.mipmap.ic_launcher);
        photoPrinter.printBitmap("print_picture_" + (int)(Math.random()*100), bitmap);
    }
}
