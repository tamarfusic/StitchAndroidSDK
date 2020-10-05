package com.fusit.stitchutils.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

//import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by tamarraviv.
 */
public class FileHelpers {

    public static final String LOGGER = "FILEHELPERS";

    public static HashMap<Boolean,HashMap<Boolean,ArrayList<File>>> dirsMap;
    public static boolean FILE_TYPE_CACHE=true;
    public static boolean FILE_TYPE_PERMANENT=false;

    private static Long lastRecalculation=0l;

    //cache/files -> external/internal -> files
    public static HashMap<Boolean,HashMap<Boolean,FileSpace>> largestDirs;


    private static class FileSpace{
        public final File file;
        public final int space;

        FileSpace(File file, int space){
            this.file=file;
            this.space=space;
        }
    }

    public static class NoValidDir extends Exception {

    }

    private static void resetAllFilesData(Context context) {
        largestDirs = null;
        lastRecalculation = 0l;
        dirsMap = null;
    }

    public static void buildDirsMap(Context context){
        if(dirsMap!=null){
            return;
        }
        dirsMap = new HashMap<Boolean,HashMap<Boolean,ArrayList<File>>>();
        fillMap(dirsMap,true,context);
        fillMap(dirsMap, false, context);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static synchronized void fillMap(HashMap<Boolean, HashMap<Boolean, ArrayList<File>>> dirsMap, boolean cache, Context context) {
//        FLog.i(LOGGER,"Building folders map");
        HashMap<Boolean, ArrayList<File>> typeDirsMap = new HashMap<Boolean, ArrayList<File>>();
        ArrayList<File> externalDirsList = new ArrayList<File>();
        ArrayList<File> innerDirsList = new ArrayList<File>();
        typeDirsMap.put(true, externalDirsList);
        typeDirsMap.put(false, innerDirsList);

        File[] externalDirs = null;
        File innerDir = null;
        if(cache){
            if(context != null) {
                innerDir = context.getCacheDir();
            }
        }else{
            if(context != null) {
                innerDir = context.getFilesDir();
            }
        }

        try{
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                int currentapiVersion = Build.VERSION.SDK_INT;
                if(cache){
                    if (currentapiVersion >= Build.VERSION_CODES.KITKAT){
                        externalDirs = context.getExternalCacheDirs();
                    }else{
                        externalDirs = new File[]{context.getExternalCacheDir()};
                    }
                }else{
                    if (currentapiVersion >= Build.VERSION_CODES.KITKAT){
                        externalDirs = context.getExternalFilesDirs(null);
                    }else{
                        externalDirs = new File[]{context.getExternalFilesDir(null)};

                    }
                }
            }
        }catch (Exception e){
//            FLog.e(LOGGER,"Error happened trying to receive dirs list "+e.toString());
        }
        if(externalDirs!=null){
            for(File dir:externalDirs){
                verifyAndAddDir(externalDirsList, dir);
            }
            if(innerDir != null) {
                verifyAndAddDir(innerDirsList, innerDir);
            }
        }
        dirsMap.put(cache,typeDirsMap);
    }

    public static synchronized File bestDirBySize(Context context,boolean cache, int minMegabytes) throws NoValidDir{
        buildDirsMap(context);
        if(largestDirs==null ||  DateHelpers.unixTime() - lastRecalculation > 5){
            recalculateLargestDir();
        }
        HashMap<Boolean, FileSpace> currentMap = largestDirs.get(cache);

        //Order of false/true represents order of where try to get access first, internal or external
        for(Boolean isExternal:new Boolean[]{false,true}){
            FileSpace fileSpace = currentMap.get(isExternal);
            if(fileSpace!=null && fileSpace.space > minMegabytes){
//                FLog.i(LOGGER, String.format("Selected matching folder for %d mb: %s with %d available", minMegabytes, fileSpace.file, fileSpace.space));
                return fileSpace.file;
            }
        }

        printCurrentMap();
        NoValidDir noValidDirException = new NoValidDir();
//        Crashlytics.logException(noValidDirException);
        throw noValidDirException;
    }

    private static void printCurrentMap() {
        if(dirsMap==null){
            Log.e(LOGGER,"Current map is null, should not be");
            return;
        }

        Log.e(LOGGER,"Current map is:");
        Boolean[] booleans = new Boolean[]{true,false};
        try{
            for(Boolean cache:booleans){
                for(Boolean isExternal:booleans){
                    for(File file:dirsMap.get(cache).get(isExternal)){
                        float avail = megabytesAvailable(file);
                        try{
                            Log.i(LOGGER, String.format("Location:%s isExternal:%s isCache: %s Space: %s", file, isExternal, cache, avail));
                        }catch (Exception e){
                            Log.e(LOGGER, "Error printing $s" + file);
                        }
                    }
                }
            }
        }catch (Exception e){
            Log.e(LOGGER, "Error printing current map, something essential is missing");
        }
    }

