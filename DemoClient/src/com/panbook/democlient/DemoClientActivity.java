package com.panbook.democlient;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
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

public class DemoClientActivity extends Activity {

	private EditText mIpEditText;
	private Button mConnectServerButton;
	private TextView mRecieveContentTextView;
	private EditText mSendMessageEditText;
	private Button mSendMessageButton;
	
	private OutputStream mOutStream;
	private InputStream mInStream;
	
	private String mRecieveContent;
	private String mIpHexStr;
	private Handler mHandler;
	private Socket mClientSocket;
	private PrintWriter mPrintWriter;

	private final int PORT = 8886;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mIpEditText = (EditText) findViewById(R.id.fillIPEditText);
		mConnectServerButton = (Button) findViewById(R.id.connectServerButton);
		mRecieveContentTextView = (TextView) findViewById(R.id.recieveContentTV);
		mSendMessageEditText = (EditText) findViewById(R.id.sendMessageEditText);
		mSendMessageButton = (Button) findViewById(R.id.sendMessageButton);

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (!Thread.currentThread().isInterrupted()) {
					switch (msg.what) {
					// 连接成功
					case 0:
						Toast.makeText(DemoClientActivity.this,"Connect success!", Toast.LENGTH_LONG).show();
						break;
					// 连接失败
					case 1:
						Toast.makeText(DemoClientActivity.this,"Connect error!", Toast.LENGTH_LONG).show();
						break;
					// 显示接受消息
					case 2:
						mRecieveContentTextView.setText(mRecieveContent);
						break;
					}
				}
				super.handleMessage(msg);
			}
		};
		
		mConnectServerButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				mIpHexStr = mIpEditText.getText().toString();
				Thread thread = new Thread() {
					public void run() {
						connectToSever();
					}
				};
				thread.start();
				thread = null;
			}
		});

		mSendMessageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String msg = mSendMessageEditText.getText().toString();
//				mOutStream.write(msg.getBytes());
				if (null != mPrintWriter) {
					Log.i("send msg", msg);
					mPrintWriter.println(msg);
/*					try {
						mOutStream.write(msg.getBytes());
					} catch (IOException e) {
						Toast.makeText(DemoClientActivity.this,"write data error!", Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}*/
				}
			}
		});
	}

	/**
	 * 连接服务器
	 */
	private void connectToSever() {

		try {
			int ip = Integer.parseInt(mIpHexStr, 16);
			String ipStr = intToIp(ip);
			InetAddress serverAddr = InetAddress.getByName(ipStr);
			Log.d("TCP", "Connecting...");
			mClientSocket = new Socket(serverAddr, PORT);
			if (null != mClientSocket) {
				sendHanlderMessage(0);
				Log.d("TCP", "Connected");
			} else {
				sendHanlderMessage(1);
				Log.d("TCP", "Connect error!");
				return ;
			}
			mOutStream = mClientSocket.getOutputStream();
			mInStream = mClientSocket.getInputStream();
			DataInputStream input = new DataInputStream(mInStream);
			// 获取 Client 端的输出流
			mPrintWriter = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(mOutStream)), true);
			while (true) {
				Thread.sleep(100);
				mRecieveContent = input.readLine();
				if (null != mRecieveContent) {
					Log.i("recieve", mRecieveContent);
					sendHanlderMessage(2);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			sendHanlderMessage(1);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			sendHanlderMessage(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			sendHanlderMessage(1);
		} catch (IOException e) {
			e.printStackTrace();
			sendHanlderMessage(1);
		} finally {
			//mClientSocket.close();
		}
		if (null != mClientSocket) {
			sendHanlderMessage(0);
		}
	}

	/**
	 * 内部handler通信
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

/*		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);*/
		return "172.22." + (i & 0xFF) + "." + ((i >> 8) & 0xFF);
	}

	@Override
	/**
	 * 退出Activity，关闭连接。
	 */
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