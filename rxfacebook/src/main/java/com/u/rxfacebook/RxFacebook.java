package com.u.rxfacebook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import org.json.JSONObject;
import rx.Observable;
import rx.functions.Action0;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * ReactiveX Facebook extension. This class lets you interact with most of the facebook api.
 * Created by saguilera on 6/27/17.
 */
public final class RxFacebook {

    private @Nullable AccessToken accessToken;

    private @Nullable Bundle params;

    private @Nullable String tag;
    private @Nullable String version;
    private @Nullable HttpMethod httpMethod;
    private boolean skipClientToken;
    private @Nullable String graphPath;
    private @Nullable JSONObject graphObject;

    private static @Nullable LoginImpl loginImpl;

    private RxFacebook() {
        // Default private constructor
    }

    /**
     * Create an empty request builder
     * @return
     */
    public static @NonNull RxFacebook create() {
        return new RxFacebook();
    }

    /**
     * Set the request tag
     * @param tag for the request
     * @return builder instance
     */
    public @NonNull RxFacebook tag(@NonNull String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Set the version to use of the graph
     * @param version to use
     * @return builder instance
     */
    public @NonNull RxFacebook version(@NonNull String version) {
        this.version = version;
        return this;
    }

    /**
     * If you wont use a default mode or post/delete/get, this provides a particular HttpMethod
     * @param httpMethod to use
     * @return builder instance
     */
    public @NonNull RxFacebook httpMethod(@NonNull HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * @param skipClientToken if it should or not skip the client accessToken
     * @return builder instance
     * By default its false.
     */
    public @NonNull RxFacebook skipClientToken(@NonNull boolean skipClientToken) {
        this.skipClientToken = skipClientToken;
        return this;
    }

    /**
     * Graph path to use.
     * If using a specific domain method (eg requesting my user (ME)) this param is ignored
     * @param graphPath to use as route of the endpoint
     * @return builder instance
     */
    public @NonNull RxFacebook graphPath(@NonNull String graphPath) {
        this.graphPath = graphPath;
        return this;
    }

    /**
     * @param graphObject to use as body of the endpoint in a POST
     * @return builder instance
     */
    public @NonNull RxFacebook graphObject(@NonNull JSONObject graphObject) {
        this.graphObject = graphObject;
        return this;
    }

    /**
     * @param params of the request
     * @return builder instance
     */
    public @NonNull RxFacebook params(@NonNull Bundle params) {
        this.params = params;
        return this;
    }

    /**
     * @param accessToken to use in the request auth
     * @return builder instance
     */
    public @NonNull RxFacebook accessToken(@NonNull AccessToken accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Request my user
     * <b>Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)</b>
     * @return observable of a graph response
     */
    public @NonNull Observable<GraphResponse> requestMe() {
        return request(GraphRequest.newMeRequest(accessToken, null));
    }

    /**
     * Request my friends
     * <b>Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)</b>
     * @return observable of a graph response
     */
    public @NonNull Observable<GraphResponse> requestMyFriends() {
        return request(GraphRequest.newMyFriendsRequest(accessToken, null));
    }

    /**
     * Upload a picture
     *
     * <b>Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)</b>
     *
     * @param image to upload
     * @param caption optional. caption of the image
     * @return observable of a graph response
     */
    public @NonNull Observable<GraphResponse> requestUploadPhotos(@NonNull Bitmap image, @Nullable String caption) {
        return request(GraphRequest.newUploadPhotoRequest(
            accessToken,
            graphPath,
            image,
            caption,
            params,
            null));
    }

    /**
     * Upload a picture
     *
     * <b>Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)</b>
     *
     * @param image to upload
     * @param caption optional. caption of the image
     * @return observable of a graph response
     */
    public @NonNull Observable<GraphResponse> requestUploadPhotos(@NonNull File image, @Nullable String caption) throws FileNotFoundException {
        return request(GraphRequest.newUploadPhotoRequest(
            accessToken,
            graphPath,
            image,
            caption,
            params,
            null));
    }

    /**
     * Upload a picture
     *
     * <b>Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)</b>
     *
     * @param image to upload
     * @param caption optional. caption of the image
     * @return observable of a graph response
     */
    public @NonNull Observable<GraphResponse> requestUploadPhotos(@NonNull Uri image, @Nullable String caption) throws FileNotFoundException {
        return request(GraphRequest.newUploadPhotoRequest(
            accessToken,
            graphPath,
            image,
            caption,
            params,
            null));
    }

    /**
     * <b>Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)</b>
     * @param where to search
     * @param radiusLimit limit radius
     * @param radiusMeters meters of radius
     * @param searchText text
     * @return observable of a graph response
     */
    public @NonNull Observable<GraphResponse> requestPlacesSearch(@NonNull Location where, int radiusLimit, int radiusMeters, @Nullable String searchText) {
        return request(GraphRequest.newPlacesSearchRequest(
            accessToken,
            where,
            radiusMeters,
            radiusLimit,
            searchText,
            null
        ));
    }

    /**
     * Perform a POST request with this request builder attributes
     * <b>Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)</b>
     * @return observable of a graph response
     */
    public @NonNull Observable<GraphResponse> post() {
        return request(GraphRequest.newPostRequest(accessToken, graphPath,  graphObject, null));
    }

    /**
     * Perform a DELETE request with this request builder attributes
     * <b>Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)</b>
     * @return observable of a graph response
     */
    public @NonNull Observable<GraphResponse> delete() {
        return request(GraphRequest.newDeleteObjectRequest(accessToken, graphPath, null));
    }

    /**
     * Perform a GET request with this request builder attributes
     * <b>Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)</b>
     * @return observable of a graph response
     */
    public @NonNull Observable<GraphResponse> get() {
        return request(new GraphRequest(accessToken, graphPath, params, HttpMethod.GET));
    }

    /**
     * Perform a request with this request builder attributes
     * <b>Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)</b>
     * @return observable of a graph response
     */
    public @NonNull Observable<GraphResponse> request() {
        return request(new GraphRequest());
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with read permissions.
     *
     * Note that one should call in the onActivityResult of the {@param context}
     * {@link #postLoginActivityResult(int, int, Intent)}, since we dont have control over the activity
     *
     * If theres an error on the login phase it will be sent to the error stream
     */
    public @NonNull Observable<LoginResult> loginWithReadPermissions(@NonNull final Activity context, final Collection<String> permissions) {
        Action0 action0 = new Action0() {
            @Override
            public void call() {
                LoginManager.getInstance().logInWithPublishPermissions(context, permissions);
            }
        };
        return login(action0);
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with read permissions.
     *
     * Note that one should call in the onActivityResult of the {@param context}
     * {@link #postLoginActivityResult(int, int, Intent)}, since we dont have control over the activity
     *
     * If theres an error on the login phase it will be sent to the error stream
     */
    public @NonNull Observable<LoginResult> loginWithReadPermissions(@NonNull final Fragment context, final Collection<String> permissions) {
        Action0 action0 = new Action0() {
            @Override
            public void call() {
                LoginManager.getInstance().logInWithPublishPermissions(context, permissions);
            }
        };
        return login(action0);
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with write permissions.
     *
     * Note that one should call in the onActivityResult of the {@param context}
     * {@link #postLoginActivityResult(int, int, Intent)}, since we dont have control over the activity
     *
     * If theres an error on the login phase it will be sent to the error stream
     */
    public @NonNull Observable<LoginResult> loginWithPublishPermissions(@NonNull final Activity context, final Collection<String> permissions) {
        Action0 action0 = new Action0() {
            @Override
            public void call() {
                LoginManager.getInstance().logInWithPublishPermissions(context, permissions);
            }
        };
        return login(action0);
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with write permissions.
     *
     * Note that one should call in the onActivityResult of the {@param context}
     * {@link #postLoginActivityResult(int, int, Intent)}, since we dont have control over the activity
     *
     * If theres an error on the login phase it will be sent to the error stream
     */
    public @NonNull Observable<LoginResult> logInWithPublishPermissions(@NonNull final Fragment context, final Collection<String> permissions) {
        Action0 action0 = new Action0() {
            @Override
            public void call() {
                LoginManager.getInstance().logInWithPublishPermissions(context, permissions);
            }
        };
        return login(action0);
    }

    /**
     * Should be ran on the UI thread
     * Logs out of facebook.
     */
    public @NonNull Observable<Void> logout() {
        if (loginImpl != null) {
            loginImpl.shutdown();
            loginImpl = null;
        }

        return Observable.<Void>just(null)
            .doOnSubscribe(new Action0() {
                @Override
                public void call() {
                    LoginManager.getInstance().logOut();
                }
            });
    }

    /**
     * Perform a simple login agnostic to the type
     * @param action to execute when subscribed
     * @return observable of a login result
     */
    private @NonNull Observable<LoginResult> login(@NonNull Action0 action) {
        if (loginImpl != null) {
            loginImpl.shutdown();
        }

        PublishSubject<LoginResult> subject = PublishSubject.create();
        loginImpl = new LoginImpl(subject);
        subject.doOnSubscribe(action);
        return subject;
    }

    /**
     * Post the login results to be processed. The initial stream used will output its results/error
     * @param requestCode code used for request
     * @param resultCode result code
     * @param data of the result
     * @return if the on activity result could be parsed or not (maybe it wasnt a facebook intent the one started?)
     */
    public static boolean postLoginActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        if (loginImpl != null) {
            boolean result = loginImpl.onActivityResult(requestCode, resultCode, data);
            loginImpl = null;
            return result;
        }
        return false;
    }

    /**
     * Perform a request over a given {@link GraphRequest}
     * @param request to execute
     * @return observable of a graph response
     */
    public @NonNull Observable<GraphResponse> request(final @NonNull GraphRequest request) {
        final PublishSubject<GraphResponse> responseSubject = PublishSubject.create();

        if (httpMethod != null && request.getHttpMethod() == null) {
            request.setHttpMethod(httpMethod);
        }

        if (accessToken != null && request.getAccessToken() == null) {
            request.setAccessToken(accessToken);
        }

        if (params != null && request.getParameters() == null) {
            request.setParameters(params);
        }

        if (tag != null && request.getTag() == null) {
            request.setTag(tag);
        }

        if (version != null && request.getVersion() == null) {
            request.setVersion(version);
        }

        request.setSkipClientToken(skipClientToken);

        request.setCallback(new GraphRequest.Callback() {
            @Override
            public void onCompleted(final GraphResponse response) {
                responseSubject.onNext(response);
                responseSubject.onCompleted();
            }
        });

        return responseSubject.doOnSubscribe(new Action0() {
            @Override
            public void call() {
                request.executeAndWait();
            }
        });
    }

    /**
     * Internal class to use as bridge for the facebook login
     */
    private static class LoginImpl implements FacebookCallback<LoginResult> {

        @NonNull
        private Subject<LoginResult, LoginResult> subject;
        @Nullable
        private CallbackManager facebookCallback;

        /**
         * Constructor
         * @param subject to communicate the outcome
         */
        LoginImpl(@NonNull Subject<LoginResult, LoginResult> subject) {
            this.subject = subject;
            facebookCallback = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(facebookCallback, this);
        }

        /**
         * Shutdown the impl
         */
        void shutdown() {
            facebookCallback = null;
            subject.onCompleted();
        }

        /**
         * Trigger.
         * @param requestCode code
         * @param resultCode code
         * @param data data
         * @return if was processed or not the info.
         */
        boolean onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
            if (facebookCallback != null) {
                boolean processed = facebookCallback.onActivityResult(requestCode, resultCode, data);
                facebookCallback = null;
                return processed;
            }
            return false;
        }

        @Override
        public void onSuccess(final LoginResult loginResult) {
            subject.onNext(loginResult);
            subject.onCompleted();
        }

        @Override
        public void onCancel() {
            subject.onCompleted();
        }

        @Override
        public void onError(final FacebookException error) {
            subject.onError(error);
        }

    }

}
