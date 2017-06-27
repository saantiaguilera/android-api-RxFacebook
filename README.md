# RxFacebook

[![Build Status](https://travis-ci.org/saantiaguilera/android-api-RxFacebook.svg?branch=develop)](https://travis-ci.org/saantiaguilera/android-api-RxFacebook) [![Download](https://api.bintray.com/packages/saantiaguilera/maven/com.saantiaguilera.rx.rxfacebook/images/download.svg) ](https://bintray.com/saantiaguilera/maven/com.saantiaguilera.rx.rxfacebook/_latestVersion)

A reactive extension over the facebook SDK.

### Requirements

RxFacebook can be included in any Android application.

RxFacebook supports Android ApiLevel 14 and later.

RxJava and RxAndroid. They arent added as dependencies, but expects you to add them. It supports both 1.X and 2.X

### Description

This library simply wraps the `GraphRequest` and `LoginManager` facebook provides in its SDK and moves them to a stream based logic

### Relevant notes

- Instead of using `[4,5)` for the facebook dependency version. We are statically adding `4.5.0` since its the last one to give support to api 14. If you plan on using a newer one, please bear in mind that your minimum api level wont be this one
- RxJava and RxAndroid are used as provided dependencies, so you should add them to your project. This project supports both Rx 1.X and 2.X, use whichever you prefer :)

### Usage

#### Adding it to your project

Add in your `build.gradle`:

```gradle
dependencies {
    compile "com.saantiaguilera.rx:rxfacebook:<latest_version>"
}
```

#### Getting started

Create a `RxFacebook` instance and configure it as you like. There are built-in methods provided out of the box (the same that `GraphRequest` provides)

```Java
RxFacebook.create()
    .accessToken(token)
    .params(bundle)
    .graphPath(path)
    .httpMethod(HttpMethod.GET)
    .request() // From this point onwards, we are using a Observable<GraphResponse>
    .observeOn(AndroidSchedulers.mainThread())
    .subscribeOn(Schedulers.immediate())
    .map(new Func1() {
        @Override
        public SomeDto call(final GraphResponse response) {
            // This library is agnostic to your parsing method, use whichever you want to.
            // Here im using gson as example
            return gson.parse(response.getJSONObject(), SomeDto.class);
        }
    })
    .subscribe(...);
```

You can configure the following properties of a facebook request:
```Java
RxFacebook.create()         // Create a instance
    .accessToken(token)     // Set an access token if the request is authenticated
    .params(bundle)         // Set a bundle with field params for the request
    .tag(object)            // Set a tag as id of the request (in the response you
                            // can get it with response.getRequest().getTag())
    .version(string)        // Version of the graph api to use
    .httpMethod(method)     // Method of HTTP to use, if request doesnt include it
    .skipClientToken(bool)  // If it should skip the auth
    .graphPath(string)      // Path of the endpoint to hit (eg /users/me)
    .graphObject(JSONObj)   // Body of a post 
```

And you can call the following methods to build a request:
```Java
RxFacebook.create()                // Create a instance
    .request()                     // Perform a request with the HttpMethod/GraphPath/etc setted
    .requestMe()                   // Perform a request to fetch the authorized user
    .requestMyFriends()            // Perform a request to fetch the authorized user friends
    .requestUploadPhotos()         // Perform a request to upload a picture on the user account
    .requestPlacesSearch()         // Perform a request to search for places with some given params
    .request(GraphRequest req)     // Perform a request for the given GraphRequest
    .post()                        // Perform a POST with the GraphPath/GraphObject/etc setted
    .get()                         // Perform a GET with the GraphPath/params/etc setted
    .delete()                      // Perform a DELETE with the GraphPath/params/etc setted
    .loginWithReadPermissions()    // Perform a login with read permissions.
    .loginWithPublishPermissions() // Perform a login with publish permissions
    .logout()                      // Perform a logout
```

Note that if using `loginXXXX()` methods, it will start a facebook activity and you should override the `onActivityResult` of the context passed as param. You should call in your `onActivityResult` the following method:
```Java
// Returns a boolean telling if the activity result was processed or not
// Since it can be of another requestCode and not of a facebook login.
RxFacebook.postLoginActivityResult(requestCode, resultCode, data);
```
This should be called if login is used so that we can know how the operation finished and handle its result (the returned stream of the login method used will be used for broadcasting how it went).

### Notes

This library doesnt add any dependency for parsing the facebook responses nor it handles graphRequest errors as stream errors (it will be outputed as a `onNext(GraphResponse)` always). 

This is done like this so that the user has control over all the facebook variables (pagination, error status, connection status, rawResponse, tags, etc) and can use whichever JSON parser wants. 

**Its up to you to parse the response with `Gson`/`Jackson`/`whatever parser you use` into the DTO/Model/Entity you requested.**

**Its up to you to check the `graphResponse.getError() != null`, since the `onError()` of the stream is used for internal errors (malformed requests for example, or a login activity performed incorrectly)**
 
This shouldnt impose any restriction since we are using Rx. It should be as easy as this:
```Java
RxFacebook.create()
    ... // Customize
    .request()
    .observeOn(somewhere)
    .subscribeOn(somewhereElse)
    .filter(new Func1<GraphResponse, Boolean>() {
        @Override
        public Boolean call(final GraphResponse graphResponse) {
            return graphResponse.getError == null; // Filter only the ones without error
            // Or handle the error? This is just an example so its up to you...
        }
    })
    .map(new Func1<GraphResponse, MyDto>() {
        @Override
        public MyDto call(final GraphResponse graphResponse) {
            return yourParserOfChoice.parse(graphResponse.getJSONObject(), MyDto.class);
        }
    })
    .subscribe(...);
```