package liubaoyua.customtext.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import liubaoyua.customtext.R;
import liubaoyua.customtext.app.AppHelper;
import liubaoyua.customtext.entity.AppInfo;
import liubaoyua.customtext.entity.CustomText;
import liubaoyua.customtext.ui.AppListActivity;

/**
 * Created by liubaoyua on 2015/6/20 0020.
 * tools
 */
public abstract class Utils {

    public static String getPinYin(String src) {
        String t4 = "";
        try {
            char[] t1 = null;
            t1 = src.toCharArray();
            String[] t2 = new String[t1.length];
            // 设置汉字拼音输出的格式

            HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
            t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            t3.setVCharType(HanyuPinyinVCharType.WITH_V);
            int t0 = t1.length;
            try {
                for (int i = 0; i < t0; i++) {
                    // 判断能否为汉字字符
                    // System.out.println(t1[i]);
                    if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
                        t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);// 将汉字的几种全拼都存到t2数组中
                        t4 += t2[0];// 取出该汉字全拼的第一种读音并连接到字符串t4后
                    } else {
                        // 如果不是汉字字符，间接取出字符并连接到字符串t4后
                        t4 += Character.toString(t1[i]);
                    }
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
            return t4;
        } catch (Exception e) {
            e.printStackTrace();
            return t4;
        }
    }

