package com.fusit.stitchutils.utils;


import com.fusit.stitchutils.network.Exclude;
import com.fusit.stitchutils.network.HttpUrl;
import com.fusit.stitchutils.network.RequestListener;
import com.fusit.stitchutils.network.RequestType;
import com.fusit.stitchutils.network.VRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tamarraviv.
 */
public class StructUtils {
    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }


    public static JsonObject jsonFromHashmap(HashMap<String,String> map){
        JsonObject json = new JsonObject();
        for(Map.Entry<String,String> entries:map.entrySet()){
            json.addProperty(entries.getKey(),entries.getValue());
        }
        return json;
    }

    public static Gson getGson() {
        Exclude ex = new Exclude();
        GsonBuilder gson_builder= new GsonBuilder();
        gson_builder.registerTypeAdapter(HttpUrl.class,new HttpUrl.HttpsDeserializer());
        gson_builder.excludeFieldsWithModifiers(Modifier.PRIVATE);
        gson_builder.addDeserializationExclusionStrategy(ex);
        gson_builder.addSerializationExclusionStrategy(ex);
        return gson_builder.create();
    }

//    public static ArrayList<FusicVideo> getVideosList(String resultString) {
//        JsonElement root = new JsonParser().parse(resultString);
//        JsonArray elements;
//        elements = root.getAsJsonArray();
//        ArrayList<FusicVideo> songsList = new ArrayList<>();
//        if(elements.size() > 0) {
////            ArrayList<FusicVideo> songsList = new ArrayList<>();
//            for (JsonElement element : elements) {
//                FusicVideo model = StructUtils.getGson().fromJson(element, FusicVideo.class);
//                songsList.add(model);
//            }
//        }
//        return songsList;
//    }

//    public static boolean isMyVideo(Long songOwnerId) {
//        Long myId = new Long(DataManager.getInstance().getSession().id);
//        return songOwnerId == myId.longValue();
//    }

//    public static ArrayList<FusicVideoPart> parsePartsResponse(String partsUrls) {
//        ArrayList<FusicVideoPart> videoPartsList = null;
//        JsonElement root = new JsonParser().parse(partsUrls);
//        JsonArray elements;
//        elements = root.getAsJsonArray();
//
//        if (elements.size() > 0) {
//            videoPartsList = new ArrayList<>();
//            for (JsonElement element : elements) {
//                FusicVideoPart part = StructUtils.getGson().fromJson(element, FusicVideoPart.class);
//                videoPartsList.add(part);
//            }
//        } else {
//            //no parts recieved
//        }
//        return videoPartsList;
//    }

//    public static void downloadVideoParts(long id, RequestListener listener, String packageName) {
//        Map<String, String> params = new HashMap<>();
//        params.put(ShareAndPreviewActivity.PARAM_VIDEO, Long.toString(id));
//        params.put("format", "mp4");
//        VRequest getVideoPartsRequest = RequestFactory.buildVideoPartsRequest(getShareRequestType(packageName), listener, params);
//        if(getVideoPartsRequest != null) {
//            RequestManager.getInstance().performRequest(getVideoPartsRequest);
//        } else {
//            FLog.e(listener.getClass().getSimpleName(), "callVideoPartsApi: getVideoPartsRequest is null");
//        }
//    }

//    private static RequestType getShareRequestType (String packageName) {
//        switch (packageName) {
//            case ShareUtils.INSTAGRAM_PACKAGE:
//                return RequestType.VIDEO_PARTS_INSTAGRAM;
//
//            case ShareUtils.FB_MESSENGER_PACKAGE:
//                return RequestType.VIDEO_PARTS_FB_MESSENGER;
//        }
//        return null;
//    }
}
