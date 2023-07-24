package com.tom_roush.pdfbox.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentCatalog;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission;
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDCheckBox;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDComboBox;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDField;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDListBox;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDRadioButton;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDTextField;
import com.tom_roush.pdfbox.rendering.ImageType;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MainActivity extends Activity {
    File root;
    AssetManager assetManager;
    Bitmap pageImage;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 666);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 豐富菜單； 這會將項目添加到操作欄（如果存在）。
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * 初始化變量以方便使用
     */
    private void setup() {
        // 啟用 Android 資源加載
        PDFBoxResourceLoader.init(getApplicationContext());
        // 找到外部存儲的根目錄。

        root = getApplicationContext().getCacheDir();
        assetManager = getAssets();
        tv = (TextView) findViewById(R.id.statusTextView);
    }

    /**
     * 從頭開始創建新的 PDF 並將其保存到文件中
     */
    public void createPdf(View v) {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        // 選擇 PDF 基本字體之一創建新字體對象
        PDFont font = PDType1Font.HELVETICA;
        // Or a custom font
//        try
//        {
//            // Replace MyFontFile with the path to the asset font you'd like to use.
//            // Or use LiberationSans "com/tom_roush/pdfbox/resources/ttf/LiberationSans-Regular.ttf"
//            font = PDType0Font.load(document, assetManager.open("MyFontFile.TTF"));
//        }
//        catch (IOException e)
//        {
//            Log.e("PdfBox-Android-Sample", "Could not load font", e);
//        }

        PDPageContentStream contentStream;

        try {
            // 定義用於添加到 PDF 的內容流
            contentStream = new PDPageContentStream(document, page);

            // 用藍色文本寫“Hello World”
            contentStream.beginText();
            contentStream.setNonStrokingColor(15, 38, 192);
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Hello World");
            contentStream.endText();

            // 加載圖像
            InputStream in = assetManager.open("falcon.jpg");
            InputStream alpha = assetManager.open("trans.png");

            // 畫一個綠色的矩形
            contentStream.addRect(5, 500, 100, 100);
            contentStream.setNonStrokingColor(0, 255, 125);
            contentStream.fill();

            // 繪製獵鷹基礎圖像
            PDImageXObject ximage = JPEGFactory.createFromStream(document, in);
            contentStream.drawImage(ximage, 20, 20);

            // 繪製紅色疊加圖像
            Bitmap alphaImage = BitmapFactory.decodeStream(alpha);
            PDImageXObject alphaXimage = LosslessFactory.createFromImage(document, alphaImage);
            contentStream.drawImage(alphaXimage, 20, 20 );

            // 確保內容流已關閉：
            contentStream.close();

            // 將最終的 pdf 文檔保存到文件中
            String path = root.getAbsolutePath() + "/Created.pdf";
            document.save(path);
            document.close();
            tv.setText("Successfully wrote PDF to " + path);

        } catch (IOException e) {
            Log.e("PdfBox-Android-Sample", "Exception thrown while creating PDF", e);
        }
    }

    /**
     * 加載現有 PDF 並將其渲染為位圖
     */
    public void renderFile(View v) {
        // 渲染頁面並將其保存到圖像文件
        try {
            // 加載已創建的 PDF
            PDDocument document = PDDocument.load(assetManager.open("Created.pdf"));
            // Create a renderer for the document
            PDFRenderer renderer = new PDFRenderer(document);
            // 為文檔創建渲染器
            pageImage = renderer.renderImage(0, 1, ImageType.RGB);

            // 將渲染結果保存到圖像
            String path = root.getAbsolutePath() + "/render.jpg";
            File renderFile = new File(path);
            FileOutputStream fileOut = new FileOutputStream(renderFile);
            pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
            fileOut.close();
            tv.setText("Successfully rendered image to " + path);
            // 可選：在屏幕上顯示渲染結果
            displayRenderedImage();
        }
        catch (IOException e)
        {
            Log.e("PdfBox-Android-Sample", "Exception thrown while rendering file", e);
        }
    }

    /**
     * 填寫 PDF 表單並保存結果
     */
    public void fillForm(View v) {
        try {
            // 加載文檔並獲取 AcroForm
            PDDocument document = PDDocument.load(assetManager.open("FormTest.pdf"));
            PDDocumentCatalog docCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            // 填寫文本字段
            PDTextField field = (PDTextField) acroForm.getField("TextField");
            field.setValue("Filled Text Field");
            // 可選：不允許編輯該字段
            field.setReadOnly(true);

            PDField checkbox = acroForm.getField("Checkbox");
            ((PDCheckBox) checkbox).check();

            PDField radio = acroForm.getField("Radio");
            ((PDRadioButton)radio).setValue("Second");

            PDField listbox = acroForm.getField("ListBox");
            List<Integer> listValues = new ArrayList<>();
            listValues.add(1);
            listValues.add(2);
            ((PDListBox) listbox).setSelectedOptionsIndex(listValues);

            PDField dropdown = acroForm.getField("Dropdown");
            ((PDComboBox) dropdown).setValue("Hello");

            String path = root.getAbsolutePath() + "/FilledForm.pdf";
            tv.setText("Saved filled form to " + path);
            document.save(path);
            document.close();
        } catch (IOException e) {
            Log.e("PdfBox-Android-Sample", "Exception thrown while filling form fields", e);
        }
    }

    /**
     * 從 PDF 中刪除文本並在屏幕上顯示文本
     */
    public void stripText(View v) {
        String parsedText = null;
        PDDocument document = null;
        try {
            document = PDDocument.load(assetManager.open("Hello.pdf"));
        } catch(IOException e) {
            Log.e("PdfBox-Android-Sample", "Exception thrown while loading document to strip", e);
        }

        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(0);
            pdfStripper.setEndPage(1);
            parsedText = "Parsed text: " + pdfStripper.getText(document);
        }
        catch (IOException e)
        {
            Log.e("PdfBox-Android-Sample", "Exception thrown while stripping text", e);
        } finally {
            try {
                if (document != null) document.close();
            }
            catch (IOException e)
            {
                Log.e("PdfBox-Android-Sample", "Exception thrown while closing document", e);
            }
        }
        tv.setText(parsedText);
    }

    /**
     * 創建一個簡單的 pdf 並對其進行加密
     */
    public void createEncryptedPdf(View v)
    {
        String path = root.getAbsolutePath() + "/crypt.pdf";

        int keyLength = 128; // 目前支持最高128位

        // 限制沒有密碼的人的權限
        AccessPermission ap = new AccessPermission();
        ap.setCanPrint(false);

        // 設置所有者密碼和用戶密碼
        StandardProtectionPolicy spp = new StandardProtectionPolicy("12345", "hi", ap);

        // 設置加密參數
        spp.setEncryptionKeyLength(keyLength);
        spp.setPermissions(ap);
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        PDFont font = PDType1Font.HELVETICA;
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();

        document.addPage(page);

        try
        {
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // 用藍色文本寫“Hello World”
            contentStream.beginText();
            contentStream.setNonStrokingColor(15, 38, 192);
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Hello World");
            contentStream.endText();
            contentStream.close();

            // 將最終的 pdf 文檔保存到文件中
            document.protect(spp); // 對 PDF 應用保護
            document.save(path);
            document.close();
            tv.setText("Successfully wrote PDF to " + path);

        }
        catch (IOException e)
        {
            Log.e("PdfBox-Android-Sample", "Exception thrown while creating PDF for encryption", e);
        }
    }

    /**
     * Helper method for drawing the result of renderFile() on screen
     */
    private void displayRenderedImage() {
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView imageView = (ImageView) findViewById(R.id.renderedImageView);
                        imageView.setImageBitmap(pageImage);
                    }
                });
            }
        }.start();
    }
}