package com.nick.sampleffmpeg;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.nick.libffmpeg.FFmpeg;
import com.nick.sampleffmpeg.Define.Constant;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.ObjectGraph;

/**
 * First Commit New Two three Four
 */
public class Home extends Activity implements View.OnClickListener {

    private static final String TAG = Home.class.getSimpleName();

    @Inject
    FFmpeg ffmpeg;

    @InjectView(R.id.command_output)
    LinearLayout outputLayout;

    @InjectView(R.id.run_command)
    Button runButton;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);
        ObjectGraph.create(new DaggerDependencyModule(this)).inject(this);

        initUI();
    }

    private void initUI() {
        runButton.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
        progressDialog.setCanceledOnTouchOutside(false);

    }

    /**
     * Get testing ffmpeg-commandline script.
     * @return
     */
    public synchronized static String[] getCommand() {
        ArrayList<String> ret = new ArrayList<>();
        String path = Constant.getApplicationDirectory();
        ret.add("-y");

        ret.add("-i");
        ret.add(path + "complete_115002.wmv");

        ret.add("-i");
        ret.add(path + "caption_0.png");

        ret.add("-i");
        ret.add(path + "caption_1.png");

        ret.add("-i");
        ret.add(path + "brand.png");

        ret.add("-c:a");
        ret.add("aac");

        ret.add("-strict");
        ret.add("experimental");

        ret.add("-threads");
        ret.add("5");

        ret.add("-preset");
        ret.add("ultrafast");

        ret.add("-strict");
        ret.add("experimental");

        ret.add("-y");

        ret.add("-filter_complex");
        String strFilterComplex = "[0:v][1:v] overlay=0:562:enable='between(t,5.03082889518414,8.05192889518414)' [tmp];[tmp][2:v] overlay=54:366:enable='between(t,9.64173484419263,11.6628348441926)' [tmp];[tmp][3:v] overlay=64:56";
        ret.add(strFilterComplex);

        ret.add("-r");
        ret.add("30");
        ret.add("-c:v");
        ret.add("libx264");

        ret.add("-ss");
        ret.add("4.54604362606232");
        ret.add("-to");
        ret.add("13.0034441926346");

        strFilterComplex = path;
        strFilterComplex += "result_complete_115002_Modified.mp4";
        ret.add(strFilterComplex);

        String[] command = new String[ret.size()];
        for (int i = 0; i < ret.size(); i ++ ) {
            command[i] = ret.get(i);
        }
        return command;
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.run_command:
//                //String cmd = getCommand();
//                String[] command = getCommand();
//                if (command.length != 0) {
//                    execFFmpegBinary(command);
//                } else {
//                    Toast.makeText(Home.this, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
//                }
//                break;
//        }
    }
}
