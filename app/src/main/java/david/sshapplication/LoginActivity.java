package david.sshapplication;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoginActivity extends AppCompatActivity {
    private EditText pw;
    private EditText uname;
    private EditText hostname;
    private EditText port;
    private EditText command;
    private TextView response;
    private CheckBox gtk;
    private CheckBox session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pw = (EditText)findViewById(R.id.pw);
        uname = (EditText)findViewById(R.id.uname);
        hostname = (EditText)findViewById(R.id.ip);
        port = (EditText)findViewById(R.id.port);
        command = (EditText)findViewById(R.id.command);
        response = (TextView) findViewById(R.id.response);
        gtk = (CheckBox)findViewById(R.id.checkBox);
        response.setBackgroundColor(getResources().getColor(R.color.terminalBlack));
        response.setTextColor(getResources().getColor(R.color.terminalFont));

    }
    public void send(View v){
        final String password = pw.getText().toString();
        final String username = uname.getText().toString();
        final String hostname1 = hostname.getText().toString();
        final int port1 = Integer.valueOf(port.getText().toString());
        final String command1 = command.getText().toString();
        final String[] response1 = {null};
        final boolean gtk1 = gtk.isChecked();
        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                try {

                       String s = executeRemoteCommand(username, password, port1, hostname1, command1,gtk1);


                    System.out.println("SSSSSSS" + s);
                    response1[0] = s;
                    System.out.println("MIDXD" + response1[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                System.out.println("RESPONSE" + response1[0]);
                response.setText(response1[0]);
            }
        }.execute(1);
        System.out.println("XDDDDDDDDDDDDD");

    }
    public static String executeRemoteCommand(String username, String password, int port, String hostname ,String command, boolean gtk) throws JSchException, IOException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(username,hostname,port);
        session.setPassword(password);
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);
        session.connect();
        // SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);
        InputStream in= null;
        try {
            in = channelssh.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        channelssh.setPty(true);
        channelssh.setPtyType("VT100");
       // channelssh.setCommand("echo divadv|sudo -S bash -c \"export DISPLAY=:0\"&&" + command);
        if(gtk) {

            channelssh.setCommand("env DISPLAY=:0 nohup " + command);
        }
        else{
            channelssh.setCommand(command);
        }
        // Execute command

        channelssh.connect();
        byte[] tmp=new byte[1024];
        while(true){
            try {
                while(in.available()>0){
                    System.out.println("AVAILABLE");
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;

                    System.out.print("XD?" + new String(tmp, 0, i));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(channelssh.isClosed()){
                System.out.println("CLOSED");

                try {
                    if(in.available()>0) continue;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("exit-status: "+channelssh.getExitStatus());
                break;
            }
            try{Thread.sleep(1000);}catch(Exception ee){}
        }
        baos.write(tmp);

        System.out.println("Output:" + baos.toString());
        channelssh.disconnect();
        System.out.println("DISCONNECTED");
        return baos.toString();

    }

}
