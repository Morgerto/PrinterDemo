package printerdemo.android.com.printerdemo.Tools;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by 刘英杰 on 2017/6/14.
 */

public class MyPrintAdapter extends PrintDocumentAdapter {

    Context mContext;
    private float mPageHeight;
    private float mPageWidth;
    public PdfDocument myPdfDocument;
    public int mTotalpages = 1;

    //test
    private double mTempSmallGridWidth = 2.83845;//this value avoid arithmetic error

    public MyPrintAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal,
                         LayoutResultCallback callback,
                         Bundle metadata) {

        Log.i("blb", "--------run onLayout");
        myPdfDocument = new PrintedPdfDocument(mContext, newAttributes); //create document

        mPageHeight =
                newAttributes.getMediaSize().getHeightMils() / 1000.0f * 72.0f; //设置尺寸,为什么是1000 * 72, 72dpi, 1000mils = 1 inch
        mPageWidth =
                newAttributes.getMediaSize().getWidthMils() / 1000.0f * 72.0f;

        //calculate small grid width
        if (mPageHeight > mPageWidth){
            mTempSmallGridWidth = 72 / 25.4f;
//            mTempSmallGridWidth = (mPageHeight / 280.0f + mPageWidth / 201.0f) / 2.0f;//calculate 1mm = ? px when screen is
        }else {
//            mSmallGridWidth = (mPageHeight / 203.2f + mPageWidth / 280.0f) / 2.0f
            mTempSmallGridWidth = 72 / 25.4f;//calculate 1mm = ? px
        }

        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        if (mTotalpages > 0) {
            PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                    .Builder("whiteRadish")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(mTotalpages);//构建文档配置信息

            PrintDocumentInfo info = builder.build();
            callback.onLayoutFinished(info, true);
        } else {
            callback.onLayoutFailed("Page count is zero.");
        }
    }

    @Override
    public void onWrite(final PageRange[] pageRanges, final ParcelFileDescriptor destination, final CancellationSignal cancellationSignal,
                        final WriteResultCallback callback) {

        Log.i("blb", "--------run onWrite");

        for (int i = 0; i < mTotalpages; i++) {
            if (pageInRange(pageRanges, i)) //keep page index is correct
            {
                PageInfo newPage = new PageInfo.Builder((int)mPageWidth,
                        (int)mPageHeight, i).create();

                PdfDocument.Page page =
                        myPdfDocument.startPage(newPage);  //创建新页面

                if (cancellationSignal.isCanceled()) {  //取消信号
                    callback.onWriteCancelled();
                    myPdfDocument.close();
                    myPdfDocument = null;
                    return;
                }
                drawPage(page, i);  //将内容绘制到页面Canvas上
                myPdfDocument.finishPage(page);
            }
        }

        try {
            myPdfDocument.writeTo(new FileOutputStream(
                    destination.getFileDescriptor()));
        } catch (IOException e) {
            callback.onWriteFailed(e.toString());
            return;
        } finally {
            myPdfDocument.close();
            myPdfDocument = null;
        }

        callback.onWriteFinished(pageRanges);
    }

    private boolean pageInRange(PageRange[] pageRanges, int page) {
        for (int i = 0; i < pageRanges.length; i++) {
            if ((page >= pageRanges[i].getStart()) &&
                    (page <= pageRanges[i].getEnd()))
                return true;
        }
        return false;
    }

    //页面绘制（渲染）
    private void drawPage(PdfDocument.Page page,
                          int pagenumber) {
        Canvas canvas = page.getCanvas();
        Log.i("blb", "----------density:" + canvas.getDensity());
//        canvas.setMatrix();
//        canvas.setDensity();
        //这里是页码。页码不能从0开始
        pagenumber++;

        Paint paint = new Paint();

        PageInfo pageInfo = page.getInfo();

        drawBackgroundSquares(canvas, paint);
        drawSomeThing(canvas, paint);
//        drawPictureFromSDCard(canvas, paint);
    }

    //draw picture from other directory
    private void drawPictureFromSDCard(Canvas canvas, Paint paint) throws IOException {
        //draw picture
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
//        options.inJustDecodeBounds = false;
//        options.outWidth = 400;
//        options.
        canvas.drawBitmap(BitmapFactory.decodeStream(mContext.getAssets().open("testPicture.jpg")),
                0,//pageInfo.getPageWidth()/2, mPageWidth * 0.87f
                0,//pageInfo.getPageHeight()/2, 13
                paint);
    }

    //draw background squares
    private void drawBackgroundSquares(Canvas canvas, Paint paint){

        Log.i("blb", "paint stroke width:" + paint.getStrokeWidth());

        //draw vertical lines
        for (int i = 0; i < mPageWidth / mTempSmallGridWidth; i++) {
            if (i % 5 == 0){
                paint.setColor(Color.parseColor("#bbDC143C"));
                paint.setStrokeWidth(0.2f);
            }else {
                paint.setColor(Color.parseColor("#bb00ffff"));
                paint.setStrokeWidth(0.10f);
            }
            canvas.drawLine((float)((i * 72.0d) / 25.4d), 0,
                    (float)((i * 72.0d) / 25.4d), mPageHeight, paint);
        }

        //draw horizontal lines of background
        for (int i = 0; i < mPageHeight / mTempSmallGridWidth; i++) {
            if (i % 5 == 0){
                paint.setColor(Color.parseColor("#bbDC143C"));
                paint.setStrokeWidth(0.2f);
            }else {
                paint.setColor(Color.parseColor("#bb00ffff"));
                paint.setStrokeWidth(0.1f);
            }
            canvas.drawLine(0, (float)((i * 72.0d) / 25.4d), mPageWidth, (float)((i * 72.0d) / 25.4d), paint);
        }

    }

    private void drawSomeThing(Canvas canvas, Paint paint){
        //draw circle
        paint.reset();
        paint.setColor(Color.BLACK);
        canvas.drawCircle(mPageWidth / 2.0f, mPageHeight / 2.0f, 20.0f, paint);
    }
}
