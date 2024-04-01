package com.study.qrscanner;

import static com.study.qrscanner.R.string.content_copied;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.study.qrscanner.DataBase.DBHandler;
import com.study.qrscanner.DataBase.ScanedData;
import com.study.qrscanner.resultfragments.BizCardResultFragment;
import com.study.qrscanner.resultfragments.ContactResultFragment;
import com.study.qrscanner.resultfragments.EmailResultFragment;
import com.study.qrscanner.resultfragments.GeoResultFragment;
import com.study.qrscanner.resultfragments.MMSResultFragment;
import com.study.qrscanner.resultfragments.MeCardResultFragment;
import com.study.qrscanner.resultfragments.ProductResultFragment;
import com.study.qrscanner.resultfragments.QRResultType;
import com.study.qrscanner.resultfragments.ResultFragment;
import com.study.qrscanner.resultfragments.SMSResultFragment;
import com.study.qrscanner.resultfragments.SendEmailResultFragment;
import com.study.qrscanner.resultfragments.TelResultFragment;
import com.study.qrscanner.resultfragments.TextResultFragment;
import com.study.qrscanner.resultfragments.URLResultFragment;
import com.study.qrscanner.resultfragments.WifiResultFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultActivity extends AppCompatActivity implements TextResultFragment.OnFragmentInteractionListener {

    private QRResultType currentResultType;
    private String currentQrCode;
    private ResultFragment currentResultFragment;

    @Override
    public void onFragmentInteraction(Uri uri) {
        //getSupportFragmentManager()
    }

    DBHandler dbHandler;/////////DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        dbHandler = new DBHandler(this, null);//DB

        // 从App偏好设置管理器获取共享的偏好设置
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // 获取“是否开启历史记录”的开关
        boolean checked = sharedPref.getBoolean("bool_history", true);
        Bundle intentExtras = getIntent().getExtras();
        // 获取扫描页面跳转传递的QRCode参数
        assert intentExtras != null;
        String QRCode = intentExtras.getString("QRResult");
        this.currentQrCode = QRCode;
        // 获取历史列表页面跳转传递的二维码内容参数
        String QrHistory = intentExtras.getString("QrHistory");

        QRResultType resultType = QRResultType.TEXT;

        if (checked && QRCode != null) {
            // addContent();
            ScanedData content = new ScanedData(QRCode);
            // 将历史记录存入DB
            dbHandler.addContent(content);
            resultType = checkResult(QRCode);
        } else if (!checked && QRCode != null) {
            resultType = checkResult(QRCode);
        } else if (QrHistory != null) {
            resultType = checkResult(QrHistory);
            QRCode = QrHistory;
            this.currentQrCode = QrHistory;
        }

        loadFragment(resultType, QRCode);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btnProceed) {
            if (currentResultFragment != null) {
                currentResultFragment.onProceedPressed(this, currentQrCode);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle intentExtras = getIntent().getExtras();
        String qrCode = intentExtras.getString("QRResult");

        switch (item.getItemId()) {
            case R.id.share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, qrCode);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
                return true;

            case R.id.copy:
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Text", qrCode);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), content_copied, Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadFragment(QRResultType resultType, String qrCodeString) {
        // Android使用Fragment来管理布局（翻译成碎片）
        // https://blog.csdn.net/u011240877/category_6895052.html
        // Fragment FragmentManager FragmentTransaction三者的区别
        // https://blog.csdn.net/u011240877/article/details/78132990
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Activity Android的四大组件之一，一般不翻译，强行翻译的话就是活动

        currentResultType = resultType;

        ResultFragment resultFragment;

        switch (resultType) {
            case EMAIL:
                resultFragment = new EmailResultFragment();
                break;
            case PHONE:
                resultFragment = new TelResultFragment();
                break;
            case SEND_EMAIL:
                resultFragment = new SendEmailResultFragment();
                break;
            case URL:
                resultFragment = new URLResultFragment();
                break;
            case SMS:
                resultFragment = new SMSResultFragment();
                break;
            case MMS:
                resultFragment = new MMSResultFragment();
                break;
            case CONTACT:
                resultFragment = new ContactResultFragment();
                break;
            case ME_CARD:
                resultFragment = new MeCardResultFragment();
                break;
            case BIZ_CARD:
                resultFragment = new BizCardResultFragment();
                break;
            case WIFI:
                resultFragment = new WifiResultFragment();
                break;
            case GEO:
                resultFragment = new GeoResultFragment();
                break;
            case PRODUCT:
                resultFragment = new ProductResultFragment();
                break;
            case TEXT:
            default:
                resultFragment = new TextResultFragment();
        }

        currentResultFragment = resultFragment;

        resultFragment.putQRCode(qrCodeString, resultType);

        ft.replace(R.id.activity_result_frame_layout, resultFragment);
        ft.commit();
    }

    public QRResultType checkResult(String result) {
        if (isValidEmail(result)) {
            return QRResultType.EMAIL;
        } else if (isValidCellPhone(result)) {
            return QRResultType.PHONE;
        } else if (isValidSendEmail(result)) {
            return QRResultType.SEND_EMAIL;
        } else if (isValidURL(result)) {
            return QRResultType.URL;
        } else if (isValidSms(result) || isValidSmsTo(result)) {
            return QRResultType.SMS;
        } else if (isValidMms(result) || isValidMmsTo(result)) {
            return QRResultType.MMS;
        } else if (isValidContact(result)) {
            return QRResultType.CONTACT;
        } else if (isValidMeCard(result)) {
            return QRResultType.ME_CARD;
        } else if (isValidBizCard(result)) {
            return QRResultType.BIZ_CARD;
        } else if (isValidWifi(result)) {
            return QRResultType.WIFI;
        } else if (isValidGeoInfo(result)) {
            return QRResultType.GEO;
        } else if (isValidProduct(result)) {
            return QRResultType.PRODUCT;
        } else {
            return QRResultType.TEXT;
        }
    }

    // *********************************************************************** //

    public static boolean isValidEmail(CharSequence target) {

        String expression = "^MAILTO:+[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

        Pattern pattern = Pattern.compile(expression, CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.matches();
        //return Patterns.EMAIL_ADDRESS.matcher(target).matches();

    }

    public static boolean isValidSendEmail(String target) {
        Pattern pattern = Pattern.compile("MATMSG:TO:(.+?);SUB:(.+?);BODY:(.+?)", CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(target);
        return matcher.matches();

        // return Patterns.EMAIL_ADDRESS.matcher(target).matches();

    }

    public static boolean isValidCellPhone(String number) {
        // return android.util.Patterns.PHONE.matcher(number).matches();
        if (number.startsWith("tel:"))
            return true;
        else return false;

    }

    public static boolean isValidProduct(String target) {

        if (target.startsWith("market://"))
            return true;
        else return false;

    }

    public static boolean isValidURL(String target) {
        return Patterns.WEB_URL.matcher(target).matches();
    }

    public static boolean isValidSms(String target) {
        Pattern pattern = Pattern.compile("sms:(.+?):(.+?)", CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.matches();
    }

    public static boolean isValidSmsTo(String target) {
        Pattern pattern = Pattern.compile("SMSTO:(.+?):(.+?)", CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.matches();
    }

    public static boolean isValidMms(String target) {
        Pattern pattern = Pattern.compile("mms:(.+?):(.+?)", CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.matches();
    }

    public static boolean isValidMmsTo(String target) {
        Pattern pattern = Pattern.compile("MMSTO:(.+?):(.+?)", CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.matches();
    }

    public boolean isValidContact(String target) {
       /* Pattern pattern = Pattern.compile("([\\n|;|:](FN:|N:)[0-9a-zA-Z-\\säöüÄÖÜß,]*[\\n|;])");
         Matcher matcher = pattern.matcher(target);
        return matcher.matches();*/
        return target.startsWith("BEGIN:VCARD");
    }

    public boolean isValidMeCard(String target) {

        return target.startsWith("MECARD");
    }

    public boolean isValidBizCard(String target) {

        return target.startsWith("BIZCARD");
    }

    public boolean isValidWifi(String target) {
        return target.startsWith("WIFI:");
    }

    public boolean isValidGeoInfo(String target) {
        return target.startsWith("geo:");
    }

}
