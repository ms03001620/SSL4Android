package com.example.mark.ssl4android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTextView = (TextView)findViewById(R.id.text);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetTask task = new NetTask();
                task.execute(0);
            }
        });
    }

    class NetTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {

            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                //https://itconnect.uw.edu/security/securing-computer/install/
                InputStream caInput = getResources().openRawResource(R.raw.load_der2);
                final Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                    Log.i("Longer", "ca=" + ((X509Certificate) ca).getSubjectDN());
                    Log.i("Longer", "key=" + ((X509Certificate) ca).getPublicKey());
                } finally {
                    caInput.close();
                }
                // Create an SSLContext that uses our TrustManager
                SSLContext context = SSLContext.getInstance("TLSv1","AndroidOpenSSL");
                context.init(null, new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                for (X509Certificate cert : chain) {
                                    // Make sure that it hasn't expired.
                                    cert.checkValidity();
                                    // Verify the certificate's public key chain.
                                    try {
                                        cert.verify(((X509Certificate) ca).getPublicKey());
                                    } catch (NoSuchAlgorithmException e) {
                                        e.printStackTrace();
                                    } catch (InvalidKeyException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchProviderException e) {
                                        e.printStackTrace();
                                    } catch (SignatureException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                }, null);

                URL url = new URL("https://certs.cac.washington.edu/CAtest/");
                HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
                urlConnection.setSSLSocketFactory(context.getSocketFactory());
                InputStream in = urlConnection.getInputStream();
                copyInputStreamToOutputStream(in, System.out);
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public static void copyInputStreamToOutputStream(final InputStream in, final OutputStream out) throws IOException {
        try {
            try {
                final byte[] buffer = new byte[1024];
                int n;
                while ((n = in.read(buffer)) != -1)
                    out.write(buffer, 0, n);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}

