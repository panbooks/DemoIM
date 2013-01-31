package com.panbook.DemoServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DemoServerActivity extends Activity {
	/** Called when the activity is first created. */
	private Button mStartServerButton;
	private TextView mIpInfoTextView;
	private TextView mRecieveContentTextView;
	private EditText mSendMessageEditText;
	private Button mSendMessageButton;

	private String mClientContent;
	private OutputStream mOutStream;
	private InputStream mInStream;
	private PrintWriter mPrintWriter;

	private Handler mHandler;
	private WifiManager mWifiManager;
	private ServerSocket mServerSocket;
	private Socket mClientSocket;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mStartServerButton = (Button) findViewById(R.id.startServer);
		mIpInfoTextView = (TextView) findViewById(R.id.ipInfoTV);
		mRecieveContentTextView = (TextView) findViewById(R.id.recieveContentTV);
		mSendMessageEditText = (EditText) findViewById(R.id.sendMessageEditText);
		mSendMessageButton = (Button) findViewById(R.id.sendMessageButton);

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (!Thread.currentThread().isInterrupted()) {
					switch (msg.what) {
					// 连接成功
					case 0:
						Toast.makeText(DemoServerActivity.this,
								"Connect success!", Toast.LENGTH_LONG).show();
						break;
					// 连接失败
					case 1:
						Toast.makeText(DemoServerActivity.this,
								"Connect success!", Toast.LENGTH_LONG).show();
						break;
					// 接受到消息
					case 2:
						String str = mRecieveContentTextView.getText() + "\n"
								+ mClientContent;
						mRecieveContentTextView.setText(str);
						break;
					}
				}
				super.handleMessage(msg);
			}
		};

		mStartServerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// 获取wifi相关信息
				getWifiInfo();
				Thread thread = new Thread() {
					public void run() {
						serverStart();
					}
				};
				thread.start();
				thread = null;
			}
		});

		mSendMessageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String sendMsg = mSendMessageEditText.getText().toString();
				if (null != mPrintWriter && null != sendMsg) {
					Log.i("send msg", sendMsg);
					mPrintWriter.println(sendMsg);
				}
			}
		});
	}

	/**
	 * 启动server
	 */
	private void serverStart() {

		try {
			// 实例化服务器套接字 设置端口号8888
			mServerSocket = new ServerSocket(8886);
			mClientSocket = mServerSocket.accept();
			if (null != mClientSocket) {
				sendHanlderMessage(0);
			}
			mOutStream = mClientSocket.getOutputStream();
			mInStream = mClientSocket.getInputStream();
			// 获取server端的输出流
			mPrintWriter = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(mOutStream)), true);
			// 获取server端输入流
			BufferedReader in = new BufferedReader(new InputStreamReader(
					mInStream));
			while (true) {
				mClientContent = in.readLine();
				if (null != mClientContent) {
					Log.i("reciever:", mClientContent);
				}
				sendHanlderMessage(2);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取当前wifi的信息
	 */
	private void getWifiInfo() {
		mWifiManager = (WifiManager) DemoServerActivity.this
				.getSystemService(Context.WIFI_SERVICE);
		mWifiManager.setWifiEnabled(true);
		WifiInfo info = mWifiManager.getConnectionInfo();
		String infoStr = "IP:" + Integer.toHexString(info.getIpAddress())
				+ "\nIP:" + intToIp(info.getIpAddress()) + "\nSSID:"
				+ info.getSSID() + "\nLinkSpeed:" + info.getLinkSpeed()
				+ "\nMacAddress:" + info.getMacAddress();

		int ip = info.getIpAddress();
		String ipHexStr = Integer.toHexString(ip);
		mIpInfoTextView.setText(ipHexStr);

		Log.i("wifi", "SSID: " + info.getSSID());
		Log.i("wifi", "IPAddress: " + ip);
		Log.i("wifi", "IPAddress String: " + intToIp(ip));
		Log.i("wifi", "LinkSpeed: " + info.getLinkSpeed());
		Log.i("wifi", "MacAddress: " + info.getMacAddress());

		Toast.makeText(DemoServerActivity.this, infoStr, Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 内部handler通信
	 * 
	 * @param what
	 */
	private void sendHanlderMessage(int what) {
		Message msg = mHandler.obtainMessage();
		msg.what = what;
		mHandler.sendMessage(msg);
	}

	/**
	 * 将int型ip转换为可读string型
	 * 
	 * @param i
	 * @return
	 */
	private String intToIp(int i) {

		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);

	}

	@Override
	public void finish() {
		if (null != mClientSocket) {
			try {
				mClientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.finish();
	}

}