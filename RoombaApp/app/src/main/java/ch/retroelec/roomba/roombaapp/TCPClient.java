package ch.retroelec.roomba.roombaapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

enum TCPCmd {
    START, STOP, CLEAN, SPOT,
    DRIVEFWD, DRIVEBCK, DRIVELEFT, DRIVERIGHT, DRIVESTOP, MANUAL, BROOM,
    IRSENSOR, TEMPERATURE, BATTERY, XMAS
}

public class TCPClient extends AsyncTask<TCPCmd, Void, String> {
    private static final String TAG = TCPClient.class.getSimpleName();
    public static final int SERVER_PORT = 2002;
    public static final int MAXLENRESP = 16;
    private static DataOutputStream tcpOut;
    private static DataInputStream tcpIn;
    private static Socket socket = null;
    private static boolean broomOn = false;

    private void getConnection() {
        if ((socket == null) || (socket.isClosed())) {
            try {
                socket = new Socket(MainActivity.getIp(), SERVER_PORT);
                tcpOut = new DataOutputStream(socket.getOutputStream());
                tcpIn = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                Log.e(TAG, "getConnection", e);
                return;
            }
        }
    }

    private byte[] readnBytesResp(int n) throws IOException {
        byte[] resarr = new byte[MAXLENRESP];
        // wait max. 2 seconds
        socket.setSoTimeout(2000);
        for (int i = 0; i < n; i++) {
            resarr[i] = tcpIn.readByte();
        }
        socket.setSoTimeout(0);
        return resarr;
    }

    private String readnBytesRespStr(int n) {
        byte[] resarr;
        try {
            resarr = readnBytesResp(n);
        } catch (IOException e) {
            return "IOException";
        }
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < n; i++) {
            response.append((char) resarr[i]);
        }
        return response.toString();
    }

    @Override
    protected String doInBackground(TCPCmd... cmds) {
        try {
            TCPCmd cmd = cmds[0];
            byte[] bytes;
            switch (cmd) {
                case START:
                    // connect to server
                    getConnection();
                    tcpOut.write("start".getBytes());
                    tcpOut.flush();
                    return readnBytesRespStr(2);
                case STOP:
                    // close
                    if ((socket != null) && (socket.isConnected())) {
                        tcpOut.write("stop".getBytes());
                        tcpOut.flush();
                        try {
                            tcpIn.close();
                            tcpOut.close();
                            socket.close();
                        } catch (IOException e) {
                            Log.e(TAG, "closeConnection", e);
                            return "error in closeConnection";
                        }
                        socket = null;
                    }
                    return "ok";
                case CLEAN: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("cmd1".getBytes());
                    barr.write(CMDConsts.CLEAN);
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    return readnBytesRespStr(2);
                }
                case SPOT: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("cmd1".getBytes());
                    barr.write(CMDConsts.SPOT);
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    return readnBytesRespStr(2);
                }
                case DRIVEFWD: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("drive".getBytes());
                    barr.write(100);
                    barr.write(1);
                    barr.write(0);
                    barr.write(0x80);
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    return "";
                }
                case DRIVEBCK: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("drive".getBytes());
                    barr.write(0x9c);
                    barr.write(0xfe);
                    barr.write(0);
                    barr.write(0x80);
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    return "";
                }
                case DRIVELEFT: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("drive".getBytes());
                    barr.write(100);
                    barr.write(0);
                    barr.write(1);
                    barr.write(0);
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    return "";
                }
                case DRIVERIGHT: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("drive".getBytes());
                    barr.write(100);
                    barr.write(0);
                    barr.write(0xff);
                    barr.write(0xff);
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    return "";
                }
                case DRIVESTOP: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("drive".getBytes());
                    barr.write(0);
                    barr.write(0);
                    barr.write(0);
                    barr.write(0x80);
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    return "";
                }
                case MANUAL: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("cmd1".getBytes());
                    barr.write(CMDConsts.SAFE);
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    return readnBytesRespStr(2);
                }
                case BROOM: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("cmd2".getBytes());
                    barr.write(CMDConsts.MOTORS);
                    barr.write(1);
                    if (broomOn) {
                        barr.write(0);
                    }
                    else {
                        barr.write(7);
                    }
                    broomOn = !broomOn;
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    return readnBytesRespStr(2);
                }
                case IRSENSOR: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("sens".getBytes());
                    barr.write(1);
                    barr.write(17);
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    byte[] data = readnBytesResp(1);
                    return Integer.toString(data[0] & 0xFF);
                }
                case TEMPERATURE: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("sens".getBytes());
                    barr.write(1);
                    barr.write(24);
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    byte[] data = readnBytesResp(1);
                    return Integer.toString(data[0] & 0xFF);
                }
                case BATTERY: {
                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    barr.write("sens".getBytes());
                    barr.write(1);
                    barr.write(25);
                    bytes = barr.toByteArray();
                    tcpOut.write(bytes);
                    tcpOut.flush();
                    byte[] data = readnBytesResp(2);
                    int capacity = (data[0] & 0xFF) * 256 + (data[1] & 0xFF);
                    return Integer.toString(capacity);
                }
                case XMAS: {
                    tcpOut.write("xmas".getBytes());
                    tcpOut.flush();
                    return "XMAS";
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "execute tcp command", e);
        }
        return null;
    }

}
