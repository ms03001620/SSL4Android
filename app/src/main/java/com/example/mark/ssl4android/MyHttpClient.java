package com.example.mark.ssl4android;
import org.apache.http.conn.ssl.SSLSocketFactory;
import android.content.Context;

import org.apache.http.params.HttpParams;

import java.io.InputStream;
import java.security.KeyStore;

/**
 * Created by Mark on 2015/12/13.
 */
public class MyHttpClient /* extends DefaultHttpClient*/ {

    private static Context context;

    public static void setContext(Context context) {
        MyHttpClient.context = context;
    }

  /*  public MyHttpClient(HttpParams params) {
        super(params);
    }

    public MyHttpClient(ClientConnectionManager httpConnectionManager, HttpParams params) {
        super(httpConnectionManager, params);
    }

    @Override
    protected ClientConnectionManager createClientConnectionManager() {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        // 用我们自己定义的 SSLSocketFactory 在 ConnectionManager 中注册一个 443 端口
        registry.register(new Scheme("https", newSslSocketFactory(), 443));
        return new SingleClientConnManager(getParams(), registry);
    }
*/
    private SSLSocketFactory newSslSocketFactory() {
        try {
            // Get an instance of the Bouncy Castle KeyStore format
            KeyStore trusted = KeyStore.getInstance("bks");
            // 从资源文件中读取你自己创建的那个包含证书的 keystore 文件

            InputStream in = MyHttpClient.context.getResources().openRawResource(R.raw.codeprojectssl); //这个参数改成你的 keystore 文件名
            try {
                // 用 keystore 的密码跟证书初始化 trusted
                trusted.load(in, "这里是你的 keystore 密码".toCharArray());
            } finally {
                in.close();
            }
            // Pass the keystore to the SSLSocketFactory. The factory is responsible
            // for the verification of the server certificate.
            SSLSocketFactory sf = new SSLSocketFactory(trusted);
            // Hostname verification from certificate
            // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
            sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER); // 这个参数可以根据需要调整, 如果对主机名的验证不需要那么严谨, 可以将这个严谨程度调低些.
            return sf;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
