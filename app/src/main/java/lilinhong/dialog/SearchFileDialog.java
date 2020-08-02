package lilinhong.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.libsdl.app.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lilinhong.utils.PreferencesData;
import lilinhong.utils.Utils;

public class SearchFileDialog extends Dialog {
    private PreferencesData preferencesData;
    private TextView search_file_text_path = null;
    private ProgressBar search_file_progres = null;
    private TextView search_file_text_findrom = null;
    private TextView search_file_text_progres = null;
    private Context context = null;
    private SearchFileAsyncTask searchFileAsyncTask = null;
    private Button search_file_btn = null;
    //是否中断搜索
    private boolean isBreakSearch = false;
    private boolean searchFinish = false;
    public SearchFileDialog(Context context) {
        super(context, R.style.mdialog);
        this.context = context;
        this.initData();
        this.initUI();
        this.iniDataFinish();
    }

    private void initData() {
        preferencesData = PreferencesData.getInstance();
    }

    private void iniDataFinish() {
        File[] files;
        List<String> tempList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            files = this.context.getExternalFilesDirs(Environment.MEDIA_MOUNTED);
            for(File file:files){
                Log.e("main:",file.getAbsolutePath());
                tempList.add(file.getAbsolutePath());
            }
        }
        //低于4.1版本
        tempList.add(context.getExternalFilesDir(null).getAbsolutePath());
        //内存存储
        tempList.add(context.getFilesDir().getAbsolutePath());
        //外部存储
        tempList.add(Environment.getExternalStorageDirectory().getAbsolutePath());

        List<File> pathList = new ArrayList<>();
        for (String mount : tempList) {
            if(mount !=null){
                File root = new File(mount);
                if (root.exists() && root.isDirectory() && root.canRead()) {
                    pathList.add(root);
                }
            }
        }
        searchFileAsyncTask = new SearchFileAsyncTask(this.context);
        searchFileAsyncTask.execute(pathList);
    }

    private void initUI() {
        setContentView(R.layout.search_file_dialog);
        search_file_text_path = (TextView)findViewById(R.id.search_file_text_path);
        search_file_progres = (ProgressBar)findViewById(R.id.search_file_progres);
        search_file_progres.setProgress(0);
        search_file_text_progres = (TextView)findViewById(R.id.search_file_text_progres);
        search_file_text_findrom = (TextView)findViewById(R.id.search_file_text_findrom);
        search_file_btn = (Button)findViewById(R.id.search_file_btn);
        search_file_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(searchFinish){
                        dismiss();
                    }else{
                        isBreakSearch = true;
                    }
                }
                return true;
            }
        });
        setCancelable(false);
    }

    //获取是否是用户故意中断
    public boolean getIsBreakSearch(){
        return isBreakSearch;
    }

    class ProgresData{
        private float progres = 0.0f;
        private String dirName = "";
        private int findNum;
        public String getDirName() {
            return dirName;
        }

        public void setDirName(String dirName) {
            this.dirName = dirName;
        }

        public float getProgres() {
            return progres;
        }

        public void setProgres(float progres) {
            this.progres = progres;
        }

        public int getFindNum() {
            return findNum;
        }

        public void setFindNum(int findNum) {
            this.findNum = findNum;
        }
    }
    public class SearchFileAsyncTask extends AsyncTask<List<File>, ProgresData, List<File>> {
        private Context context=null;
        public SearchFileAsyncTask(Context context){
            this.context = context;
        }
        @Override
        protected List<File> doInBackground(List<File>... lists) {
            List<File> gameFileList = new ArrayList<>();
            float allDirCount = 0;
            for (File file:lists[0]){
                //中断扫描
                if(isBreakSearch){
                    break;
                }

                File[] files = file.listFiles();
                if(files!=null) {
                    allDirCount+=files.length;
                }
            }
            float saerchDirIndex = 0;
            for (File file:lists[0]){
                //中断扫描
                if(isBreakSearch){
                    break;
                }

                File[] files = file.listFiles();
                if(files!=null){
                    for (File dirFile:files) {
                        saerchDirIndex+=1;
                        Utils.getGameFiles(dirFile.getAbsolutePath(), gameFileList);
                        float progres = (saerchDirIndex/allDirCount);

                        ProgresData progresData = new ProgresData();
                        progresData.setDirName(dirFile.getName());
                        progresData.setProgres(progres);
                        progresData.setFindNum(gameFileList.size());
                        publishProgress(progresData);
                    }
                }
            }
            return gameFileList;
        }
        @Override
        protected void onPostExecute(List<File> result) {
            Utils.addGameFileList(result);
            //中断直接关闭
            if(isBreakSearch){
                dismiss();
                return;
            }
            //扫描完成
            searchFinish = true;
            search_file_btn.setText(this.context.getString(R.string.close));
        }
        //该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
        @Override
        protected void onPreExecute() {
            //btn.setText("开始执行异步线程");
        }
        /**
         * 这里的Intege参数对应AsyncTask中的第二个参数
         * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行
         * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作
         */
        @Override
        protected void onProgressUpdate(ProgresData... values) {
            ProgresData vlaue = values[0];
            Log.e("getDirName",vlaue.getDirName());
            search_file_text_path.setText(vlaue.getDirName());
            int progres = (int) (vlaue.getProgres()*100+0.5);
            search_file_progres.setProgress(progres);
            search_file_text_progres.setText(String.valueOf(progres)+this.context.getString(R.string.progres_num));
            search_file_text_findrom.setText(String.format(this.context.getString(R.string.find_roms),vlaue.getFindNum()));
        }
    }
}
