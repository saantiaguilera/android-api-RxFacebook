package com.u.testapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.facebook.AccessToken
import com.facebook.FacebookSdk
import com.u.rxfacebook.RxFacebook
import java.util.ArrayList
import junit.framework.Assert
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    internal var accessToken: AccessToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(applicationContext)
        }

        setContentView(R.layout.activity_main)

        setLoginView()
        setMeView()
        setMyFriendsView()
        setCustomRequestView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        RxFacebook.postLoginActivityResult(requestCode, resultCode, data)
    }

    internal fun setCustomRequestView() {
        findViewById(R.id.activity_main_request_something).setOnClickListener {
            RxFacebook.create()
                    .version("2.9")
                    .graphPath("/2900")
                    .accessToken(accessToken!!)
                    .get() // Change here for request adding a HttpMethod.GET and other ways!
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .subscribe { graphResponse -> Log.w("RxFacebook", graphResponse.rawResponse) }
        }
    }

    internal fun setMyFriendsView() {
        findViewById(R.id.activity_main_request_friends).setOnClickListener { v ->
            RxFacebook.create()
                    .tag(v)
                    .accessToken(accessToken!!)
                    .requestMyFriends()
                    .observeOn(Schedulers.computation())
                    .subscribeOn(Schedulers.computation())
                    .subscribe { graphResponse ->
                        Assert.assertEquals(v, graphResponse.request.tag)
                        Log.w("RxFacebook", graphResponse.rawResponse)
                    }
        }
    }

    internal fun setMeView() {
        findViewById(R.id.activity_main_request_me).setOnClickListener { v ->
            RxFacebook.create()
                    .tag(v)
                    .accessToken(accessToken!!)
                    .requestMe()
                    .observeOn(Schedulers.computation())
                    .subscribeOn(Schedulers.computation())
                    .subscribe { graphResponse ->
                        Assert.assertEquals(v, graphResponse.request.tag)
                        Log.w("RxFacebook", graphResponse.rawResponse)
                    }
        }
    }

    internal fun setLoginView() {
        if (AccessToken.getCurrentAccessToken() != null) {
            accessToken = AccessToken.getCurrentAccessToken()
            (findViewById(R.id.activity_main_login) as TextView).text = "Logged in, click to logout! Token: " + accessToken!!
        }

        findViewById(R.id.activity_main_login).setOnClickListener { v ->
            val view = v as TextView
            if (accessToken != null) {
                RxFacebook.create()
                        .logout()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe {
                            accessToken = null
                            view.text = "Logged out. Click to login!"
                        }
            } else {
                RxFacebook.create()
                        .loginWithReadPermissions(this@MainActivity, ArrayList<String>())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe { loginResult ->
                            accessToken = loginResult.accessToken
                            view.text = "Logged in, click to logout! Token: " + loginResult.accessToken
                        }
            }
        }
    }

}
