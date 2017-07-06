package com.u.rxfacebook

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import java.io.File
import java.io.FileNotFoundException
import org.json.JSONObject
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subjects.Subject

/**
 * ReactiveX Facebook extension. This class lets you interact with most of the facebook api.
 * Created by saguilera on 6/27/17.
 */
class RxFacebook private constructor() {

    private var accessToken: AccessToken? = null

    private var params: Bundle? = null

    private var tag: Any? = null
    private var version: String? = null
    private var httpMethod: HttpMethod? = null
    private var skipClientToken: Boolean = false
    private var graphPath: String? = null
    private var graphObject: JSONObject? = null

    /**
     * Set the request tag
     * @param tag for the request
     * *
     * @return builder instance
     */
    fun tag(tag: Any): RxFacebook {
        this.tag = tag
        return this
    }

    /**
     * Set the version to use of the graph
     * @param version to use
     * *
     * @return builder instance
     */
    fun version(version: String): RxFacebook {
        this.version = version
        return this
    }

    /**
     * If you wont use a default mode or post/delete/get, this provides a particular HttpMethod
     * @param httpMethod to use
     * *
     * @return builder instance
     */
    fun httpMethod(httpMethod: HttpMethod): RxFacebook {
        this.httpMethod = httpMethod
        return this
    }

    /**
     * @param skipClientToken if it should or not skip the client accessToken
     * *
     * @return builder instance
     * * By default its false.
     */
    fun skipClientToken(skipClientToken: Boolean): RxFacebook {
        this.skipClientToken = skipClientToken
        return this
    }

    /**
     * Graph path to use.
     * If using a specific domain method (eg requesting my user (ME)) this param is ignored
     * @param graphPath to use as route of the endpoint
     * *
     * @return builder instance
     */
    fun graphPath(graphPath: String): RxFacebook {
        this.graphPath = graphPath
        return this
    }

    /**
     * @param graphObject to use as body of the endpoint in a POST
     * *
     * @return builder instance
     */
    fun graphObject(graphObject: JSONObject): RxFacebook {
        this.graphObject = graphObject
        return this
    }

    /**
     * @param params of the request
     * *
     * @return builder instance
     */
    fun params(params: Bundle): RxFacebook {
        this.params = params
        return this
    }

    /**
     * @param accessToken to use in the request auth
     * *
     * @return builder instance
     */
    fun accessToken(accessToken: AccessToken): RxFacebook {
        this.accessToken = accessToken
        return this
    }

    /**
     * Request my user
     * **Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)**
     * @return observable of a graph response
     */
    fun requestMe(): Observable<GraphResponse> {
        return request(GraphRequest.newMeRequest(accessToken, null))
    }

    /**
     * Request my friends
     * **Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)**
     * @return observable of a graph response
     */
    fun requestMyFriends(): Observable<GraphResponse> {
        return request(GraphRequest.newMyFriendsRequest(accessToken, null))
    }

    /**
     * Upload a picture

     * **Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)**

     * @param image to upload
     * *
     * @param caption optional. caption of the image
     * *
     * @return observable of a graph response
     */
    fun requestUploadPhotos(image: Bitmap, caption: String?): Observable<GraphResponse> {
        return request(GraphRequest.newUploadPhotoRequest(
                accessToken,
                graphPath,
                image,
                caption,
                params, null))
    }

    /**
     * Upload a picture

     * **Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)**

     * @param image to upload
     * *
     * @param caption optional. caption of the image
     * *
     * @return observable of a graph response
     */
    @Throws(FileNotFoundException::class)
    fun requestUploadPhotos(image: File, caption: String?): Observable<GraphResponse> {
        return request(GraphRequest.newUploadPhotoRequest(
                accessToken,
                graphPath,
                image,
                caption,
                params, null))
    }

    /**
     * Upload a picture

     * **Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)**

     * @param image to upload
     * *
     * @param caption optional. caption of the image
     * *
     * @return observable of a graph response
     */
    @Throws(FileNotFoundException::class)
    fun requestUploadPhotos(image: Uri, caption: String?): Observable<GraphResponse> {
        return request(GraphRequest.newUploadPhotoRequest(
                accessToken,
                graphPath,
                image,
                caption,
                params, null))
    }

    /**
     * **Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)**
     * @param where to search
     * *
     * @param radiusLimit limit radius
     * *
     * @param radiusMeters meters of radius
     * *
     * @param searchText text
     * *
     * @return observable of a graph response
     */
    fun requestPlacesSearch(where: Location, radiusLimit: Int, radiusMeters: Int, searchText: String?): Observable<GraphResponse> {
        return request(GraphRequest.newPlacesSearchRequest(
                accessToken,
                where,
                radiusMeters,
                radiusLimit,
                searchText, null
        ))
    }

    /**
     * Perform a POST request with this request builder attributes
     * **Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)**
     * @return observable of a graph response
     */
    fun post(): Observable<GraphResponse> {
        return request(GraphRequest.newPostRequest(accessToken, graphPath, graphObject, null))
    }

    /**
     * Perform a DELETE request with this request builder attributes
     * **Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)**
     * @return observable of a graph response
     */
    fun delete(): Observable<GraphResponse> {
        return request(GraphRequest.newDeleteObjectRequest(accessToken, graphPath, null))
    }

