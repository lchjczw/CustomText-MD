package liubaoyua.customtext.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import liubaoyua.customtext.R;
import liubaoyua.customtext.fragments.AppListFragment;
import liubaoyua.customtext.fragments.FragmentAdapter;
import liubaoyua.customtext.interfaces.FragmentCommunicator;
import liubaoyua.customtext.utils.AppInfo;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.DrawableUtils;
import liubaoyua.customtext.utils.PicassoTools;
import liubaoyua.customtext.utils.Utils;

public class AppList extends AppCompatActivity implements FragmentCommunicator {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private AppListFragment appListFragment;
    private AppListFragment recentListFragment;
    private SearchView mSearchView;
    private FragmentAdapter fragmentAdapter;

    private Context context;

    private List<AppListFragment> fragmentList = new ArrayList<>();
    private List<AppInfo> appList = Collections.synchronizedList(new ArrayList<AppInfo>());
    private List<AppInfo> recentList = Collections.synchronizedList(new ArrayList<AppInfo>());
    private List<String> titles =null;

    private String nameFilter;
    private SharedPreferences prefs;

    private LoadAppsTask loadAppsTask;

    private static File prefsDir = new File(Environment.getDataDirectory()+"/data/" + Common.PACKAGE_NAME + "/shared_prefs" );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
//        initSystemBar();
        Log.v(Common.TAG, Locale.TAIWAN.toString() + Locale.CHINA.toString());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        titles = new ArrayList<>();
        titles.add(getResources().getString(R.string.all_apps));
        titles.add(getResources().getString(R.string.recent_apps));

