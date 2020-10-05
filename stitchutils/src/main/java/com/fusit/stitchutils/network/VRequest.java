package com.fusit.stitchutils.network;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.fusit.stitchutils.utils.Character;
import com.fusit.stitchutils.utils.StructUtils;
import com.google.gson.Gson;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by tamarraviv.
 */
public class VRequest<T> extends Request<T> {
    private RequestListener requestListener;
    private ResponseType responseType = ResponseType.STRING;
    private Class<T> responseClass;
    private Type collectionType;
    private String body;
    private String contentType;
    private String charset = "utf-8";
    private RequestType requestType;
    private String videoPath;



    public VRequest(int method, String url, RequestType requestType, RequestListener requestListener, Response.ErrorListener listener) {
        super(method, url, listener);
        this.requestListener = requestListener;
        this.requestType = requestType;
        setRetryPolicy(new DefaultRetryPolicy(10000, 0 , 0));
    }

    public VRequest<T> setHeaders(Map<String, String> headers){
        Map<String, String> headers1 = headers;
        return this;
    }

    public VRequest<T> setBody(String body){
        this.body = body;
        return this;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Log.e("tamar","======================= in getHeaders!!!");
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Token a70f6d1f700e316061e90a417eb1ac2bba786c44");
        return headers;
    }

//    @Override
//    protected Map<String, String> getParams() throws AuthFailureError {
//        Log.e("tamar","======================= in getParams!!!");
//        if(videoPath!=null) {
//            Log.e("tamar","======================= in getParams videoPath="+videoPath);
//            Map<String, String> params = new HashMap<String, String>();
//            params.put("video", "@"+videoPath);
//            return params;
//        } else {
//            return super.getParams();
//        }
//    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (body == null) return super.getBody();
        try {
            if(TextUtils.isEmpty(charset)){
                return body.getBytes();
            }else{
                return body.getBytes(charset);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return super.getBody();
        }
    }



    //    RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//            .addFormDataPart("video","video2.mp4",
//                    RequestBody.create(MediaType.parse("application/octet-stream"),
//                            new File("/Users/binshtok/Downloads/video2.mp4")))
//            .build();



    @Override
    public String getBodyContentType(){
        if(!TextUtils.isEmpty(contentType) && !TextUtils.isEmpty(charset)){
            return String.format(Locale.US, "%s; charset=%s", contentType, charset);
        }else{
            return super.getBodyContentType();
        }
    }


    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public void setCollectionType(Type collectionType) {
        this.collectionType = collectionType;
    }

    public void setResponseClass(Class<T> responseClass) {
        this.responseClass = responseClass;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {

            String responseString = new String(response.data, charset);
            Log.e("tamar", "============== responseString="+responseString);
//            String responseString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//            if(requestType == RequestType.EVENT_RULES) { //FOLLOWED_VIDEOS || requestType == RequestType.FOLLOWED_USERS) {
//                Log.e("tamar","=======================");
//                String charsetstr = HttpHeaderParser.parseCharset(response.headers);
//                        Log.e("tamar", "responseString=" + responseString);
//                Log.e("tamar", "charsetstr=" + charsetstr);
//            }

            Object parsedResponse;
            switch (responseType) {
                case GSON:
                    parsedResponse = StructUtils.getGson().fromJson(responseString, responseClass);
                    break;
                case ARRAY:
                    parsedResponse = new Gson().fromJson(responseString, collectionType); //todo: check if need getGson for all options
                    break;
                case DOUBLE:
                    parsedResponse = Double.parseDouble(responseString);
                    break;
                case INTEGER:
                    parsedResponse = Integer.parseInt(responseString.trim());
                    break;
                case STRING:
                    parsedResponse = responseString;
                    break;
                default:
                    parsedResponse = responseString;
            }
//fails here!!
            return (Response<T>) Response.success(parsedResponse, HttpHeaderParser.parseCacheHeaders(response));

        } catch (Exception e) {
            requestListener.onResponseFail(requestType, new ParseError(e), null);
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        Log.e("tamar", "============== in deliverResponse calling onResponseSuccess"+response.getClass());
        Log.e("tamar", "=======response vid path="+((Character)response).getVideo_path());
        requestListener.onResponseSuccess(requestType, response, null);
      }

    public enum ResponseType{
        GSON,
        ARRAY,
        DOUBLE,
        INTEGER,
        STRING
    }
}
