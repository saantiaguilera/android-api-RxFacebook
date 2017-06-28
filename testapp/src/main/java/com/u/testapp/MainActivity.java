package com.u.testapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.u.rxfacebook.RxFacebook;
import java.util.ArrayList;
import junit.framework.Assert;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(getApplicationContext());
        }

        setContentView(R.layout.activity_main);

        setLoginView();
        setMeView();
        setMyFriendsView();
        setCustomRequestView();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        RxFacebook.postLoginActivityResult(requestCode, resultCode, data);
    }

    void setCustomRequestView() {
        findViewById(R.id.activity_main_request_something)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    RxFacebook.create()
                        .version("2.9")
                        .graphPath("/2900")
                        .accessToken(accessToken)
                        .get() // Change here for request adding a HttpMethod.GET and other ways!
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<GraphResponse>() {
                            @Override
                            public void call(final GraphResponse graphResponse) {
                                Log.w("RxFacebook", graphResponse.getRawResponse());
                            }
                        });
                }
            });
    }

    void setMyFriendsView() {
        findViewById(R.id.activity_main_request_friends)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    RxFacebook.create()
                        .tag(v)
                        .accessToken(accessToken)
                        .requestMyFriends()
                        .observeOn(Schedulers.computation())
                        .subscribeOn(Schedulers.computation())
                        .subscribe(new Action1<GraphResponse>() {
                            @Override
                            public void call(final GraphResponse graphResponse) {
                                Assert.assertEquals(v, graphResponse.getRequest().getTag());
                                Log.w("RxFacebook", graphResponse.getRawResponse());
                            }
                        });
                }
            });
    }

    void setMeView() {
        findViewById(R.id.activity_main_request_me)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    RxFacebook.create()
                        .tag(v)
                        .accessToken(accessToken)
                        .requestMe()
                        .observeOn(Schedulers.computation())
                        .subscribeOn(Schedulers.computation())
                        .subscribe(new Action1<GraphResponse>() {
                            @Override
                            public void call(final GraphResponse graphResponse) {
                                Assert.assertEquals(v, graphResponse.getRequest().getTag());
                                Log.w("RxFacebook", graphResponse.getRawResponse());
                            }
                        });
                }
            });
    }

    void setLoginView() {
        if (AccessToken.getCurrentAccessToken() != null) {
            accessToken = AccessToken.getCurrentAccessToken();
            ((TextView) findViewById(R.id.activity_main_login))
                .setText("Logged in, click to logout! Token: " + accessToken);
        }

        findViewById(R.id.activity_main_login)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final TextView view = (TextView) v;
                    if (accessToken != null) {
                        RxFacebook.create()
                            .logout()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Action1<Void>() {
                                @Override
                                public void call(final Void aVoid) {
                                    accessToken = null;
                                    view.setText("Logged out. Click to login!");
                                }
                            });
                    } else {
                        RxFacebook.create()
                            .loginWithReadPermissions(MainActivity.this, new ArrayList<String>())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Action1<LoginResult>() {
                                @Override
                                public void call(final LoginResult loginResult) {
                                    accessToken = loginResult.getAccessToken();
                                    view.setText("Logged in, click to logout! Token: " + loginResult.getAccessToken());
                                }
                            });
                    }
                }
            });
    }

}