    private static synchronized void recalculateLargestDir() {
        Log.i(LOGGER,"Recalculating largest dir");
        largestDirs = new HashMap<Boolean, HashMap<Boolean, FileSpace>>();
        Boolean[] booleans = new Boolean[]{true,false};
        for(Boolean cache:booleans){
            HashMap<Boolean, FileSpace> levelMap = new HashMap<Boolean, FileSpace>();
            largestDirs.put(cache,levelMap);
            for(Boolean isExternal:booleans){
                FileSpace max=new FileSpace(null,0);
                for(File file:dirsMap.get(cache).get(isExternal)){
                    float avail;
                    try{
                        avail = megabytesAvailable(file);
                    }catch (RuntimeException e){
//                        Crashlytics.logException(e);
                        avail = (float) 0.0;
                    }
                    if(avail>max.space){
                        max = new FileSpace(file, (int) avail);
                        Log.i(LOGGER, String.format("Setting cache?(%s) external?(%s) directory %s as best match with %s available", cache, isExternal, max.file, max.space));
                        levelMap.put(isExternal,max);
                    }
                }
            }
        }
        lastRecalculation = DateHelpers.unixTime();
    }


    private static void verifyAndAddDir(ArrayList<File> externalDirsList, File dir) {
        if(dir==null){
            return;
        }

        if(!dir.exists()){
            if(!dir.mkdirs()){
                return;
            }
        }
        if(dir.canWrite()){
            externalDirsList.add(dir);
        }
    }

    public static String randomTimeName(String ext) {
        String date_prefix = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String random = RandomHelpers.randNumString();
        return String.format("%s%s.%s", date_prefix, random, ext);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static float megabytesAvailable(File f) {
        StatFs stat = new StatFs(f.getPath());
        int currentapiVersion = Build.VERSION.SDK_INT;
        long bytesAvailable;
        if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            bytesAvailable = (long)stat.getBlockSizeLong() * (long)stat.getAvailableBlocksLong();
        } else{
            bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
        }

        return bytesAvailable / (1024.f * 1024.f);
    }

//    public static void clearFilesDirectory(Context context,String path){
//        clearFilesDirectory(context,path,true);
//    }
//
//    public static void clearFilesDirectory(Context context,String path, boolean recursive){
//        File targetDir;
//        try {
//            targetDir = new File(getStorageDir(context, false, false,0),path);
//        } catch (NoValidDir noValidDir) {
//            noValidDir.printStackTrace();
//            Crashlytics.logException(noValidDir);
//            return;
//        }
//
//        MLog.d(LOGGER,"Clearing "+targetDir.toString()+"recursiverly: "+recursive);
//        if(!targetDir.exists()){
//            MLog.d(LOGGER,"Exit, dir does not exists");
//            return;
//        }
//        MLog.d(LOGGER,"Looping thru files");
//        if(recursive){
//            deleteRecursive(targetDir);
//            targetDir.delete();
//        }else{
//            for(File file:targetDir.listFiles()){
//                if(file.isFile()){
//                    MLog.d(LOGGER,"Deleting "+file.toString());
//                    file.delete();
//                }
//            }
//        }
//        MLog.d(LOGGER,"Done clearing");
//    }

    public static boolean clearAllFilesPath(Context context, String clearDirectory) {
        buildDirsMap(context);
        boolean tookAction = false;
        for(boolean isExternal:new Boolean[]{true,false}){
            ArrayList<File> files;
            try{
                files = dirsMap.get(FILE_TYPE_PERMANENT).get(isExternal);
            }catch (NullPointerException e){
                if(dirsMap==null){
                    Log.e(LOGGER,"Dirsmap is null");
                }else if(dirsMap.get(FILE_TYPE_PERMANENT)==null){
                    Log.e(LOGGER,"Permanent filesmap is null");
                }else if(dirsMap.get(FILE_TYPE_PERMANENT).get(isExternal)==null){
                    Log.e(LOGGER,"Array is null for isExternal is:"+isExternal);
                }
                Log.e(LOGGER,"Missing directory, should have not happened");
                continue;
            }
            for(File file:files){
                File removePath = new File(file, clearDirectory);
                if(removePath.exists()){
                    tookAction=true;
                    deleteRecursive(removePath);
                }
            }
        }
        return tookAction;
    }

    public static void clearAllFiles(Context context){
        if(clearAllFilesPath(context,"")){
            Log.i(LOGGER,"Resetting all files data after clearing");
            resetAllFilesData(context);
        }
    }

    public static void clearAllFilesStartingWith(Context context,FilenameFilter filter){
        buildDirsMap(context);
        boolean tookAction = false;
        for(boolean isExternal:new Boolean[]{true,false}){
            ArrayList<File> dirs;
            try{
                dirs = dirsMap.get(FILE_TYPE_PERMANENT).get(isExternal);
            }catch (NullPointerException e){
                Log.e(LOGGER,"Missing directory, should have not happened");
                continue;
            }
            for(File file:dirs){
                if(file.exists()){
                    File[] files = file.listFiles(filter);
                    for(File filetoRemove:files){
                        tookAction=true;
                        deleteRecursive(filetoRemove);
                    }
                }
            }
        }
        if(tookAction){
            Log.i(LOGGER,"Resetting all files data after clearing");
            resetAllFilesData(context);
        }

    }


    public static void deleteRecursive(File fileOrDirectory) {
        Log.d(LOGGER,"Recursive delete of "+fileOrDirectory);

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}
