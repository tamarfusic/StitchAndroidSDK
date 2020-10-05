package com.fusit.stitchutils.network;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by tamarraviv.
 */
public class HttpUrl {
    public URL url;
    public static final String LOGGER = "aa.httpsurl";

    public HttpUrl(String original_url) throws MalformedURLException,URISyntaxException {
        url = new URI("https",original_url,null).toURL();
    }

    @Override
    public String toString(){
        return url.toString();
    }

    public static class HttpsDeserializer implements JsonDeserializer<HttpUrl> {
        @Override
        public HttpUrl deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try{
                String s = json.getAsString();
                HttpUrl https_url = new HttpUrl(s);
                return https_url;
            }catch(MalformedURLException e){
                deserializationFailed(e);
                return null;
            }catch (URISyntaxException e){
                deserializationFailed(e);
                return null;
            }
        }

        private void deserializationFailed(Exception e) {
            Log.e(LOGGER, "failed deserializing");
            Log.e(LOGGER, String.format("Can't construct url %s", e));
        }
    }
}