        //三道杠
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_main_drawer);
        if (mDrawerLayout != null) {
            setupDrawerLayout();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nv_main_navigation);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        context = getApplicationContext();
        PicassoTools.init(context);

        prefs = getSharedPreferences(Common.PREFS, MODE_WORLD_READABLE);
        SharedPreferences.Editor editor;
        editor = prefs.edit();
        editor.putString(Common.PACKAGE_NAME_ARG,"^"+getString(R.string.app_name)+"$");
        editor.putString(Common.MESSAGE, getString(R.string.setting_default_message));

        int version = prefs.getInt(Common.PACKAGE_VERSION_CODE,0);

        PackageInfo packageInfo = null;
        try{
            packageInfo = getPackageManager().getPackageInfo(Common.PACKAGE_NAME, PackageManager.GET_UNINSTALLED_PACKAGES);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        if(packageInfo != null){
            if(packageInfo.versionCode > version){
                Utils.showMessage(this,packageInfo.versionName);
            }
            editor.putInt(Common.PACKAGE_VERSION_CODE,packageInfo.versionCode);
        }
        editor.commit();

        setupViewPager();
        if(appListFragment.getAppRecyclerAdapter() == null){
            loadAppsTask = new LoadAppsTask(true);
            loadAppsTask.execute();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_list, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search).setVisible(true);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                nameFilter = query;
                fragmentList.get(0).getAppRecyclerAdapter()
                        .getFilter().filter(nameFilter);
                fragmentList.get(1).getAppRecyclerAdapter()
                        .getFilter().filter(nameFilter);
                mSearchView.clearFocus();
//                mSearchView.setIconified(true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                nameFilter = newText;
                fragmentList.get(0).getAppRecyclerAdapter()
                        .getFilter().filter(nameFilter);
                fragmentList.get(1).getAppRecyclerAdapter()
                        .getFilter().filter(nameFilter);
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH && (event.getFlags() & KeyEvent.FLAG_CANCELED) == 0) {
            if (mSearchView.isShown()) {
                mSearchView.setIconified(false);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
//    private void initSystemBar() {
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
//            Window window = getWindow();
//            // Translucent status bar
//            window.setFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            // Translucent navigation bar
//            window.setFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            SystemBarTintManager tintManager = new SystemBarTintManager(this);
//            tintManager.setStatusBarTintEnabled(true);
//            tintManager.setStatusBarTintResource(R.color.primary);
//            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
//            findViewById(R.id.main_content).setPadding(0, config.getPixelInsetTop(true), 0, config.getPixelInsetBottom());
////            mViewPager.setPadding(0,0,0,);
//        }
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager() {
        if(mTabLayout == null){
            mTabLayout = (TabLayout) findViewById(R.id.tabs);
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));
        }
        if(appListFragment == null){
            appListFragment = new AppListFragment();
            appListFragment.setAppList(new ArrayList<AppInfo>());
//            appListFragment.prepareAdapter();
        }
        if (recentListFragment == null){
            recentListFragment = new AppListFragment();
            recentListFragment.setAppList(new ArrayList<AppInfo>());
//            recentListFragment.prepareAdapter();
            fragmentList.add(appListFragment);
            fragmentList.add(recentListFragment);
        }
        if(mViewPager == null) {
            mViewPager = (ViewPager) findViewById(R.id.viewpager);
        }
        if(fragmentAdapter == null){
            fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragmentList, titles);
            mViewPager.setAdapter(fragmentAdapter);
            mTabLayout.setupWithViewPager(mViewPager);
            mTabLayout.setTabsFromPagerAdapter(fragmentAdapter);
        }
}

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if (id == R.id.nav_settings) {
                            startActivity(new Intent(context, Settings.class));
                        } else if (id == R.id.nav_backup) {
                            doExport();
                        } else if (id == R.id.nav_exit) {
//                            onDestroy();
                            finish();
//                            System.exit(0);
                        } else if (id == R.id.nav_restore) {
                            doImport();
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void setupDrawerLayout() {

//        动画，并设置监听器
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open,
                R.string.close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void doExport() {
        final String[] name =new String[1];
        final EditText editText =new EditText(this);
        editText.setHint(R.string.imp_exp_backup_hint);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(64,64,64,64);
        layout.addView(editText);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_input_backup_name).setView(layout);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                name[0] = editText.getText().toString();
                if (name[0].matches("^\\s*$") || name[0].length() == 0) {
                    System.out.println(true);
                    Date d = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
                    name[0] = sdf.format(d);
                }

                new ExportTask().execute(new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + Common.BACKUP_DIR, name[0]));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void doImport() {
        final List<File> backupList = new ArrayList<>();
        File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + Common.BACKUP_DIR);
        if (root.exists())
        {
            File[] temp  = root.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });
            Collections.addAll(backupList,temp);
            Collections.sort(backupList,new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase());
                }
            });
        }
        if(backupList.size() == 0){
            Toast.makeText(context,
                    getString(R.string.imp_exp_no_backup_found),Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(64, 64, 64, 64);
        ListView listView = new ListView(this);
        listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final File tmp = backupList.get(position);
                if (!tmp.exists()) {
                    Toast.makeText(context,
                            getString(R.string.imp_exp_file_does_not_exist, tmp.getAbsolutePath()),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(AppList.this);
                builder.setTitle(R.string.menu_restore);
                builder.setMessage(R.string.imp_exp_confirm);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new ImportTask().execute(tmp);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        layout.addView(listView);
        ArrayAdapter<File> adapter = new ArrayAdapter<File>(this,R.layout.list_item_restore,backupList){
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if(convertView==null){
                    convertView=View.inflate(context, R.layout.list_item_restore, null);
                }
                TextView textView = (TextView)convertView.findViewById(R.id.text_file);
                textView.setText(backupList.get(position).getName());
                final Button button=(Button)convertView.findViewById(R.id.button_delete);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Utils.emptyDirectory(backupList.get(position));
                        backupList.get(position).delete();
                        backupList.remove(position);
                        notifyDataSetChanged();
                    }
                });
                return convertView;
            }
        };
        listView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_select_restore_item).setView(layout);
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void reFreshList() {
        if(loadAppsTask == null || loadAppsTask.getStatus() == AsyncTask.Status.FINISHED){
            loadAppsTask = new LoadAppsTask(false);
            loadAppsTask.execute();
        }
    }

    class LoadAppsTask extends AsyncTask<Void,String,Void> {
        private ProgressDialog dialog;
        private Boolean showDialog;

        public LoadAppsTask(Boolean showDialog){
            this.showDialog = showDialog;
        }
        @Override
        protected void onPreExecute() {
            if(showDialog){
                dialog = new ProgressDialog(AppList.this);
                dialog.setMessage(getString(R.string.dialog_loading));
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setCancelable(false);
                dialog.show();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(dialog != null)
                dialog.setMessage(values[0]);
        }


        @Override
        protected Void doInBackground(Void... params) {
            appList  = Collections.synchronizedList(new ArrayList<AppInfo>());
            recentList = Collections.synchronizedList(new ArrayList<AppInfo>());;
            if(appList.size()==0){
                PackageManager pm =getPackageManager();
                List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
                if(dialog != null)
                    dialog.setMax(packages.size());
                int i =1;
                for (PackageInfo pkgInfo : packages) {
                    if(dialog != null)
                        dialog.setProgress(i++);
                    ApplicationInfo appInfo = pkgInfo.applicationInfo;
                    if (appInfo == null)
                        continue;
                    if (appInfo.packageName.equals(Common.PACKAGE_NAME))
                        continue;
                    if(dialog != null)
                        publishProgress(getString(R.string.dialog_loading) + "\n" + appInfo.loadLabel(pm).toString());
                    AppInfo appInfoItem = new AppInfo(pkgInfo, appInfo.loadLabel(pm).toString());
                    if(prefs.contains(appInfoItem.packageName)){
                        if(prefs.getBoolean(appInfoItem.packageName, false)){
                            appInfoItem.state = AppInfo.ENABLED;
                        }else{
                            appInfoItem.state = AppInfo.DISABLED;
                        }
                        recentList.add(appInfoItem);
                    }
                    appList.add(appInfoItem);
//                    Log.d(Common.TAG, appInfoItem.toString());
                    if(Common.FAST_DEBUG){
                        Log.d(Common.TAG, appInfoItem.toString());
                        if(appList.size()>6)
                            break;
                    }
                }
                Collections.sort(appList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return Collator.getInstance(Locale.getDefault()).compare(lhs.appName, rhs.appName);
                    }
                });
                Collections.sort(recentList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return Collator.getInstance(Locale.getDefault()).compare(lhs.appName, rhs.appName);
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aa) {
//            setupViewPager();
            if(recentListFragment.getAppRecyclerAdapter() != null){
                if(!Utils.checkList(appList, appListFragment.getAppRecyclerAdapter().getFilter().getAppList())){
                    appListFragment.getAppRecyclerAdapter().getFilter().setAppList(appList);
                    fragmentList.get(0).getAppRecyclerAdapter().getFilter().filter(nameFilter);

                    recentListFragment.getAppRecyclerAdapter().getFilter().setAppList(recentList);
                    fragmentList.get(1).getAppRecyclerAdapter().getFilter().filter(nameFilter);;
                    new LoadAppsIcon(appList).execute();
                }
            }
            if(recentListFragment.getSwipeRefreshWidget() != null){
                appListFragment.getSwipeRefreshWidget().setRefreshing(false);
                recentListFragment.getSwipeRefreshWidget().setRefreshing(false);
            }
            if(dialog!=null)
                dialog.dismiss();
        }

    }

    class ExportTask extends AsyncTask<File, String, String> {
        @Override
        protected String doInBackground(File... params) {
            File backupDir = params[0];
          if(!backupDir.exists())
                backupDir.mkdirs();
            if(prefsDir.exists()){
                String files[] = prefsDir.list();
                if(files.length!=0){
                    for (String file : files) {
                        File srcFile = new File(prefsDir, file);
                        File destFile = new File(backupDir, file);
                        try {
                            Utils.CopyFile(srcFile, destFile);
                        } catch (IOException ex) {
                            return getString(R.string.imp_exp_export_error, ex.getMessage());
                        } catch (Exception ex){
                            return ex.getMessage();
                        }
                    }
                }
            }
            return getString(R.string.imp_exp_exported, backupDir.toString());
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }

    class ImportTask extends AsyncTask<File, String, String> {


        @SuppressWarnings("deprecation")
        @Override
        protected String doInBackground(File... params) {
            File inFile = params[0];
            Utils.emptyDirectory(prefsDir);
            String files[] = inFile.list();
            for (String file : files) {
                File srcFile = new File(inFile, file);
                File destFile = new File(prefsDir, file);
                try {
                    Utils.CopyFile(srcFile, destFile);
                    destFile.setReadable(true, false);
                    destFile.setWritable(true, true);
                } catch (IOException ex) {
                    return getString(R.string.imp_exp_import_error, ex.getMessage());
                } catch (Exception ex){
                    return ex.getMessage();
                }
            }

            return getString(R.string.imp_exp_imported);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(Common.DEBUG){
            Log.d(Common.TAG,"onActivityResult");
        }
        if(requestCode == Common.APP_REQUEST_CODE){
            if(resultCode == Common.APP_RESULT_CODE){
                boolean isEnabled = data.getBooleanExtra(Common.IS_ENABLED_ARG,false);
                int position = data.getIntExtra(Common.POSITION_ARG,-1);
                if(Common.DEBUG){
                    Log.d(Common.TAG,"onActivityResult  "+isEnabled + "  position   " + position);
                }
                if(position != -1){
                    List<AppInfo> list = fragmentList.get(1)
                            .getAppRecyclerAdapter().getAppList();
                    AppInfo temp = fragmentList.get(mViewPager.getCurrentItem())
                            .getAppRecyclerAdapter().getAppList().get(position);
                    if(isEnabled){
                        temp.state = AppInfo.ENABLED;
                        if(!list.contains(temp)){
                            list.add(0,temp);
                            fragmentList.get(1)
                                    .getAppRecyclerAdapter().notifyDataSetChanged();
                        }
                    }else{
                        temp.state = AppInfo.DISABLED;
                        if(list.contains(temp)){
                            fragmentList.get(1)
                                    .getAppRecyclerAdapter().notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        PicassoTools.clearCache();
        PicassoTools.destroy();
//        loadAppsTask = null;
//        if(fragmentAdapter != null){
//            fragmentAdapter = null;
//        }
//        fragmentAdapter = null;
//        appList.clear();
//        recentList.clear();
//        appList = null;
//        recentList = null;
//        context = null;
    }

    private class LoadAppsIcon extends  AsyncTask<Void,Void,Void>{
        List<AppInfo> appInfoList;

        public LoadAppsIcon(List<AppInfo> appInfoList) {
            super();
            this.appInfoList = appInfoList;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for(AppInfo app:appInfoList){
                File file =new File(context.getCacheDir(),app.packageName+".png");
                if(!file.exists()) {
                    try {
                        Bitmap bmp = (((BitmapDrawable) context.getPackageManager().getApplicationIcon(app.packageName)).getBitmap());
                        DrawableUtils.saveBitmapToCacheDir(context, app.packageName, bmp, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(Common.DEBUG){
            Log.d(Common.TAG,"activity is finish  " + isFinishing());
        }
    }
}