    /**
     * 提取每个汉字的首字母
     *
     * @param str
     * @return String
     */
    public static String getPinYinHeadChar(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (int j = 0; j < str.length(); j++) {
                char word = str.charAt(j);
                // 提取汉字的首字母
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
                if (pinyinArray != null) {
                    stringBuilder.append(pinyinArray[0].charAt(0));
                } else {
                    stringBuilder.append(word);
                }
            }
            if (str.equals(stringBuilder.toString())) {
                String[] headChar = str.split(" ");
                stringBuilder = new StringBuilder();
                for (String a : headChar) {
                    if (a != null && a.length() > 0)
                        stringBuilder.append(a.charAt(0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String getT9(String str) {
        String convert = "";
        for (int j = 0; j < str.length(); j++) {
            char word = str.charAt(j);
            switch (word) {
                case 'a':
                case 'b':
                case 'c':
                    word = '2';
                    break;
                case 'd':
                case 'e':
                case 'f':
                    word = '3';
                    break;
                case 'g':
                case 'h':
                case 'i':
                    word = '4';
                    break;
                case 'j':
                case 'k':
                case 'l':
                    word = '5';
                    break;
                case 'm':
                case 'n':
                case 'o':
                    word = '6';
                    break;
                case 'p':
                case 'q':
                case 'r':
                case 's':
                    word = '7';
                    break;
                case 't':
                case 'u':
                case 'v':
                    word = '8';
                    break;
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    word = '9';
                    break;
                default:
                    break;
            }
            convert += word;
        }
        return convert;
    }

    public static boolean isIdenticalTextList(ArrayList<CustomText> a, ArrayList<CustomText> b) {
        Log.d("text", a.toString() + b.toString());
        if (a.size() == b.size()) {
            for (int i = 0; i < a.size(); i++) {
                if (!(a.get(i).equals(b.get(i)))) {
                    return false;
                }
            }
            return true;
        } else
            return false;
    }


    public static boolean emptyDirectory(File directory) {
        boolean result = true;
        File[] entries = directory.listFiles();
        for (int i = 0; i < entries.length; i++) {
            if (!entries[i].delete()) {
                result = false;
            }
        }
        return result;
    }

    public static boolean CopyFile(File in, File out) throws Exception {
        try {
            FileInputStream fis = new FileInputStream(in);
            FileOutputStream fos = new FileOutputStream(out);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
            fis.close();
            fos.close();
            return true;
        } catch (IOException ie) {
            ie.printStackTrace();
            return false;
        }
    }

    public static boolean CopyFile(String infile, String outfile) throws Exception {
        try {
            File in = new File(infile);
            File out = new File(outfile);
            return CopyFile(in, out);
        } catch (IOException ie) {
            ie.printStackTrace();
            return false;
        }

    }

    /**
     * 检查两个list 是否相同
     *
     * @param loaderAppList list1
     * @param appList       list2
     * @return result
     */
    public static boolean checkList(List<AppInfo> loaderAppList, List<AppInfo> appList) {
        if (loaderAppList == null || appList == null) {
            return false;
        }
        if (loaderAppList.size() == appList.size()) {
            for (int i = 0; i < appList.size(); i++) {
                if (!appList.get(i).equals(loaderAppList.get(i))) {
                    return false;
                }
            }
            return true;
        } else
            return false;
    }

    public static List<AppInfo> getRecentList(List<AppInfo> appList) {
        List<AppInfo> temp = new ArrayList<>();
        for (int i = 0; i < appList.size(); i++) {
            AppInfo info = appList.get(i);
            if (info.state != AppInfo.UNKNOWN) {
                temp.add(info);
            }
        }
        return temp;
    }

    public static void showMessage(Context context, String versionName) {
        // do nothings.
//        ScrollView scrollView = new ScrollView(context);
//        TextView textView = new TextView(context);
//        textView.setMovementMethod(LinkMovementMethod.getInstance());
//        textView.setLinksClickable(true);
//        textView.setText(context.getString(R.string.dialog_about) + versionName);
//
//        scrollView.setPadding(64, 64, 64, 64);
//        scrollView.addView(textView);
//        textView.setTextColor(context.getResources().getColor(android.R.color.black));
//        textView.setTextSize(15);
//
//        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
//        dlgBuilder.setTitle(R.string.dialog_about_title);
//        dlgBuilder.setCancelable(true);
//        dlgBuilder.setIcon(R.mipmap.ic_launcher);
//        dlgBuilder.setPositiveButton(android.R.string.ok, null);
//        dlgBuilder.setView(scrollView);
//        dlgBuilder.show();
    }

    public static PackageInfo getPackageInfoByPackageName(Context context, String packageName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        // We ask for the bounds if they have been set as they would be most
        // correct, then we check we are > 0
        final int width = !drawable.getBounds().isEmpty() ? drawable.getBounds().width() : drawable.getIntrinsicWidth();

        final int height =
                !drawable.getBounds().isEmpty() ? drawable.getBounds().height() : drawable.getIntrinsicHeight();

        // Now we check we are > 0
        final Bitmap bitmap =
                Bitmap.createBitmap(width <= 0 ? 1 : width, height <= 0 ? 1 : height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void myLog(String msg) {
        if (Common.DEBUG) {
            Log.d("liubaoyua", msg);
        }
    }

    public static AppInfo getAppInfoByPackageName(String packageName) {
        List<AppInfo> allList = AppHelper.getAllList();
        for (AppInfo info : allList) {
            if (info.packageName.equals(packageName)) {
                return info;
            }
        }
        return null;
    }

    public static void killPackage(String packageToKill) {
        // code modified from :
        // http://forum.xda-developers.com/showthread.php?t=2235956&page=6
        try { // get superuser
            Process su = Runtime.getRuntime().exec("su");
            if (su == null)
                return;
            DataOutputStream os = new DataOutputStream(su.getOutputStream());
            os.writeBytes("pkill " + packageToKill + "\n");
            os.writeBytes("exit\n");
            su.waitFor();
            os.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static void configStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (activity instanceof AppListActivity) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                activity.getWindow().setStatusBarColor(Color.parseColor("#00000000"));
            } else {
                TypedValue typedValue = new TypedValue();
                activity.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
                int color = typedValue.data;
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                activity.getWindow().setStatusBarColor(color);
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            TypedValue typedValue = new TypedValue();
            activity.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager.setStatusBarTint(activity, color);
        }
    }

    public static void launchImagePiker(Activity activity, int requestCode) {
        Intent intent;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        } else {
        intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
//        }
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.select_picture)), requestCode);
    }

    @TargetApi(19)
    public static String parsePicturePath(Context context, Uri uri) {

        if (null == context || uri == null)
            return null;

        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentUri
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageDocumentsUri
            if (isExternalStorageDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] splits = docId.split(":");
                String type = splits[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + File.separator + splits[1];
                }
            }
            // DownloadsDocumentsUri
            else if (isDownloadsDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaDocumentsUri
            else if (isMediaDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean isDocumentUri(Context context, Uri uri) {
        return DocumentsContract.isDocumentUri(context, uri);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getDataForKitKat(Context context, Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");

        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String selection = "_id=?";
        String[] selectionArgs = new String[]{split[1]};

        return getDataColumn(context, contentUri, selection, selectionArgs);
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static void getFileFromContent(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        try {

            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {

        }
    }

}

