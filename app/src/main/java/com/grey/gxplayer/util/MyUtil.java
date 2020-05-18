package com.grey.gxplayer.util;

public class MyUtil {

    public enum  IdType{
        NA(0),
        ALBUM(1),
        ARTIST(2);

        public final int mId;

        IdType(int mId){
            this.mId = mId;
        }

        public static  IdType getInstance(int id){
            for (IdType type:values()){
                if(type.mId == id){
                    return type;
                }
            }
            throw new IllegalArgumentException("Unrecognized ID");
        }
    }
}