    /**
     * Perform a GET request with this request builder attributes
     * **Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)**
     * @return observable of a graph response
     */
    fun get(): Observable<GraphResponse> {
        return request(GraphRequest(accessToken, graphPath, params, HttpMethod.GET))
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with read permissions.

     * Note that one should call in the onActivityResult of the {@param context}
     * [.postLoginActivityResult], since we dont have control over the activity

     * If theres an error on the login phase it will be sent to the error stream
     */
    fun loginWithReadPermissions(context: Activity, permissions: Collection<String>): Observable<LoginResult> {
        return login { LoginManager.getInstance().logInWithPublishPermissions(context, permissions) }
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with read permissions.

     * Note that one should call in the onActivityResult of the {@param context}
     * [.postLoginActivityResult], since we dont have control over the activity

     * If theres an error on the login phase it will be sent to the error stream
     */
    fun loginWithReadPermissions(context: Fragment, permissions: Collection<String>): Observable<LoginResult> {
        return login { LoginManager.getInstance().logInWithPublishPermissions(context, permissions) }
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with write permissions.

     * Note that one should call in the onActivityResult of the {@param context}
     * [.postLoginActivityResult], since we dont have control over the activity

     * If theres an error on the login phase it will be sent to the error stream
     */
    fun loginWithPublishPermissions(context: Activity, permissions: Collection<String>): Observable<LoginResult> {
        return login { LoginManager.getInstance().logInWithPublishPermissions(context, permissions) }
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with write permissions.

     * Note that one should call in the onActivityResult of the {@param context}
     * [.postLoginActivityResult], since we dont have control over the activity

     * If theres an error on the login phase it will be sent to the error stream
     */
    fun loginWithPublishPermissions(context: Fragment, permissions: Collection<String>): Observable<LoginResult> {
        return login { LoginManager.getInstance().logInWithPublishPermissions(context, permissions) }
    }

    /**
     * Should be ran on the UI thread
     * Logs out of facebook.
     */
    fun logout(): Observable<Void> {
        loginImpl?.shutdown()
        loginImpl = null

        return Observable.just<Void>(null)
                .doOnSubscribe { LoginManager.getInstance().logOut() }
    }

    /**
     * Perform a simple login agnostic to the type
     * @param action to execute when subscribed
     * *
     * @return observable of a login result
     */
    private fun login(action: () -> Unit): Observable<LoginResult> {
        loginImpl?.shutdown()

        val subject = PublishSubject.create<LoginResult>()
        loginImpl = LoginImpl(subject)
        return subject.doOnSubscribe { action() }
    }

    /**
     * Perform a request over a given [GraphRequest]
     * @param request to execute
     * *
     * @return observable of a graph response
     */
    @JvmOverloads fun request(request: GraphRequest = GraphRequest()): Observable<GraphResponse> {
        val responseSubject = PublishSubject.create<GraphResponse>()

        if (httpMethod != null && request.httpMethod == HttpMethod.GET) { // It wont be null, default is GET
            request.httpMethod = httpMethod
        }

        if (accessToken != null && request.accessToken == null) {
            request.accessToken = accessToken
        }

        if (params != null && request.parameters == null) {
            request.parameters = params
        }

        if (tag != null && request.tag == null) {
            request.tag = tag
        }

        if (version != null && request.version == null) {
            request.version = version
        }

        request.setSkipClientToken(skipClientToken)

        request.callback = GraphRequest.Callback { response ->
            // This is done only to get out of the subscribing msg
            Observable.just(response)
                    .observeOn(Schedulers.computation())
                    .subscribeOn(Schedulers.immediate())
                    .subscribe { graphResponse ->
                        responseSubject.onNext(graphResponse)
                        responseSubject.onCompleted()
                    }
        }

        return responseSubject.doOnSubscribe { request.executeAndWait() }
    }

    /**
     * Internal class to use as bridge for the facebook login
     */
    private class LoginImpl internal constructor(private val subject: Subject<LoginResult, LoginResult>) : FacebookCallback<LoginResult> {

        private var facebookCallback: CallbackManager?

        init {
            facebookCallback = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(facebookCallback!!, this)
        }

        /**
         * Shutdown the impl
         */
        internal fun shutdown() {
            facebookCallback = null
            subject.onCompleted()
        }

        /**
         * Trigger.
         * @param requestCode code
         * *
         * @param resultCode code
         * *
         * @param data data
         * *
         * @return if was processed or not the info.
         */
        internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
            if (facebookCallback != null) {
                val processed = facebookCallback!!.onActivityResult(requestCode, resultCode, data)
                facebookCallback = null
                return processed
            }
            return false
        }

        override fun onSuccess(loginResult: LoginResult) {
            subject.onNext(loginResult)
            subject.onCompleted()
        }

        override fun onCancel() {
            subject.onCompleted()
        }

        override fun onError(error: FacebookException) {
            subject.onError(error)
        }

    }

    companion object {

        private var loginImpl: LoginImpl? = null

        /**
         * Create an empty request builder
         * @return
         */
        fun create(): RxFacebook {
            return RxFacebook()
        }

        /**
         * Post the login results to be processed. The initial stream used will output its results/error
         * @param requestCode code used for request
         * *
         * @param resultCode result code
         * *
         * @param data of the result
         * *
         * @return if the on activity result could be parsed or not (maybe it wasnt a facebook intent the one started?)
         */
        fun postLoginActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
            if (loginImpl != null) {
                val result = loginImpl!!.onActivityResult(requestCode, resultCode, data)
                loginImpl = null
                return result
            }
            return false
        }

    }

}